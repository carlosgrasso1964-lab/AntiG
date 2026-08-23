<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$db = Database::getInstance()->getConnection();
$action = $_GET['action'] ?? 'list';

function buildRedirect() {
    $params = [];
    if (isset($_GET['filtro'])) $params['filtro'] = $_GET['filtro'];
    if (!empty($_GET['filtro_recurso'])) $params['filtro_recurso'] = $_GET['filtro_recurso'];
    if (!empty($_GET['tipo']) && $_GET['tipo'] !== 'T') $params['tipo'] = $_GET['tipo'];
    if (!empty($_GET['ordem'])) $params['ordem'] = $_GET['ordem'];
    if (!empty($_GET['pagina'])) $params['pagina'] = (int)$_GET['pagina'];
    return 'lancamentos.php' . ($params ? '?' . http_build_query($params) : '');
}

// ====== PROCESSAMENTO ANTES DE QUALQUER OUTPUT ======

// DELETE individual
if ($action === 'delete' && ($idMov = (int)($_GET['id'] ?? 0))) {
    try {
        $db->prepare("DELETE FROM tbmovimento WHERE idMov=?")->execute([$idMov]);
        flashMessage('success', 'Lançamento excluído!');
    } catch (Exception $e) {
        flashMessage('danger', 'Erro ao excluir: ' . $e->getMessage());
    }
    redirect(buildRedirect());
}

// DELETE múltiplo
if ($action === 'delete_multiple' && isset($_POST['ids'])) {
    try {
        $ids = array_map('intval', $_POST['ids']);
        $placeholders = implode(',', array_fill(0, count($ids), '?'));
        $db->prepare("DELETE FROM tbmovimento WHERE idMov IN ($placeholders)")->execute($ids);
        flashMessage('success', count($ids) . ' lançamento(s) excluído(s)!');
    } catch (Exception $e) {
        flashMessage('danger', 'Erro ao excluir: ' . $e->getMessage());
    }
    redirect(buildRedirect());
}

// RECEBER
if ($action === 'receber' && $_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $db->beginTransaction();
        $idMov = (int)$_POST['idMov'];
        $stmt = $db->prepare("SELECT * FROM tbmovimento WHERE idMov=?");
        $stmt->execute([$idMov]);
        $mov = $stmt->fetch();
        if (!$mov) throw new Exception('Registro não encontrado');
        if (in_array($mov['statusMov'], ['PG', 'RC'])) throw new Exception('Registro já está pago/recebido');

        $dtPagto = dataParaSQL($_POST['dtPagto']);
        $recursoDest = $_POST['recurso_destino'];

        $st = $db->prepare("SELECT fk_gpprinc FROM tbrecursos WHERE codigo=?");
        $st->execute([$recursoDest]);
        $dest = $st->fetch();
        if (!$dest) throw new Exception('Recurso destino não encontrado');

        $valorOrig = (float)$mov['Valor'];
        $desconto = (float)(str_replace([',', '.'], ['', ''], $_POST['desconto'] ?? '0')) / 100;
        $juros = (float)(str_replace([',', '.'], ['', ''], $_POST['juros'] ?? '0')) / 100;

        // 1. UPDATE original
        $u = $db->prepare("UPDATE tbmovimento SET statusMov='RC', Prev='V', dtApr=? WHERE idMov=?");
        $u->execute([$dtPagto, $idMov]);

        // 2. Baixa Contas a Receber (mesmo recurso, vrecurso=1.002.001)
        $i1 = $db->prepare("INSERT INTO tbmovimento (recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,Valor,dtApr,statusMov,Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        $i1->execute([
            $mov['recurso'], '1.002.001',
            $mov['clifor'], $mov['vCliFor'],
            $dtPagto, $mov['dtEmi'], $mov['dtVcto'],
            $mov['documento'], '9.001.003',
            ($mov['Descr'] ?? '') . ' -RC- ' . $idMov,
            -$valorOrig, $dtPagto, 'RC', 'V',
        ]);

        // 3. Entrada no Recurso Destino
        $i2 = $db->prepare("INSERT INTO tbmovimento (recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,Valor,dtApr,statusMov,Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        $i2->execute([
            $recursoDest, $dest['fk_gpprinc'],
            $mov['clifor'], $mov['vCliFor'],
            $dtPagto, $mov['dtEmi'], $mov['dtVcto'],
            $mov['documento'], '9.001.003',
            ($mov['Descr'] ?? '') . ' -RC- ' . $idMov,
            $valorOrig, $dtPagto, 'RC', 'V',
        ]);

        // 4. Desconto Concedido
        if ($desconto > 0) {
            $db->prepare("INSERT INTO tbmovimento (recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,Valor,dtApr,statusMov,Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")->execute([
                $recursoDest, $dest['fk_gpprinc'],
                $mov['clifor'], $mov['vCliFor'],
                $dtPagto, $mov['dtEmi'], $mov['dtVcto'],
                $mov['documento'], '4.008.006',
                'DESCONTO - ' . ($mov['Descr'] ?? '') . ' -RC- ' . $idMov,
                -$desconto, $dtPagto, 'DC', 'V',
            ]);
        }

        // 5. Juros Recebidos
        if ($juros > 0) {
            $db->prepare("INSERT INTO tbmovimento (recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,Valor,dtApr,statusMov,Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")->execute([
                $recursoDest, $dest['fk_gpprinc'],
                $mov['clifor'], $mov['vCliFor'],
                $dtPagto, $mov['dtEmi'], $mov['dtVcto'],
                $mov['documento'], '3.003.001',
                'JUROS - ' . ($mov['Descr'] ?? '') . ' -RC- ' . $idMov,
                $juros, $dtPagto, 'JP', 'V',
            ]);
        }

        $db->commit();
        flashMessage('success', 'Recebimento efetuado com sucesso!');
    } catch (Exception $e) {
        $db->rollBack();
        flashMessage('danger', 'Erro no recebimento: ' . $e->getMessage());
    }
    redirect(buildRedirect());
}

// PAGAR
if ($action === 'pagar' && $_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $db->beginTransaction();
        $idMov = (int)$_POST['idMov'];
        $stmt = $db->prepare("SELECT * FROM tbmovimento WHERE idMov=?");
        $stmt->execute([$idMov]);
        $mov = $stmt->fetch();
        if (!$mov) throw new Exception('Registro não encontrado');
        if (in_array($mov['statusMov'], ['PG', 'RC'])) throw new Exception('Registro já está pago/recebido');

        $dtPagto = dataParaSQL($_POST['dtPagto']);
        $recursoOrig = $_POST['recurso_origem'];

        $st = $db->prepare("SELECT fk_gpprinc FROM tbrecursos WHERE codigo=?");
        $st->execute([$recursoOrig]);
        $orig = $st->fetch();
        if (!$orig) throw new Exception('Recurso origem não encontrado');

        $valorOrig = (float)$mov['Valor'];
        $valorPago = $valorOrig;
        $desconto = (float)(str_replace([',', '.'], ['', ''], $_POST['desconto'] ?? '0')) / 100;
        $juros = (float)(str_replace([',', '.'], ['', ''], $_POST['juros'] ?? '0')) / 100;

        // 1. UPDATE original
        $u = $db->prepare("UPDATE tbmovimento SET statusMov='PG', Prev='V', dtApr=? WHERE idMov=?");
        $u->execute([$dtPagto, $idMov]);

        // 2. Baixa Contas a Pagar (recurso 0079, vrecurso 2.001.002)
        $i1 = $db->prepare("INSERT INTO tbmovimento (recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,Valor,dtApr,statusMov,Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        $i1->execute([
            '0079', '2.001.002',
            $mov['clifor'], $mov['vCliFor'],
            $dtPagto, $mov['dtEmi'], $mov['dtVcto'],
            $mov['documento'], '9.001.003',
            ($mov['Descr'] ?? '') . ' -PG- ' . $idMov,
            -$valorPago, $dtPagto, 'PG', 'V',
        ]);

        // 3. Saida do Recurso Origem
        $i2 = $db->prepare("INSERT INTO tbmovimento (recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,Valor,dtApr,statusMov,Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        $i2->execute([
            $recursoOrig, $orig['fk_gpprinc'],
            $mov['clifor'], $mov['vCliFor'],
            $dtPagto, $mov['dtEmi'], $mov['dtVcto'],
            $mov['documento'], '9.001.003',
            ($mov['Descr'] ?? '') . ' -PG- ' . $idMov,
            $valorPago, $dtPagto, 'PG', 'V',
        ]);

        // 4. Desconto Obtido
        if ($desconto > 0) {
            $db->prepare("INSERT INTO tbmovimento (recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,Valor,dtApr,statusMov,Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")->execute([
                $recursoOrig, $orig['fk_gpprinc'],
                $mov['clifor'], $mov['vCliFor'],
                $dtPagto, $mov['dtEmi'], $mov['dtVcto'],
                $mov['documento'], '3.003.003',
                'DESCONTO - ' . ($mov['Descr'] ?? '') . ' -PG- ' . $idMov,
                $desconto, $dtPagto, 'DO', 'V',
            ]);
        }

        // 5. Juros Pagos
        if ($juros > 0) {
            $db->prepare("INSERT INTO tbmovimento (recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,Valor,dtApr,statusMov,Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)")->execute([
                $recursoOrig, $orig['fk_gpprinc'],
                $mov['clifor'], $mov['vCliFor'],
                $dtPagto, $mov['dtEmi'], $mov['dtVcto'],
                $mov['documento'], '4.008.003',
                'JUROS - ' . ($mov['Descr'] ?? '') . ' -PG- ' . $idMov,
                -$juros, $dtPagto, 'JP', 'V',
            ]);
        }

        $db->commit();
        flashMessage('success', 'Pagamento efetuado com sucesso!');
    } catch (Exception $e) {
        $db->rollBack();
        flashMessage('danger', 'Erro no pagamento: ' . $e->getMessage());
    }
    redirect(buildRedirect());
}

// SAVE (add / edit)
if ($_SERVER['REQUEST_METHOD'] === 'POST' && $action !== 'delete_multiple') {
    try {
        $db->beginTransaction();
        // Tipo (Entrada/Saída/Nulo) define o sinal — igual ao jCbxTipo do Java (VMov.java).
        // O usuário sempre digita o valor positivo; apenas Saída recebe sinal negativo.
        $tipo = $_POST['tipo'] ?? 'E';
        $magnitude = abs((float)str_replace(['.', ','], ['', '.'], $_POST['Valor']));
        $valor = ($tipo === 'S') ? -$magnitude : $magnitude;
        $dtVcto = dataParaSQL($_POST['dtVcto']);
        $dtEmi = dataParaSQL($_POST['dtEmi']);
        $dtLancto = dataParaSQL($_POST['dtLancto']);

        // Busca vrecurso e vCliFor do banco (sem depender de JS)
        $vrecurso = $_POST['vrecurso'] ?? '';
        if (!empty($_POST['recurso'])) {
            $st = $db->prepare("SELECT fk_gpprinc FROM tbrecursos WHERE codigo=?");
            $st->execute([$_POST['recurso']]);
            $r = $st->fetch();
            if ($r && $r['fk_gpprinc']) $vrecurso = $r['fk_gpprinc'];
        }
        $vCliFor = $_POST['vCliFor'] ?? '';
        if (!empty($_POST['clifor'])) {
            $st = $db->prepare("SELECT fkCliForGp FROM tbclifor WHERE codCliFor=?");
            $st->execute([$_POST['clifor']]);
            $r = $st->fetch();
            if ($r && $r['fkCliForGp']) $vCliFor = $r['fkCliForGp'];
        }

        $dados = [
            'recurso' => $_POST['recurso'],
            'vrecurso' => $vrecurso,
            'clifor' => $_POST['clifor'],
            'vCliFor' => $vCliFor,
            'dtlancto' => $dtLancto,
            'dtEmi' => $dtEmi,
            'dtVcto' => $dtVcto,
            'documento' => mb_strtoupper(trim($_POST['documento'])),
            'classif' => $_POST['classif'],
            'Descr' => mb_strtoupper(trim($_POST['Descr'])),
            'Valor' => $valor,
            'statusMov' => $_POST['statusMov'],
            'Prev' => $_POST['Prev'],
        ];

        $idMov = (int)($_POST['idMov'] ?? 0);
        $dtApr = !empty($_POST['dtApr']) ? dataParaSQL($_POST['dtApr']) : null;

        if ($idMov) {
            $sql = "UPDATE tbmovimento SET recurso=?, vrecurso=?, clifor=?, vCliFor=?, dtlancto=?, dtEmi=?, dtVcto=?, documento=?, classif=?, Descr=?, Valor=?, dtApr=?, statusMov=?, Prev=? WHERE idMov=?";
            $valores = array_values($dados);
            array_splice($valores, 11, 0, [$dtApr]);
            $valores[] = $idMov;
            $db->prepare($sql)->execute($valores);
            flashMessage('success', 'Lançamento atualizado!');
        } elseif ($vrecurso === '2.001.003') {
            // --- CARTÃO DE CRÉDITO: Três etapas ---
            // Referência: VMov.java (Java original). O valor digitado é tratado
            // como positivo na tela; o sinal correto é aplicado em cada etapa:
            //   1ª (principal): NEGATIVO  - lançamento a pagar (recurso 0079)
            //   2ª (estorno)  : POSITIVO   - contrapartida (recurso 0079, classif 9.001.003)
            //   3ª (passivo)  : NEGATIVO   - saída pelo cartão de crédito (recurso original)
            $valorAbs = abs((float)$dados['Valor']);
            $valorNeg = -$valorAbs;

            // 1ª: Lançamento principal (classif do usuário, status=PG, fixo recurso=0079) — NEGATIVO
            $sql1 = "INSERT INTO tbmovimento (recurso, vrecurso, clifor, vCliFor, dtlancto, dtEmi, dtVcto, documento, classif, Descr, Valor, dtApr, statusMov, Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            $db->prepare($sql1)->execute([
                '0079', $dados['vrecurso'],
                $dados['clifor'], $dados['vCliFor'],
                $dados['dtlancto'], $dados['dtEmi'], $dados['dtVcto'],
                $dados['documento'],
                $dados['classif'],
                $dados['Descr'],
                $valorNeg,
                $dtApr,
                'PG', 'V',
            ]);

            // 2ª: Estorno (classif=9.001.003, valor absoluto +, status=PG) — POSITIVO
            $sql2 = "INSERT INTO tbmovimento (recurso, vrecurso, clifor, vCliFor, dtlancto, dtEmi, dtVcto, documento, classif, Descr, Valor, dtApr, statusMov, Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            $db->prepare($sql2)->execute([
                '0079', $dados['vrecurso'],
                $dados['clifor'], $dados['vCliFor'],
                $dados['dtlancto'], $dados['dtEmi'], $dados['dtVcto'],
                $dados['documento'],
                '9.001.003',
                $dados['Descr'],
                $valorAbs,
                $dtApr,
                'PG', 'V',
            ]);

            // 3ª: Passivo (classif=9.001.003, recurso original, dtApr=null, status=vazio) — NEGATIVO
            $sql3 = "INSERT INTO tbmovimento (recurso, vrecurso, clifor, vCliFor, dtlancto, dtEmi, dtVcto, documento, classif, Descr, Valor, dtApr, statusMov, Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            $db->prepare($sql3)->execute([
                $dados['recurso'], $dados['vrecurso'],
                $dados['clifor'], $dados['vCliFor'],
                $dados['dtlancto'], $dados['dtEmi'], $dados['dtVcto'],
                $dados['documento'],
                '9.001.003',
                $dados['Descr'],
                $valorNeg,
                null,
                '', 'V',
            ]);

            flashMessage('success', 'Cartão de crédito lançado com sucesso (3 etapas)!');
        } else {
            $sql = "INSERT INTO tbmovimento (recurso, vrecurso, clifor, vCliFor, dtlancto, dtEmi, dtVcto, documento, classif, Descr, Valor, dtApr, statusMov, Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            $valores = array_values($dados);
            array_splice($valores, 11, 0, [$dtApr]);
            $db->prepare($sql)->execute($valores);
            flashMessage('success', 'Lançamento cadastrado!');
        }
        $db->commit();
        redirect(buildRedirect());
    } catch (Exception $e) {
        $db->rollBack();
        flashMessage('danger', 'Erro: ' . $e->getMessage());
    }
}

// ====== RENDERIZAÇÃO ======

$title = 'Lançamentos';
include '../includes/header.php';

// Filtro de tipo (E=Entrada/S=Saída/N=Nulo)
$tipo = $_GET['tipo'] ?? 'T';

// Filtro de status
$filtro = $_GET['filtro'] ?? 'abertos';
$where = "1=1";
if ($filtro === 'abertos') {
    $where .= " AND m.statusMov NOT IN ('PG', 'RC', 'TD')";
} elseif ($filtro === 'pagos') {
    $where .= " AND m.statusMov IN ('PG', 'RC')";
} elseif ($filtro === 'prev') {
    $where .= " AND m.Prev = 'F'";
}

// Filtro por recurso
$filtroRecurso = $_GET['filtro_recurso'] ?? '';
if ($filtroRecurso) {
    $stmtR = $db->prepare("SELECT codigo FROM tbrecursos WHERE codigo = ?");
    $stmtR->execute([$filtroRecurso]);
    if ($stmtR->fetch()) {
        $where .= " AND m.recurso = " . $db->quote($filtroRecurso);
    }
}

// Ordenação
$ordem = $_GET['ordem'] ?? 'vencimento';
if ($filtro === 'recentes') {
    $ordem = 'recentes';
}
if ($ordem === 'recentes') {
    $orderBy = "ORDER BY m.idMov DESC";
} else {
    $orderBy = "ORDER BY m.dtVcto, m.recurso, m.dtEmi";
}

// Paginação (50 registros por vez)
$porPagina = 50;
$pagina = max(1, (int)($_GET['pagina'] ?? 1));

// Só carrega registros se um recurso foi selecionado
$lancamentos = [];
$totalRecords = 0;
$totalPages = 1;
if (!empty($filtroRecurso)) {
    // Total de registros
    $totalRecords = (int)$db->query("SELECT COUNT(*) FROM tbmovimento m WHERE $where")->fetchColumn();
    $totalPages = max(1, ceil($totalRecords / $porPagina));
    if ($pagina > $totalPages) $pagina = $totalPages;
    $offset = ($pagina - 1) * $porPagina;

    // Saldo acumulado antes da página atual
    $sa = 0;
    if ($offset > 0) {
        $sa = (float)$db->query("SELECT COALESCE(SUM(Valor), 0) FROM (SELECT Valor FROM tbmovimento m WHERE $where $orderBy LIMIT $offset) sub")->fetchColumn();
    }

    // Registros da página
    $stmt = $db->query("
        SELECT m.*, r.nomebco, r.fk_gpprinc as vrecurso_nome,
               c.nomeCliFor, c.fkCliForGp as vclifor_nome,
               g.nome_C as nomeClassif, g2.nome_C as nomeVRecurso
        FROM tbmovimento m
        LEFT JOIN tbrecursos r ON m.recurso = r.codigo
        LEFT JOIN tbclifor c ON m.clifor = c.codCliFor
        LEFT JOIN gpprincipal g ON m.classif = g.cod_Geral
        LEFT JOIN gpprincipal g2 ON m.vrecurso = g2.cod_Geral
        WHERE $where
        $orderBy
        LIMIT $porPagina OFFSET $offset
    ");
    $lancamentos = $stmt->fetchAll();
}

// Calcular saldo acumulado (pré-filtrando por tipo)
$saldoAcumulado = 0;
foreach ($lancamentos as &$m) {
    $m['_tipo'] = $m['Valor'] > 0 ? 'E' : ($m['Valor'] < 0 ? 'S' : 'N');
    $m['saldo_acum'] = 0;
}
unset($m);

$editMov = null;
$editId = (int)($_GET['id'] ?? 0);
if ($action === 'edit' && $editId) {
    $stmt = $db->prepare("SELECT * FROM tbmovimento WHERE idMov=?");
    $stmt->execute([$editId]);
    $editMov = $stmt->fetch();
    if ($editMov) {
        $editMov['_tipo'] = $editMov['Valor'] > 0 ? 'E' : ($editMov['Valor'] < 0 ? 'S' : 'N');
    }
}

$recursos = $db->query("SELECT codigo, nomebco, fk_gpprinc FROM tbrecursos WHERE status='A' ORDER BY nomebco")->fetchAll();
$clifors = $db->query("SELECT codCliFor, nomeCliFor, fkCliForGp FROM tbclifor ORDER BY nomeCliFor")->fetchAll();
$planos = $db->query("SELECT cod_Geral, nome_C FROM gpprincipal ORDER BY cod_Geral")->fetchAll();
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-journal-text"></i> Lançamentos</h4>
    <div>
        <button class="btn btn-danger me-2 d-none" id="btnDeleteSelected" data-action="delete_multiple">
            <i class="bi bi-trash"></i> Excluir Selecionados
        </button>
        <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#modalMov">
            <i class="bi bi-plus-lg"></i> Novo Lançamento
        </button>
    </div>
</div>

<div class="card">
    <div class="card-body">
        <!-- Filtros -->
        <div class="row mb-3">
            <div class="col-md-6">
                <ul class="nav nav-tabs">
                    <li class="nav-item">
                        <a class="nav-link <?= $filtro === 'abertos' ? 'active' : '' ?>" href="?filtro=abertos<?= $tipo !== 'T' ? '&tipo='.$tipo : '' ?><?= $filtroRecurso ? '&filtro_recurso='.$filtroRecurso : '' ?><?= $ordem ? '&ordem='.$ordem : '' ?>">Abertos</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link <?= $filtro === 'pagos' ? 'active' : '' ?>" href="?filtro=pagos<?= $tipo !== 'T' ? '&tipo='.$tipo : '' ?><?= $filtroRecurso ? '&filtro_recurso='.$filtroRecurso : '' ?><?= $ordem ? '&ordem='.$ordem : '' ?>">Pagos/Recebidos</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link <?= $filtro === 'prev' ? 'active' : '' ?>" href="?filtro=prev<?= $tipo !== 'T' ? '&tipo='.$tipo : '' ?><?= $filtroRecurso ? '&filtro_recurso='.$filtroRecurso : '' ?><?= $ordem ? '&ordem='.$ordem : '' ?>">Previsões</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link <?= $filtro === 'todos' ? 'active' : '' ?>" href="?filtro=todos<?= $tipo !== 'T' ? '&tipo='.$tipo : '' ?><?= $filtroRecurso ? '&filtro_recurso='.$filtroRecurso : '' ?><?= $ordem ? '&ordem='.$ordem : '' ?>">Todos</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link <?= $filtro === 'recentes' ? 'active' : '' ?>" href="?filtro=recentes<?= $filtroRecurso ? '&filtro_recurso='.$filtroRecurso : '' ?>" title="Últimos registros cadastrados">
                            <i class="bi bi-clock-history"></i> Recentes
                        </a>
                    </li>
                </ul>
            </div>
            <div class="col-md-2">
                <select class="form-select" id="filtroRecurso" onchange="filtrarPorRecurso()">
                    <option value="">Todos os Recursos</option>
                    <?php foreach ($recursos as $r): ?>
                    <option value="<?= htmlspecialchars($r['codigo']) ?>" <?= $filtroRecurso === $r['codigo'] ? 'selected' : '' ?>>
                        <?= htmlspecialchars($r['codigo'] . ' - ' . $r['nomebco']) ?>
                    </option>
                    <?php endforeach; ?>
                </select>
            </div>
            <div class="col-md-2">
                <select class="form-select" id="filtroTipo" onchange="filtrarPorTipo()">
                    <option value="T" <?= $tipo === 'T' ? 'selected' : '' ?>>Todos os Tipos</option>
                    <option value="E" <?= $tipo === 'E' ? 'selected' : '' ?>>Entradas (E)</option>
                    <option value="S" <?= $tipo === 'S' ? 'selected' : '' ?>>Saídas (S)</option>
                    <option value="N" <?= $tipo === 'N' ? 'selected' : '' ?>>Nulos (N)</option>
                </select>
            </div>
            <div class="col-md-2">
                <select class="form-select" id="filtroOrdem" onchange="filtrarPorOrdem()">
                    <option value="vencimento" <?= $ordem === 'vencimento' ? 'selected' : '' ?>>Ordenar por Vencimento</option>
                    <option value="recentes" <?= $ordem === 'recentes' ? 'selected' : '' ?>>Mais Recentes</option>
                </select>
            </div>
        </div>

        <div class="table-responsive">
            <form id="formMultipleDelete" method="POST" action="?action=delete_multiple&amp;filtro=<?= $filtro ?><?= $filtroRecurso ? '&amp;filtro_recurso='.$filtroRecurso : '' ?><?= ($tipo !== 'T') ? '&amp;tipo='.$tipo : '' ?><?= $ordem ? '&amp;ordem='.$ordem : '' ?><?= $pagina > 1 ? '&amp;pagina='.$pagina : '' ?>">
                <table class="table table-hover table-sm" id="tbDadosMov">
                    <thead class="table-light">
                        <tr>
                            <th width="30"><input type="checkbox" id="checkAll"></th>
                            <th title="Registro">Reg.</th>
                            <th title="Recurso">Recurso</th>
                            <th title="vRecurso">vRecurso</th>
                            <th title="Favorecido">Favorecido</th>
                            <th title="vFavorecido">vFavorecido</th>
                            <th title="Lançamento">Lancto.</th>
                            <th title="Emissão">Emissão</th>
                            <th title="Vencimento">Vencimento</th>
                            <th title="Documento">Documento</th>
                            <th title="Classificação">Classif</th>
                            <th title="Descrição">Descrição</th>
                            <th class="text-end" title="Valor">Valor</th>
                            <th title="Apresentação">Apresentação</th>
                            <th title="Status">Status</th>
                            <th title="Previsão">Prev</th>
                            <th class="text-end" title="Saldo">Saldo</th>
                            <th width="80">Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php if (empty($lancamentos)): ?>
                        <tr>
                            <td colspan="18" class="text-center text-muted py-4">
                                <?= empty($filtroRecurso) ? '<i class="bi bi-search"></i> Selecione um Recurso acima para exibir os lançamentos.' : 'Nenhum lançamento encontrado.' ?>
                            </td>
                        </tr>
                        <?php endif; ?>
                        <?php
                        $sa = 0;
                        foreach ($lancamentos as $m):
                            $vencido = $m['dtVcto'] < date('Y-m-d') && !in_array($m['statusMov'], ['PG', 'RC', 'TD']);
                            if ($tipo !== 'T' && $tipo !== $m['_tipo']) continue;
                            $sa += (float)$m['Valor'];
                            $m['saldo_acum'] = $sa;
                        ?>
                        <tr class="<?= $vencido ? 'table-danger' : '' ?>">
                            <td><input type="checkbox" name="ids[]" value="<?= $m['idMov'] ?>" class="checkItem"></td>
                            <td class="text-center"><?= $m['idMov'] ?></td>
                            <td><code><?= htmlspecialchars($m['recurso']) ?></code></td>
                            <td><code><?= htmlspecialchars($m['vrecurso']) ?></code></td>
                            <td><?= htmlspecialchars($m['nomeCliFor'] ?? $m['clifor']) ?></td>
                            <td><code><?= htmlspecialchars($m['vCliFor'] ?? '') ?></code></td>
                            <td><?= formatData($m['dtlancto']) ?></td>
                            <td><?= formatData($m['dtEmi']) ?></td>
                            <td><?= formatData($m['dtVcto']) ?></td>
                            <td><?= htmlspecialchars($m['documento']) ?></td>
                            <td><code><?= htmlspecialchars($m['classif']) ?></code></td>
                            <td><?= htmlspecialchars(mb_substr($m['Descr'], 0, 50)) ?></td>
                            <td class="text-end <?= $m['Valor'] < 0 ? 'valor-negativo' : 'valor-positivo' ?>">
                                <?= formatMoeda($m['Valor']) ?>
                            </td>
                            <td><?= formatData($m['dtApr']) ?></td>
                            <td><?= statusBadge($m['statusMov']) ?></td>
                            <td class="text-center"><?= htmlspecialchars($m['Prev']) ?></td>
                            <td class="text-end fw-bold <?= $sa < 0 ? 'valor-negativo' : 'valor-positivo' ?>">
                                <?= 'R$ ' . number_format($sa, 2, ',', '.') ?>
                            </td>
                            <td>
                                <?php if ($m['Valor'] > 0): ?>
                                <button type="button" class="btn btn-sm btn-success"
                                    data-mov='<?= htmlspecialchars(json_encode(['id'=>$m['idMov'],'recurso'=>$m['recurso'],'favorecido'=>($m['nomeCliFor']??$m['clifor']),'clifor'=>$m['clifor'],'documento'=>$m['documento'],'valor'=>$m['Valor'],'descricao'=>$m['Descr'],'dtVcto'=>$m['dtVcto'],'dtEmi'=>$m['dtEmi'],'vCliFor'=>$m['vCliFor']]), ENT_QUOTES) ?>'
                                    title="Receber"
                                    onclick="abrirModalReceber(this)">
                                    <i class="bi bi-cash-coin"></i>
                                </button>
                                <?php endif; ?>
                                <?php if ($m['Valor'] < 0): ?>
                                <button type="button" class="btn btn-sm btn-warning"
                                    data-mov='<?= htmlspecialchars(json_encode(['id'=>$m['idMov'],'recurso'=>$m['recurso'],'favorecido'=>($m['nomeCliFor']??$m['clifor']),'clifor'=>$m['clifor'],'documento'=>$m['documento'],'valor'=>$m['Valor'],'descricao'=>$m['Descr'],'dtVcto'=>$m['dtVcto'],'dtEmi'=>$m['dtEmi'],'vCliFor'=>$m['vCliFor']]), ENT_QUOTES) ?>'
                                    title="Pagar"
                                    onclick="abrirModalPagar(this)">
                                    <i class="bi bi-credit-card"></i>
                                </button>
                                <?php endif; ?>
                                <a href="?action=edit&id=<?= $m['idMov'] ?>&filtro=<?= $filtro ?><?= $filtroRecurso ? '&filtro_recurso='.$filtroRecurso : '' ?><?= ($tipo !== 'T') ? '&tipo='.$tipo : '' ?><?= $ordem ? '&ordem='.$ordem : '' ?><?= $pagina > 1 ? '&pagina='.$pagina : '' ?>" class="btn btn-sm btn-outline-primary" title="Editar">
                                    <i class="bi bi-pencil"></i>
                                </a>
                                <a href="?action=delete&id=<?= $m['idMov'] ?>&filtro=<?= $filtro ?><?= ($tipo !== 'T') ? '&tipo='.$tipo : '' ?><?= $ordem ? '&ordem='.$ordem : '' ?><?= $pagina > 1 ? '&pagina='.$pagina : '' ?>" class="btn btn-sm btn-outline-danger" title="Excluir" data-confirm="Excluir lançamento <?= $m['idMov'] ?>?">
                                    <i class="bi bi-trash"></i>
                                </a>
                            </td>
                        </tr>
                        <?php endforeach; ?>
                    </tbody>
                    <?php if (!empty($lancamentos)):
                    $totalValor = 0;
                    foreach ($lancamentos as $m) {
                        if ($tipo !== 'T' && $tipo !== $m['_tipo']) continue;
                        $totalValor += (float)$m['Valor'];
                    }
                    ?>
                    <tfoot class="table-light">
                        <tr>
                            <th colspan="12" class="text-end">Totais:</th>
                            <th class="text-end"><?= formatMoeda($totalValor) ?></th>
                            <th colspan="4"></th>
                            <th class="text-end"><?= 'R$ ' . number_format($sa, 2, ',', '.') ?></th>
                            <th></th>
                        </tr>
                    </tfoot>
                    <?php endif; ?>
                </table>
            </form>
        </div>
    </div>
</div>

<?php if (!empty($filtroRecurso) && $totalPages > 1): ?>
<div class="d-flex justify-content-between align-items-center mb-3">
    <small class="text-muted"><?= $totalRecords ?> registro(s) — Página <?= $pagina ?> de <?= $totalPages ?></small>
    <nav>
        <ul class="pagination pagination-sm mb-0">
            <li class="page-item <?= $pagina <= 1 ? 'disabled' : '' ?>">
                <a class="page-link" href="?<?= http_build_query(array_merge($_GET, ['pagina' => $pagina - 1])) ?>">&laquo;</a>
            </li>
            <?php
            $inicio = max(1, $pagina - 4);
            $fim = min($totalPages, $pagina + 4);
            for ($i = $inicio; $i <= $fim; $i++):
            ?>
            <li class="page-item <?= $i === $pagina ? 'active' : '' ?>">
                <a class="page-link" href="?<?= http_build_query(array_merge($_GET, ['pagina' => $i])) ?>"><?= $i ?></a>
            </li>
            <?php endfor; ?>
            <li class="page-item <?= $pagina >= $totalPages ? 'disabled' : '' ?>">
                <a class="page-link" href="?<?= http_build_query(array_merge($_GET, ['pagina' => $pagina + 1])) ?>">&raquo;</a>
            </li>
        </ul>
    </nav>
</div>
<?php endif; ?>

<!-- Modal do formulário -->
<div class="modal fade" id="modalMov" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <form method="POST" action="?action=<?= $editMov ? 'edit' : 'add' ?>&amp;filtro=<?= $filtro ?><?= $filtroRecurso ? '&amp;filtro_recurso='.$filtroRecurso : '' ?><?= ($tipo !== 'T') ? '&amp;tipo='.$tipo : '' ?><?= $ordem ? '&amp;ordem='.$ordem : '' ?><?= $pagina > 1 ? '&amp;pagina='.$pagina : '' ?>">
                <div class="modal-header">
                    <h5 class="modal-title"><?= $editMov ? 'Editar' : 'Novo' ?> Lançamento</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <!-- Linha 1: Registro + Recurso -->
                    <div class="row mb-2">
                        <div class="col-md-2">
                            <label class="form-label">Registro</label>
                            <input type="text" class="form-control bg-light" value="<?= $editMov ? $editMov['idMov'] : '(automático)' ?>" readonly>
                            <?php if ($editMov): ?>
                            <input type="hidden" name="idMov" value="<?= $editMov['idMov'] ?>">
                            <?php endif; ?>
                        </div>
                        <div class="col-md-5">
                            <label class="form-label required">Recurso (Conta)</label>
                            <select name="recurso" id="recurso" class="form-select select2" required>
                                <option value="">Selecione...</option>
                                <?php foreach ($recursos as $r): ?>
                                <option value="<?= htmlspecialchars($r['codigo']) ?>" data-fk_gpprinc="<?= htmlspecialchars($r['fk_gpprinc']) ?>" data-nomebco="<?= htmlspecialchars($r['nomebco']) ?>" <?= ($editMov['recurso'] ?? '') === $r['codigo'] ? 'selected' : '' ?>>
                                    <?= htmlspecialchars($r['codigo'] . ' - ' . $r['nomebco']) ?>
                                </option>
                                <?php endforeach; ?>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Nr. Recurso</label>
                            <input type="text" id="tfNrRecurso" class="form-control bg-light" value="<?= htmlspecialchars($editMov['recurso'] ?? '') ?>" readonly>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">vRecurso (classif.)</label>
                            <input type="text" id="tfRecurso" class="form-control bg-light" value="<?= htmlspecialchars($editMov['vrecurso'] ?? '') ?>" readonly>
                            <input type="hidden" name="vrecurso" id="vrecurso" value="<?= htmlspecialchars($editMov['vrecurso'] ?? '') ?>">
                        </div>
                    </div>

                    <!-- Linha 2: Favorecido -->
                    <div class="row mb-2">
                        <div class="col-md-5">
                            <label class="form-label required">Favorecido (Cli/For)</label>
                            <select name="clifor" id="clifor" class="form-select select2" required>
                                <option value="">Selecione...</option>
                                <?php foreach ($clifors as $c): ?>
                                <option value="<?= htmlspecialchars($c['codCliFor']) ?>" data-fkCliForGp="<?= htmlspecialchars($c['fkCliForGp'] ?: '') ?>" data-nome="<?= htmlspecialchars($c['nomeCliFor']) ?>" <?= ($editMov['clifor'] ?? '') === $c['codCliFor'] ? 'selected' : '' ?>>
                                    <?= htmlspecialchars($c['codCliFor'] . ' - ' . $c['nomeCliFor']) ?>
                                </option>
                                <?php endforeach; ?>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Nr. Favorecido</label>
                            <input type="text" id="tfNrFav" class="form-control bg-light" value="<?= htmlspecialchars($editMov['clifor'] ?? '') ?>" readonly>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">Classif. Favorecido</label>
                            <input type="text" id="tfFavorecidos" class="form-control bg-light" value="<?= htmlspecialchars($editMov['vCliFor'] ?? '') ?>" readonly>
                            <input type="hidden" name="vCliFor" id="vCliFor" value="<?= htmlspecialchars($editMov['vCliFor'] ?? '') ?>">
                        </div>
                    </div>

                    <!-- Linha 3: Datas -->
                    <div class="row mb-2">
                        <div class="col-md-3">
                            <label class="form-label">Lançamento</label>
                            <input type="text" name="dtLancto" class="form-control datepicker" id="dtLancto" value="<?= $editMov ? formatData($editMov['dtlancto']) : date('d/m/Y') ?>">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label required">Emissão</label>
                            <input type="text" name="dtEmi" class="form-control datepicker" value="<?= $editMov ? formatData($editMov['dtEmi']) : date('d/m/Y') ?>" required>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label required">Vencimento</label>
                            <input type="text" name="dtVcto" class="form-control datepicker" value="<?= $editMov ? formatData($editMov['dtVcto']) : date('d/m/Y') ?>" required>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">Apresentação</label>
                            <input type="text" name="dtApr" class="form-control datepicker" value="<?= $editMov && $editMov['dtApr'] && $editMov['dtApr'] !== '0000-00-00' ? formatData($editMov['dtApr']) : '' ?>">
                        </div>
                    </div>

                    <!-- Linha 4: Documento + Tipo + Valor -->
                    <div class="row mb-2">
                        <div class="col-md-3">
                            <label class="form-label required">Documento</label>
                            <input type="text" name="documento" class="form-control" value="<?= htmlspecialchars($editMov['documento'] ?? '') ?>" required>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label required">Tipo</label>
                            <select name="tipo" id="tipo" class="form-select" required>
                                <option value="E" <?= ($editMov['_tipo'] ?? 'E') === 'E' ? 'selected' : '' ?>>E - Entrada</option>
                                <option value="S" <?= ($editMov['_tipo'] ?? '') === 'S' ? 'selected' : '' ?>>S - Saída</option>
                                <option value="N" <?= ($editMov['_tipo'] ?? '') === 'N' ? 'selected' : '' ?>>N - Nulo (Saldo Inicial)</option>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label required">Valor</label>
                            <input type="text" name="Valor" class="form-control money" value="<?= isset($editMov['Valor']) ? number_format($editMov['Valor'], 2, ',', '.') : '' ?>" required>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">Saldo Anterior</label>
                            <input type="text" id="tfSaldoAnterior" class="form-control bg-light" value="0,00" readonly>
                        </div>
                    </div>

                    <!-- Linha 5: Descrição -->
                    <div class="row mb-2">
                        <div class="col-md-12">
                            <label class="form-label required">Descrição</label>
                            <input type="text" name="Descr" class="form-control" value="<?= htmlspecialchars($editMov['Descr'] ?? '') ?>" required>
                        </div>
                    </div>

                    <!-- Linha 6: Classificação -->
                    <div class="row mb-2">
                        <div class="col-md-5">
                            <label class="form-label required">Classificação (Plano de Contas)</label>
                            <select name="classif" id="classif" class="form-select select2" required>
                                <option value="">Selecione...</option>
                                <?php foreach ($planos as $p): ?>
                                <option value="<?= htmlspecialchars($p['cod_Geral']) ?>" <?= ($editMov['classif'] ?? '') === $p['cod_Geral'] ? 'selected' : '' ?>>
                                    <?= htmlspecialchars($p['cod_Geral'] . ' - ' . $p['nome_C']) ?>
                                </option>
                                <?php endforeach; ?>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">Classif. Selecionada</label>
                            <input type="text" id="tfClass" class="form-control bg-light" value="<?= htmlspecialchars($editMov['classif'] ?? '') ?>" readonly>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Status</label>
                            <select name="statusMov" class="form-select">
                                <option value="" <?= ($editMov['statusMov'] ?? '') === '' ? 'selected' : '' ?>>(em branco)</option>
                                <option value="AB" <?= ($editMov['statusMov'] ?? '') === 'AB' ? 'selected' : '' ?>>AB - Aberto</option>
                                <option value="PG" <?= ($editMov['statusMov'] ?? '') === 'PG' ? 'selected' : '' ?>>PG - Pago</option>
                                <option value="RC" <?= ($editMov['statusMov'] ?? '') === 'RC' ? 'selected' : '' ?>>RC - Recebido</option>
                                <option value="PP" <?= ($editMov['statusMov'] ?? '') === 'PP' ? 'selected' : '' ?>>PP - Pago Parcial</option>
                                <option value="SI" <?= ($editMov['statusMov'] ?? '') === 'SI' ? 'selected' : '' ?>>SI - Saldo Inicial</option>
                                <option value="TO" <?= ($editMov['statusMov'] ?? '') === 'TO' ? 'selected' : '' ?>>TO - Transf. Origem</option>
                                <option value="TD" <?= ($editMov['statusMov'] ?? '') === 'TD' ? 'selected' : '' ?>>TD - Transf. Destino</option>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Previsão</label>
                            <select name="Prev" class="form-select">
                                <option value="F" <?= ($editMov['Prev'] ?? '') === 'F' ? 'selected' : '' ?>>F - Falso (Previsão)</option>
                                <option value="V" <?= ($editMov['Prev'] ?? 'V') === 'V' ? 'selected' : '' ?>>V - Verdadeiro (Confirmado)</option>
                            </select>
                        </div>
                    </div>

                    <!-- Banco Destino (oculto, só exibe quando necessário) -->
                    <div id="bancoDestinoFields" class="row mb-2 d-none">
                        <div class="col-md-12">
                            <hr>
                            <h6><i class="bi bi-bank"></i> Banco Destino (Transferência)</h6>
                        </div>
                        <div class="col-md-5">
                            <label class="form-label">Banco Destino</label>
                            <select name="recurso_destino" id="recurso_destino" class="form-select select2">
                                <option value="">Selecione...</option>
                                <?php foreach ($recursos as $r): ?>
                                <option value="<?= htmlspecialchars($r['codigo']) ?>" data-fk_gpprinc="<?= htmlspecialchars($r['fk_gpprinc']) ?>">
                                    <?= htmlspecialchars($r['codigo'] . ' - ' . $r['nomebco']) ?>
                                </option>
                                <?php endforeach; ?>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Nr. Banco Destino</label>
                            <input type="text" id="tfNrBancoDestino" class="form-control bg-light" readonly>
                        </div>
                        <div class="col-md-5">
                            <label class="form-label">Banco Destino (classif.)</label>
                            <input type="text" id="tfBancoDestino" class="form-control bg-light" readonly>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-primary">Salvar</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Receber -->
<div class="modal fade" id="modalReceber" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form method="POST" action="?action=receber&amp;filtro=<?= $filtro ?><?= $filtroRecurso ? '&amp;filtro_recurso='.$filtroRecurso : '' ?><?= ($tipo !== 'T') ? '&amp;tipo='.$tipo : '' ?><?= $ordem ? '&amp;ordem='.$ordem : '' ?><?= $pagina > 1 ? '&amp;pagina='.$pagina : '' ?>">
                <div class="modal-header bg-success text-white">
                    <h5 class="modal-title"><i class="bi bi-cash-coin"></i> Receber Lançamento</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="idMov" id="recIdMov">
                    <div class="row mb-2">
                        <div class="col-md-4">
                            <label class="form-label">Registro</label>
                            <input type="text" class="form-control bg-light" id="recRegistro" readonly>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Recurso Origem</label>
                            <input type="text" class="form-control bg-light" id="recRecurso" readonly>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Favorecido</label>
                            <input type="text" class="form-control bg-light" id="recFavorecido" readonly>
                        </div>
                    </div>
                    <div class="row mb-2">
                        <div class="col-md-4">
                            <label class="form-label">Documento</label>
                            <input type="text" class="form-control bg-light" id="recDocumento" readonly>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Valor</label>
                            <input type="text" class="form-control bg-light" id="recValor" readonly>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Vencimento</label>
                            <input type="text" class="form-control bg-light" id="recVencimento" readonly>
                        </div>
                    </div>
                    <div class="row mb-2">
                        <div class="col-md-12">
                            <label class="form-label">Descrição</label>
                            <input type="text" class="form-control bg-light" id="recDescricao" readonly>
                        </div>
                    </div>
                    <hr>
                    <div class="row mb-2">
                        <div class="col-md-5">
                            <label class="form-label required">Recurso Destino (onde o valor entrará)</label>
                            <select name="recurso_destino" id="recRecursoDestino" class="form-select" required>
                                <option value="">Selecione...</option>
                                <?php foreach ($recursos as $r): ?>
                                <option value="<?= htmlspecialchars($r['codigo']) ?>"><?= htmlspecialchars($r['codigo'] . ' - ' . $r['nomebco']) ?></option>
                                <?php endforeach; ?>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label required">Data Recebimento</label>
                            <input type="text" name="dtPagto" class="form-control datepicker" id="recDtPagto" value="<?= date('d/m/Y') ?>" required>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Desconto</label>
                            <input type="text" name="desconto" class="form-control money" id="recDesconto" value="0,00">
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Juros</label>
                            <input type="text" name="juros" class="form-control money" id="recJuros" value="0,00">
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-success" onclick="return confirm('Confirmar recebimento?')"><i class="bi bi-check-lg"></i> Confirmar Recebimento</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modal Pagar -->
<div class="modal fade" id="modalPagar" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form method="POST" action="?action=pagar&amp;filtro=<?= $filtro ?><?= $filtroRecurso ? '&amp;filtro_recurso='.$filtroRecurso : '' ?><?= ($tipo !== 'T') ? '&amp;tipo='.$tipo : '' ?><?= $ordem ? '&amp;ordem='.$ordem : '' ?><?= $pagina > 1 ? '&amp;pagina='.$pagina : '' ?>">
                <div class="modal-header bg-warning text-dark">
                    <h5 class="modal-title"><i class="bi bi-credit-card"></i> Pagar Lançamento</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="idMov" id="pagIdMov">
                    <div class="row mb-2">
                        <div class="col-md-4">
                            <label class="form-label">Registro</label>
                            <input type="text" class="form-control bg-light" id="pagRegistro" readonly>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Recurso Origem</label>
                            <input type="text" class="form-control bg-light" id="pagRecurso" readonly>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Favorecido</label>
                            <input type="text" class="form-control bg-light" id="pagFavorecido" readonly>
                        </div>
                    </div>
                    <div class="row mb-2">
                        <div class="col-md-4">
                            <label class="form-label">Documento</label>
                            <input type="text" class="form-control bg-light" id="pagDocumento" readonly>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Valor</label>
                            <input type="text" class="form-control bg-light" id="pagValor" readonly>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label">Vencimento</label>
                            <input type="text" class="form-control bg-light" id="pagVencimento" readonly>
                        </div>
                    </div>
                    <div class="row mb-2">
                        <div class="col-md-12">
                            <label class="form-label">Descrição</label>
                            <input type="text" class="form-control bg-light" id="pagDescricao" readonly>
                        </div>
                    </div>
                    <hr>
                    <div class="row mb-2">
                        <div class="col-md-5">
                            <label class="form-label required">Recurso Origem (de onde o valor sairá)</label>
                            <select name="recurso_origem" id="pagRecursoOrigem" class="form-select" required>
                                <option value="">Selecione...</option>
                                <?php foreach ($recursos as $r): ?>
                                <option value="<?= htmlspecialchars($r['codigo']) ?>"><?= htmlspecialchars($r['codigo'] . ' - ' . $r['nomebco']) ?></option>
                                <?php endforeach; ?>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label required">Data Pagamento</label>
                            <input type="text" name="dtPagto" class="form-control datepicker" id="pagDtPagto" value="<?= date('d/m/Y') ?>" required>
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Desconto</label>
                            <input type="text" name="desconto" class="form-control money" id="pagDesconto" value="0,00">
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">Juros</label>
                            <input type="text" name="juros" class="form-control money" id="pagJuros" value="0,00">
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                    <button type="submit" class="btn btn-warning" onclick="return confirm('Confirmar pagamento?')"><i class="bi bi-check-lg"></i> Confirmar Pagamento</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
var recursosData = <?= json_encode($recursos, JSON_UNESCAPED_UNICODE) ?>;
var cliforsData = <?= json_encode($clifors, JSON_UNESCAPED_UNICODE) ?>;
var planosData = <?= json_encode($planos, JSON_UNESCAPED_UNICODE) ?>;
</script>

<script>
function formatDateBR(dateStr) {
    if (!dateStr) return '';
    var parts = dateStr.split('-');
    return parts[2] + '/' + parts[1] + '/' + parts[0];
}
function formatMoneyBR(val) {
    return 'R$ ' + Math.abs(val).toFixed(2).replace('.', ',');
}
function abrirModalReceber(el) {
    var d = JSON.parse(el.getAttribute('data-mov'));
    document.getElementById('recIdMov').value = d.id;
    document.getElementById('recRegistro').value = d.id;
    document.getElementById('recRecurso').value = d.recurso;
    document.getElementById('recFavorecido').value = d.favorecido;
    document.getElementById('recDocumento').value = d.documento;
    document.getElementById('recValor').value = formatMoneyBR(d.valor);
    document.getElementById('recVencimento').value = formatDateBR(d.dtVcto);
    document.getElementById('recDescricao').value = d.descricao;
    document.getElementById('recRecursoDestino').value = '';
    document.getElementById('recDesconto').value = '0,00';
    document.getElementById('recJuros').value = '0,00';
    new bootstrap.Modal(document.getElementById('modalReceber')).show();
}
function abrirModalPagar(el) {
    var d = JSON.parse(el.getAttribute('data-mov'));
    document.getElementById('pagIdMov').value = d.id;
    document.getElementById('pagRegistro').value = d.id;
    document.getElementById('pagRecurso').value = d.recurso;
    document.getElementById('pagFavorecido').value = d.favorecido;
    document.getElementById('pagDocumento').value = d.documento;
    document.getElementById('pagValor').value = formatMoneyBR(d.valor);
    document.getElementById('pagVencimento').value = formatDateBR(d.dtVcto);
    document.getElementById('pagDescricao').value = d.descricao;
    document.getElementById('pagRecursoOrigem').value = '';
    document.getElementById('pagDesconto').value = '0,00';
    document.getElementById('pagJuros').value = '0,00';
    new bootstrap.Modal(document.getElementById('modalPagar')).show();
}
<?php if ($editMov): ?>
(function() {
    var check = setInterval(function() {
        if (typeof bootstrap !== 'undefined' && document.getElementById('modalMov')) {
            new bootstrap.Modal(document.getElementById('modalMov')).show();
            clearInterval(check);
        }
    }, 50);
})();
<?php endif; ?>
</script>

<?php include '../includes/footer.php'; ?>
