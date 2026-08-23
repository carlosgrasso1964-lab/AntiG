<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$db = Database::getInstance()->getConnection();

// ====== PROCESSAMENTO ANTES DE QUALQUER OUTPUT ======
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $db->beginTransaction();

        $tipo = strtoupper(trim($_POST['tipo'])); // E = Entrada, S = Saída
        $valorCompra = str_replace(['.', ','], ['', '.'], $_POST['valor_compra']);
        $numParcelas = (int)$_POST['num_parcelas'];
        $valorParcela = str_replace(['.', ','], ['', '.'], $_POST['valor_parcela']);
        $recursoCartao = $_POST['recurso_cartao'];
        $clifor = $_POST['clifor'];
        $documento = $_POST['documento'] ?? 'CART';
        $emissao = dataParaSQL($_POST['emissao']);
        $vcto = dataParaSQL($_POST['vcto']);
        $descricao = mb_strtoupper(trim($_POST['descricao']));
        $status = strtoupper(trim($_POST['status']));
        $prev = strtoupper(trim($_POST['prev']));
        $intervalo = $_POST['intervalo'];
        $corrigeEmi = isset($_POST['corrige_emi']) && $_POST['corrige_emi'] === 'S';

        // Buscar vCliFor do banco (sem depender de JS) — idêntico ao lancamentos.php
        $vCliFor = '';
        if (!empty($clifor)) {
            $st = $db->prepare("SELECT fkCliForGp FROM tbclifor WHERE codCliFor=?");
            $st->execute([$clifor]);
            $r = $st->fetch();
            if ($r && $r['fkCliForGp']) $vCliFor = $r['fkCliForGp'];
        }

        // Classificação selecionada
        $classif = $_POST['vclass'] ?? '';
        if (empty($classif) && !empty($_POST['classif'])) {
            $classif = $_POST['classif'];
        }

        // Valores para avançar datas (idêntico ao Java VParcelCart)
        $fieldMap = [
            'mensal'        => ['field' => 'month',  'amount' => 1],
            'trimestral'    => ['field' => 'month',  'amount' => 3],
            'bimestral'     => ['field' => 'month',  'amount' => 2],
            'quadrimestral' => ['field' => 'month',  'amount' => 4],
            'anual'         => ['field' => 'year',   'amount' => 1],
            'semanal'       => ['field' => 'week',   'amount' => 1],
            'diario'        => ['field' => 'day',    'amount' => 1],
        ];

        // Cálculo da diferença (idêntico ao Java VParcelCart)
        $princ = $valorCompra;
        $liqParc = $valorParcela;
        $difer = 0;
        $parcelaCorrecao = 1;

        if ($valorCompra != 0) {
            $princ = $valorCompra;
            $meValor = $princ / $numParcelas;
            $valor = floor($meValor * 100) / 100;
            $liqParc = $valor;
            $vrParcD = $princ - ($valor * $numParcelas);
            $difer = round($vrParcD, 2);

            if ($difer != 0) {
                $parcelaCorrecao = (int)($_POST['parcela_correcao'] ?? 1);
                if ($parcelaCorrecao < 1 || $parcelaCorrecao > $numParcelas) {
                    $parcelaCorrecao = 1;
                }
            }
        }

        $calEmi = new DateTime($emissao);
        $calVcto = new DateTime($vcto);

        // Loop de parcelas (idêntico ao Java)
        for ($i = 0; $i < $numParcelas; $i++) {
            $parcelaNum = $i + 1;

            // Avançar datas (a partir da 2ª parcela — preservando fim do mês como Java)
            if ($i > 0) {
                $interval = $fieldMap[$intervalo] ?? $fieldMap['mensal'];
                avancarDataPreservandoFimMes($calVcto, $interval['field'], $interval['amount']);
                if ($corrigeEmi) {
                    avancarDataPreservandoFimMes($calEmi, $interval['field'], $interval['amount']);
                }
            }

            $sqlEmi = $calEmi->format('Y-m-d');
            $sqlVcto = $calVcto->format('Y-m-d');

            // Valor final com tratamento da diferença
            $valorFinal = $liqParc;
            if ($parcelaNum == $parcelaCorrecao) {
                $valorFinal = $liqParc + $difer;
            }
            $valorFinal = abs($valorFinal);

            $descParcela = 'parc.' . $parcelaNum . '/' . $numParcelas . ' - ' . $descricao;

            // === INSERT 1: Recurso (tipo "1") ===
            $stmt1 = $db->prepare("INSERT INTO tbmovimento (recurso, vrecurso, clifor, vCliFor, dtlancto, dtEmi, dtVcto, documento, classif, Descr, Valor, dtApr, statusMov, Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            $stmt1->execute([
                '0079', '2.001.003',
                $clifor, $vCliFor,
                $sqlEmi, $sqlEmi, $sqlVcto,
                $documento, $classif,
                $descParcela, -$valorFinal,
                $sqlVcto, 'PG', $prev
            ]);

            // === INSERT 2: Pagamento (tipo "2") ===
            $stmt2 = $db->prepare("INSERT INTO tbmovimento (recurso, vrecurso, clifor, vCliFor, dtlancto, dtEmi, dtVcto, documento, classif, Descr, Valor, dtApr, statusMov, Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            $stmt2->execute([
                '0079', '2.001.003',
                $clifor, $vCliFor,
                $sqlEmi, $sqlEmi, $sqlVcto,
                $documento, '9.001.003',
                $descParcela, $valorFinal,
                $sqlVcto, 'PG', $prev
            ]);

            // === INSERT 3: Cartão (tipo "3") ===
            $stmt3 = $db->prepare("INSERT INTO tbmovimento (recurso, vrecurso, clifor, vCliFor, dtlancto, dtEmi, dtVcto, documento, classif, Descr, Valor, dtApr, statusMov, Prev) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            $stmt3->execute([
                $recursoCartao, '2.001.003',
                $clifor, $vCliFor,
                $sqlEmi, $sqlEmi, $sqlVcto,
                $documento, '9.001.003',
                $descParcela, -$valorFinal,
                null, '', $prev
            ]);
        }

        $db->commit();
        flashMessage('success', "Parcelamento de cartões gerado com sucesso! ($numParcelas parcelas × 3 registros)");
        redirect('parcelamento_cartoes.php');
    } catch (Exception $e) {
        $db->rollBack();
        flashMessage('danger', 'Erro: ' . $e->getMessage());
    }
}

// ====== RENDERIZAÇÃO ======
$title = 'Parcelamento - Cartões';
include '../includes/header.php';

// Buscar cartões de crédito (fk_gpprinc = '2.001.003')
$cartoes = $db->query("SELECT codigo, nomebco FROM tbrecursos WHERE fk_gpprinc = '2.001.003' AND status='A' ORDER BY nomebco")->fetchAll();
$clifors = $db->query("SELECT codCliFor, nomeCliFor, apelidoCliFor, fkCliForGp FROM tbclifor ORDER BY apelidoCliFor")->fetchAll();
$planos = $db->query("SELECT cod_Geral, nome_C FROM gpprincipal WHERE nome_C <> '-' ORDER BY cod_Geral")->fetchAll();
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-credit-card"></i> P a r c e l a m e n t o &nbsp; - &nbsp; C a r t õ e s</h4>
</div>

<div class="row">
    <div class="col-md-5">
        <div class="card border-primary">
            <div class="card-header bg-primary text-white"><i class="bi bi-credit-card-2-front"></i> Gerar Parcelamento de Cartão</div>
            <div class="card-body">
                <form method="POST" id="formParcelCartao">
                    <div class="mb-3">
                        <label class="form-label required">Recurso (Cartão de Crédito)</label>
                        <select name="recurso_cartao" id="recurso_cartao" class="form-select select2" required>
                            <option value="">Selecione o cartão</option>
                            <?php foreach ($cartoes as $r): ?>
                            <option value="<?= htmlspecialchars($r['codigo']) ?>">
                                <?= htmlspecialchars($r['nomebco'] . '-' . $r['codigo'] . '-2.001.003') ?>
                            </option>
                            <?php endforeach; ?>
                        </select>
                        <?php if (empty($cartoes)): ?>
                        <div class="text-danger small mt-1">
                            <i class="bi bi-exclamation-triangle"></i> Nenhum cartão encontrado. Cadastre recursos com classificação "2.001.003".
                        </div>
                        <?php endif; ?>
                    </div>
                    <div class="row">
                        <div class="col-md-8 mb-3">
                            <label class="form-label required">Tipo (Entrada / Saída)</label>
                            <select name="tipo" id="tipo" class="form-select" required>
                                <option value="">Selecione ("E"ntrada - "S"aída)</option>
                                <option value="E">E - Entrada</option>
                                <option value="S">S - Saída</option>
                            </select>
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label required">Valor da Compra</label>
                            <input type="text" name="valor_compra" id="valor_compra" class="form-control money" required>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label required">Qtde. Parcelas</label>
                            <input type="number" name="num_parcelas" id="num_parcelas" class="form-control" min="1" max="120" required>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label required">Valor da Parcela</label>
                            <input type="text" name="valor_parcela" id="valor_parcela" class="form-control money" required>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label required">Favorecido</label>
                        <select name="clifor" id="clifor_select" class="form-select select2" required>
                            <option value="">Selecione o Favorecido</option>
                            <?php foreach ($clifors as $c): ?>
                            <option value="<?= htmlspecialchars($c['codCliFor']) ?>"
                                data-vclifor="<?= htmlspecialchars($c['fkCliForGp'] ?? '') ?>">
                                <?= htmlspecialchars($c['apelidoCliFor'] . '-' . $c['codCliFor'] . '-' . $c['fkCliForGp']) ?>
                            </option>
                            <?php endforeach; ?>
                        </select>
                        <input type="hidden" name="vclifor" id="vclifor" value="">
                    </div>
                    <div class="mb-3">
                        <label class="form-label required">Classificação</label>
                        <select name="classif" id="classif_select" class="form-select select2" required>
                            <option value="">Selecione a Classificação</option>
                            <?php foreach ($planos as $p): ?>
                            <option value="<?= htmlspecialchars($p['cod_Geral']) ?>">
                                <?= htmlspecialchars($p['nome_C'] . '-' . $p['cod_Geral']) ?>
                            </option>
                            <?php endforeach; ?>
                        </select>
                        <input type="hidden" name="vclass" id="vclass" value="">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Documento</label>
                        <input type="text" name="documento" class="form-control" value="CART">
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label required">Emissão</label>
                            <input type="text" name="emissao" class="form-control datepicker" value="<?= date('d/m/Y') ?>" required>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label required">1º Vencimento</label>
                            <input type="text" name="vcto" class="form-control datepicker" value="<?= date('d/m/Y') ?>" required>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Status</label>
                            <input type="text" name="status" class="form-control" value="" maxlength="2" style="width:60px;">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Previsto</label>
                            <input type="text" name="prev" class="form-control" value="V" maxlength="1" style="width:60px;">
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label required">Descrição</label>
                        <input type="text" name="descricao" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label required">Intervalo</label>
                        <div class="row g-2">
                            <div class="col-md-4">
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="intervalo" id="int_mensal" value="mensal" checked>
                                    <label class="form-check-label" for="int_mensal">Mensal</label>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="intervalo" id="int_bimestral" value="bimestral">
                                    <label class="form-check-label" for="int_bimestral">Bimestral</label>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="intervalo" id="int_trimestral" value="trimestral">
                                    <label class="form-check-label" for="int_trimestral">Trimestral</label>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="intervalo" id="int_quadrimestral" value="quadrimestral">
                                    <label class="form-check-label" for="int_quadrimestral">Quadrimestral</label>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="intervalo" id="int_anual" value="anual">
                                    <label class="form-check-label" for="int_anual">Anual</label>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="intervalo" id="int_semanal" value="semanal">
                                    <label class="form-check-label" for="int_semanal">Semanal</label>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" name="intervalo" id="int_diario" value="diario">
                                    <label class="form-check-label" for="int_diario">Diário</label>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Parcela para Correção da Diferença</label>
                        <input type="number" name="parcela_correcao" id="parcela_correcao" class="form-control" min="1" max="120" value="1">
                        <small class="text-muted">Se houver diferença de arredondamento, será aplicada nesta parcela.</small>
                    </div>
                    <div class="mb-3">
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" name="corrige_emi" id="corrige_emi" value="S">
                            <label class="form-check-label" for="corrige_emi">
                                <i class="bi bi-calendar-check"></i> Corrige Data da Emissão?
                            </label>
                        </div>
                        <small class="text-muted">Se marcado, a data de emissão também avança junto com o vencimento.</small>
                    </div>
                    <div class="alert alert-info small mb-3">
                        <i class="bi bi-info-circle"></i>
                        <strong>Lançamentos gerados por parcela:</strong>
                        <ul class="mb-0 mt-1">
                            <li><strong>Recurso (0079):</strong> Saída negativa na conta interna</li>
                            <li><strong>Pagamento (0079):</strong> Entrada positiva (classificação 9.001.003)</li>
                            <li><strong>Cartão:</strong> Saída negativa no cartão selecionado</li>
                        </ul>
                    </div>
                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-primary"><i class="bi bi-gear"></i> Gerar</button>
                        <button type="button" class="btn btn-secondary" onclick="window.print()"><i class="bi bi-printer"></i> Imprimir</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <div class="col-md-7">
        <div class="card">
            <div class="card-header"><i class="bi bi-list-ul"></i> Últimos Parcelamentos de Cartão</div>
            <div class="card-body">
                <?php
                $stmt = $db->query("
                    SELECT m.*, r.nomebco, c.nomeCliFor
                    FROM tbmovimento m
                    LEFT JOIN tbrecursos r ON m.recurso = r.codigo
                    LEFT JOIN tbclifor c ON m.clifor = c.codCliFor
                    WHERE m.Descr LIKE 'parc.%'
                    ORDER BY m.idMov DESC LIMIT 50
                ");
                $parcelas = $stmt->fetchAll();
                ?>
                <div class="table-responsive">
                    <table class="table table-sm datatable">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Recurso</th>
                                <th>Doc</th>
                                <th>Descrição</th>
                                <th>Cli/For</th>
                                <th>Emissão</th>
                                <th>Vencimento</th>
                                <th class="text-end">Valor</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($parcelas as $p): ?>
                            <tr>
                                <td><?= $p['idMov'] ?></td>
                                <td><?= htmlspecialchars($p['nomebco'] ?? $p['recurso']) ?></td>
                                <td><?= htmlspecialchars($p['documento']) ?></td>
                                <td><?= htmlspecialchars(mb_substr($p['Descr'], 0, 45)) ?></td>
                                <td><?= htmlspecialchars($p['nomeCliFor'] ?? $p['clifor']) ?></td>
                                <td><?= formatData($p['dtEmi']) ?></td>
                                <td><?= formatData($p['dtVcto']) ?></td>
                                <td class="text-end <?= $p['Valor'] < 0 ? 'valor-negativo' : 'valor-positivo' ?>">
                                    <?= formatMoeda($p['Valor']) ?>
                                </td>
                                <td><?= statusBadge($p['statusMov']) ?></td>
                            </tr>
                            <?php endforeach; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    // Auto-preencher vclifor ao selecionar favorecido
    const cliforSelect = document.getElementById('clifor_select');
    const vcliforInput = document.getElementById('vclifor');
    if (cliforSelect) {
        cliforSelect.addEventListener('change', function() {
            const selected = this.options[this.selectedIndex];
            vcliforInput.value = selected.getAttribute('data-vclifor') || '';
        });
    }

    // Auto-preencher vclass ao selecionar classificação
    const classifSelect = document.getElementById('classif_select');
    const vclassInput = document.getElementById('vclass');
    if (classifSelect) {
        classifSelect.addEventListener('change', function() {
            vclassInput.value = this.value || '';
        });
    }

    // Calcular valor da parcela automaticamente
    const valorCompra = document.getElementById('valor_compra');
    const numParcelas = document.getElementById('num_parcelas');
    const valorParcela = document.getElementById('valor_parcela');

    function calcularParcela() {
        const vc = parseFloat(valorCompra.value.replace(/\./g, '').replace(',', '.')) || 0;
        const np = parseInt(numParcelas.value) || 0;
        if (vc > 0 && np > 0) {
            const vp = vc / np;
            valorParcela.value = vp.toFixed(2).replace('.', ',');
        }
    }

    if (valorCompra) valorCompra.addEventListener('blur', calcularParcela);
    if (numParcelas) numParcelas.addEventListener('blur', calcularParcela);
});
</script>

<?php include '../includes/footer.php'; ?>
