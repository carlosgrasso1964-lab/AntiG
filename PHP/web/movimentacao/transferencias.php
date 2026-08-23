<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$db = Database::getInstance()->getConnection();

// ====== PROCESSAMENTO POST ======
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $db->beginTransaction();

        $data = dataParaSQL($_POST['data']);
        $doc = trim($_POST['documento']);
        $valor = str_replace(['.', ','], ['', '.'], $_POST['valor']);
        $recursoOrigem = $_POST['recurso_origem'];
        $recursoDestino = $_POST['recurso_destino'];

        // Garante que o clifor correspondente a cada recurso exista (evita erro de FK 1452)
        // Padrão do sistema: clifor = código do recurso na transferência
        $garantirClifor = function ($codigoRecurso, $nomeRecurso, $fk_gpprinc) use ($db) {
            $chk = $db->prepare("SELECT COUNT(*) FROM tbclifor WHERE codCliFor=?");
            $chk->execute([$codigoRecurso]);
            if ($chk->fetchColumn() == 0) {
                $tipo = (strpos($fk_gpprinc, '2.001.003') === 0) ? 'CAR'
                      : ((strpos($fk_gpprinc, '1.001.001') === 0) ? 'BCO' : 'FOR');
                $ins = $db->prepare("INSERT INTO tbclifor (codCliFor, Tipo, nomeCliFor, apelidoCliFor, contatoCliFor, obs, fkCliForGp) VALUES (?,?,?,?,?,?,?)");
                $ins->execute([$codigoRecurso, $tipo, $nomeRecurso, $nomeRecurso, '-', '-', $fk_gpprinc]);
            }
        };

        // Busca o fk_gpprinc de cada recurso (classificação automática, igual ao Java)
        $stO = $db->prepare("SELECT fk_gpprinc, nomebco FROM tbrecursos WHERE codigo=?");
        $stO->execute([$recursoOrigem]);
        $rowO = $stO->fetch();
        $classifOrigem = $rowO ? $rowO['fk_gpprinc'] : null;
        $nomeOrigem = $rowO ? $rowO['nomebco'] : $recursoOrigem;

        $stD = $db->prepare("SELECT fk_gpprinc, nomebco FROM tbrecursos WHERE codigo=?");
        $stD->execute([$recursoDestino]);
        $rowD = $stD->fetch();
        $classifDestino = $rowD ? $rowD['fk_gpprinc'] : null;
        $nomeDestino = $rowD ? $rowD['nomebco'] : $recursoDestino;

        if (!$classifOrigem || !$classifDestino) {
            throw new Exception('Recurso origem ou destino não encontrado!');
        }

        $garantirClifor($recursoOrigem, $nomeOrigem, $classifOrigem);
        $garantirClifor($recursoDestino, $nomeDestino, $classifDestino);

        $sql = "INSERT INTO tbmovimento (recurso, vrecurso, clifor, vCliFor, dtlancto, dtEmi, dtVcto, documento, classif, Descr, Valor, dtApr, statusMov, Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        // Lançamento 1 — SAÍDA (Origem) — classif = 9.001.001, statusMov = TO
        $stmt = $db->prepare($sql);
        $stmt->execute([
            $recursoOrigem,       // recurso = código recurso origem
            $classifOrigem,       // vrecurso = fk_gpprinc origem
            $recursoOrigem,       // clifor = código recurso origem
            $classifOrigem,       // vCliFor = fk_gpprinc origem
            $data,                // dtlancto
            $data,                // dtEmi
            $data,                // dtVcto
            $doc,                 // documento (informado pelo usuário)
            '9.001.001',          // classif fixo = 9.001.001 (Transferência Origem)
            'TRANSFERIDO PARA: ' . $recursoDestino,  // Descr automático
            -$valor,              // Valor negativo (saída)
            $data,                // dtApr = data da transferência
            'TO',                 // statusMov = TO (Transferência Origem)
            'V',                  // Prev = V (Confirmado)
        ]);

        // Lançamento 2 — ENTRADA (Destino) — classif = 9.001.002, statusMov = TD
        $stmt2 = $db->prepare($sql);
        $stmt2->execute([
            $recursoDestino,      // recurso = código recurso destino
            $classifDestino,      // vrecurso = fk_gpprinc destino
            $recursoDestino,      // clifor = código recurso destino
            $classifDestino,      // vCliFor = fk_gpprinc destino
            $data,                // dtlancto
            $data,                // dtEmi
            $data,                // dtVcto
            $doc,                 // documento
            '9.001.002',          // classif fixo = 9.001.002 (Transferência Destino)
            'TRANSFERIDO DE: ' . $recursoOrigem,      // Descr automático
            $valor,               // Valor positivo (entrada)
            $data,                // dtApr
            'TD',                 // statusMov = TD (Transferência Destino)
            'V',                  // Prev = V (Confirmado)
        ]);

        $db->commit();
        flashMessage('success', 'Transferência efetuada com sucesso!');
        redirect('transferencias.php');
    } catch (Exception $e) {
        $db->rollBack();
        flashMessage('danger', 'Erro: ' . $e->getMessage());
    }
}

// ====== RENDERIZAÇÃO ======
$title = 'Transferências';
include '../includes/header.php';

$recursos = $db->query("SELECT codigo, nomebco, fk_gpprinc FROM tbrecursos WHERE status='A' ORDER BY nomebco")->fetchAll();
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-arrow-left-right"></i> Transferências entre Recursos</h4>
</div>

<div class="row">
    <div class="col-md-6">
        <div class="card">
            <div class="card-header"><i class="bi bi-arrow-right-circle"></i> Nova Transferência</div>
            <div class="card-body">
                <form method="POST" id="formTransf">
                    <div class="mb-3">
                        <label class="form-label required">Data</label>
                        <input type="text" name="data" class="form-control datepicker" value="<?= date('d/m/Y') ?>" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label required">Documento</label>
                        <input type="text" name="documento" class="form-control" placeholder="Ex: DOC001" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label required">Origem (Recurso de Saída)</label>
                        <select name="recurso_origem" id="recurso_origem" class="form-select select2" required
                                onchange="atualizarClassifOrigem()">
                            <option value="">Selecione Origem...</option>
                            <?php foreach ($recursos as $r): ?>
                            <option value="<?= htmlspecialchars($r['codigo']) ?>"
                                    data-fk_gpprinc="<?= htmlspecialchars($r['fk_gpprinc']) ?>">
                                <?= htmlspecialchars($r['nomebco'] . ' - ' . $r['codigo'] . ' - ' . $r['fk_gpprinc']) ?>
                            </option>
                            <?php endforeach; ?>
                        </select>
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-4">
                            <label class="form-label">Nr. Recurso Orig.</label>
                            <input type="text" id="tfNrOrigem" class="form-control bg-light" readonly>
                        </div>
                        <div class="col-md-8">
                            <label class="form-label">Classif. Origem</label>
                            <input type="text" id="tfClassOrigem" class="form-control bg-light" readonly>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label required">Destino (Recurso de Entrada)</label>
                        <select name="recurso_destino" id="recurso_destino" class="form-select select2" required
                                onchange="atualizarClassifDestino()">
                            <option value="">Selecione Destino...</option>
                            <?php foreach ($recursos as $r): ?>
                            <option value="<?= htmlspecialchars($r['codigo']) ?>"
                                    data-fk_gpprinc="<?= htmlspecialchars($r['fk_gpprinc']) ?>">
                                <?= htmlspecialchars($r['nomebco'] . ' - ' . $r['codigo'] . ' - ' . $r['fk_gpprinc']) ?>
                            </option>
                            <?php endforeach; ?>
                        </select>
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-4">
                            <label class="form-label">Nr. Recurso Dest.</label>
                            <input type="text" id="tfNrDestino" class="form-control bg-light" readonly>
                        </div>
                        <div class="col-md-8">
                            <label class="form-label">Classif. Destino</label>
                            <input type="text" id="tfClassDestino" class="form-control bg-light" readonly>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label required">Valor</label>
                        <input type="text" name="valor" class="form-control money" required>
                    </div>
                    <button type="submit" class="btn btn-primary" onclick="return confirm('Confirmar transferência?')">
                        <i class="bi bi-arrow-left-right"></i> Transferir
                    </button>
                </form>
            </div>
        </div>
    </div>
    <div class="col-md-6">
        <div class="card">
            <div class="card-header"><i class="bi bi-clock-history"></i> Últimas Transferências</div>
            <div class="card-body">
                <?php
                // Busca transferências: Origem (TO) e Destino (TD) — igual ao Java
                $stmt = $db->query("
                    SELECT m.*, r.nomebco
                    FROM tbmovimento m
                    LEFT JOIN tbrecursos r ON m.recurso = r.codigo
                    WHERE m.statusMov IN ('TO', 'TD')
                    ORDER BY m.idMov DESC
                    LIMIT 20
                ");
                $transfs = $stmt->fetchAll();
                ?>
                <?php if (empty($transfs)): ?>
                    <p class="text-muted mb-0">Nenhuma transferência encontrada.</p>
                <?php else: ?>
                <div class="table-responsive">
                    <table class="table table-sm table-hover">
                        <thead>
                            <tr>
                                <th>Data</th>
                                <th>Recurso</th>
                                <th>Tipo</th>
                                <th>Documento</th>
                                <th class="text-end">Valor</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($transfs as $t): ?>
                            <tr>
                                <td><?= formatData($t['dtEmi']) ?></td>
                                <td><?= htmlspecialchars($t['nomebco'] ?? $t['recurso']) ?></td>
                                <td>
                                    <?php if ($t['statusMov'] === 'TO'): ?>
                                        <span class="badge bg-danger">Saída</span>
                                    <?php else: ?>
                                        <span class="badge bg-success">Entrada</span>
                                    <?php endif; ?>
                                </td>
                                <td><?= htmlspecialchars($t['documento']) ?></td>
                                <td class="text-end <?= $t['Valor'] < 0 ? 'valor-negativo' : 'valor-positivo' ?>">
                                    <?= formatMoeda($t['Valor']) ?>
                                </td>
                            </tr>
                            <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>
                <?php endif; ?>
            </div>
        </div>
    </div>
</div>

<script>
// Preenche automaticamente a classificação da origem (igual ao Java)
function atualizarClassifOrigem() {
    var sel = document.getElementById('recurso_origem');
    var opt = sel.options[sel.selectedIndex];
    var cod = sel.value;
    var fk = opt.getAttribute('data-fk_gpprinc') || '';
    document.getElementById('tfNrOrigem').value = cod;
    document.getElementById('tfClassOrigem').value = fk;
}

// Preenche automaticamente a classificação do destino (igual ao Java)
function atualizarClassifDestino() {
    var sel = document.getElementById('recurso_destino');
    var opt = sel.options[sel.selectedIndex];
    var cod = sel.value;
    var fk = opt.getAttribute('data-fk_gpprinc') || '';
    document.getElementById('tfNrDestino').value = cod;
    document.getElementById('tfClassDestino').value = fk;
}
</script>

<?php include '../includes/footer.php'; ?>
