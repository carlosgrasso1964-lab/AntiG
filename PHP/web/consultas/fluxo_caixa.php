<?php
session_start();
ini_set('display_errors', 1);
error_reporting(E_ALL);
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$title = 'Fluxo de Caixa';
include '../includes/header.php';

$db = Database::getInstance()->getConnection();
$erro = '';

// Período (dataIni/dataFim), espelha VConsFlxCxDiario.java (linhas 67-69)
$dataIni = $_POST['data_ini'] ?? $_GET['data_ini'] ?? '';
$dataFim = $_POST['data_fim'] ?? $_GET['data_fim'] ?? '';

$saldoAnterior = 0;
$movimentos = [];
$isRealizado = null;
$tituloTipo = 'CONSULTA - FLUXO DE CAIXA';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!$dataIni || !$dataFim || strlen($dataIni) != 10 || strlen($dataFim) != 10) {
        $erro = 'Atenção! Favor preencher corretamente as datas (dd/mm/yyyy).';
    } else {
        try {
            $dataIniSQL = dataParaSQL($dataIni);
            $dataFimSQL = dataParaSQL($dataFim);

            // isRealizado = dataIni < hoje (ontem ou antes => Realizado/Fluxo; hoje em diante => Projeção/Orçamento)
            $hoje = date('Y-m-d');
            $isRealizado = ($dataIniSQL < $hoje);

            // === PegaSaldoAnterior (VConsFlxCxDiario.java linhas 585-622) ===
            // SEMPRE igual — usa dtlancto, vrecurso em ('1.001.001','1.001.002') E recurso NOT IN ('0079','0022')
            $stmtSa = $db->prepare("SELECT COALESCE(SUM(Valor), 0) AS saldo_anterior FROM tbmovimento
                WHERE dtlancto <= ?
                  AND vrecurso IN ('1.001.001', '1.001.002')
                  AND recurso NOT IN ('0079', '0022')");
            $stmtSa->execute([$dataIniSQL]);
            $saldoAnterior = (float)$stmtSa->fetchColumn();

            // === Consulta (VConsFlxCxDiario.java linhas 293-312) ===
            if ($isRealizado) {
                $tituloTipo = 'CONSULTA - FLUXO DE CAIXA';
                $sql = "SELECT * FROM tbmovimento
                    WHERE dtApr BETWEEN ? AND ?
                      AND recurso IN ('0079', '0022', '0080')
                      AND classif NOT IN ('9.001.000','9.001.001','9.001.002','9.001.003')
                    ORDER BY dtApr";
                $campoData = 'dtApr';
            } else {
                $tituloTipo = 'CONSULTA - PROJEÇÃO DE CAIXA';
                $sql = "SELECT * FROM tbmovimento
                    WHERE dtVcto BETWEEN ? AND ?
                      AND vrecurso IN ('1.002.001','2.001.002','2.001.003','2.001.004')
                      AND recurso <> '0080'
                      AND statusMov = ''
                    ORDER BY dtVcto, recurso, dtEmi";
                $campoData = 'dtVcto';
            }
            $stmt = $db->prepare($sql);
            $stmt->execute([$dataIniSQL, $dataFimSQL]);
            $movimentos = $stmt->fetchAll();
        } catch (Exception $e) {
            $erro = 'Erro na consulta: ' . $e->getMessage();
        }
    }
}

// Dia-da-semana (espelha getDayOfWeek do Java)
function fxDiaSemana($dataDDMMYYYY) {
    static $wd = ['Dom','Seg','Ter','Qua','Qui','Sex','Sab'];
    try {
        $d = DateTime::createFromFormat('d/m/Y', $dataDDMMYYYY);
        if (!$d) return '---';
        return $wd[(int)$d->format('w')];
    } catch (Throwable $e) { return '---'; }
}
?>

<h3 class="text-center" style="font-weight: bold; color: <?= $isRealizado === false ? '#555' : '#0d6efd' ?>;">
    <i class="bi bi-graph-up"></i> <?= $tituloTipo ?>
</h3>

<?php if ($_SERVER['REQUEST_METHOD'] === 'POST' && empty($erro)): ?>
<div class="d-flex justify-content-end gap-2 mb-2">
    <button type="button" class="btn btn-outline-primary btn-sm" id="btnGrafico" onclick="toggleGrafico()">
        <i class="bi bi-graph-up"></i> Gráfico de Linha
    </button>
    <button type="button" class="btn btn-outline-success btn-sm" id="btnExportar" onclick="exportarFluxoCaixa()">
        <i class="bi bi-download"></i> Exportar
    </button>
    <button type="button" class="btn btn-outline-secondary btn-sm" onclick="window.print()">
        <i class="bi bi-printer"></i> Imprimir
    </button>
</div>

<div class="card mb-3" id="cardGrafico" style="display:none;">
    <div class="card-header"><i class="bi bi-graph-up"></i> Evolução do Saldo no Período</div>
    <div class="card-body">
        <canvas id="chartFluxoCaixa" height="100"></canvas>
    </div>
</div>
<?php endif; ?>

<?php if ($erro): ?>
<div class="alert alert-danger"><?= htmlspecialchars($erro) ?></div>
<?php endif; ?>

<form method="POST" class="row g-3 mb-3">
    <div class="col-md-2 offset-md-3">
        <label class="form-label">Período de Vencimentos — de</label>
        <input type="text" name="data_ini" class="form-control datepicker"
               value="<?= htmlspecialchars($dataIni) ?>" placeholder="dd/mm/yyyy" required>
    </div>
    <div class="col-md-2">
        <label class="form-label">até</label>
        <input type="text" name="data_fim" class="form-control datepicker"
               value="<?= htmlspecialchars($dataFim) ?>" placeholder="dd/mm/yyyy" required>
    </div>
    <div class="col-md-2">
        <label class="form-label">Saldo Anterior</label>
        <input type="text" class="form-control" value="<?= formatMoeda($saldoAnterior) ?>" readonly>
    </div>
    <div class="col-md-1 d-flex align-items-end">
        <button type="submit" class="btn btn-primary w-100">Pesquisar</button>
    </div>
</form>

<?php if ($_SERVER['REQUEST_METHOD'] === 'POST' && empty($erro)): ?>
<div class="card">
    <div class="card-body">
        <div class="table-responsive">
            <table class="table datatable table-hover" id="tblFluxoCaixa">
                <thead>
                    <tr>
                        <th>Reg.</th>
                        <th>Recurso</th>
                        <th class="d-none">vRecurso</th>
                        <th>Favorecido</th>
                        <th class="d-none">vFavorecido</th>
                        <th class="d-none">Lancto.</th>
                        <th>Emissão</th>
                        <th><?= $isRealizado ? 'Apresent.' : 'Vencimento' ?></th>
                        <th>Semana</th>
                        <th>Documento</th>
                        <th class="d-none">Classif.</th>
                        <th>Descrição</th>
                        <th class="text-end">Valor</th>
                        <th class="d-none">dtApr</th>
                        <th class="d-none">Status</th>
                        <th>Prev.</th>
                        <th class="text-end">Saldo</th>
                    </tr>
                </thead>
                <tbody>
                <?php
                // Lógica do Java (linhas 372-424): saldo acumulado exibido apenas no
                // ÚLTIMO registro de cada dia. Aqui simplificamos para o último do dia.
                $saldoAcumulado = $saldoAnterior;
                $dataAnterior = '';
                $ultimoIdxPorDia = []; // data => índice da última row com aquela data
                $rowIdx = -1;
                // 1ª passada: descobrir último índice por dia
                foreach ($movimentos as $m) {
                    $dataBruta = $m[$campoData] ?? null;
                    $df = $dataBruta ? formatData($dataBruta) : '';
                    $rowIdx++;
                    $ultimoIdxPorDia[$df] = $rowIdx;
                }
                // 2ª passada: renderizar
                $rowIdx = -1;
                $graficoPontos = []; // [data => saldoAcumulado] no último registro de cada dia (igual GraficoDeLinha.java)
                foreach ($movimentos as $m):
                    $rowIdx++;
                    $dataBruta = $m[$campoData] ?? null;
                    $dataFormatada = $dataBruta ? formatData($dataBruta) : '';
                    $valor = (float)($m['Valor'] ?? 0);
                    $saldoAcumulado += $valor;
                    $ehUltimoDoDia = isset($ultimoIdxPorDia[$dataFormatada]) && $ultimoIdxPorDia[$dataFormatada] === $rowIdx;
                    if ($ehUltimoDoDia) {
                        $graficoPontos[] = ['data' => $dataFormatada, 'saldo' => round($saldoAcumulado, 2)];
                    }
                ?>
                    <tr>
                        <td><?= htmlspecialchars($m['idMov'] ?? '') ?></td>
                        <td><?= htmlspecialchars($m['recurso'] ?? '') ?></td>
                        <td class="d-none"><?= htmlspecialchars($m['vrecurso'] ?? '') ?></td>
                        <td><?= htmlspecialchars($m['clifor'] ?? '') ?></td>
                        <td class="d-none"><?= htmlspecialchars($m['vCliFor'] ?? '') ?></td>
                        <td class="d-none"><?= formatData($m['dtlancto'] ?? '') ?></td>
                        <td><?= formatData($m['dtEmi'] ?? '') ?></td>
                        <td><?= $dataFormatada ?></td>
                        <td class="text-center"><?= $dataFormatada ? fxDiaSemana($dataFormatada) : '' ?></td>
                        <td><?= htmlspecialchars($m['documento'] ?? '') ?></td>
                        <td class="d-none"><?= htmlspecialchars($m['classif'] ?? '') ?></td>
                        <td><?= htmlspecialchars(mb_substr($m['Descr'] ?? '', 0, 80)) ?></td>
                        <td class="text-end <?= $valor < 0 ? 'valor-negativo' : 'valor-positivo' ?>">
                            <?= formatMoeda($valor) ?>
                        </td>
                        <td class="d-none"><?= formatData($m['dtApr'] ?? '') ?></td>
                        <td class="d-none"><?= htmlspecialchars($m['statusMov'] ?? '') ?></td>
                        <td class="text-center"><?= htmlspecialchars($m['Prev'] ?? '') ?></td>
                        <td class="text-end <?= $ehUltimoDoDia ? ($saldoAcumulado >= 0 ? 'valor-positivo' : 'valor-negativo') : '' ?>">
                            <?= $ehUltimoDoDia ? formatMoeda($saldoAcumulado) : '&nbsp;' ?>
                        </td>
                    </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
        </div>
        <?php if (empty($movimentos)): ?>
            <div class="alert alert-info mb-0">Nenhum registro encontrado no período informado.</div>
        <?php endif; ?>
    </div>
</div>
<?php endif; ?>

<?php if ($_SERVER['REQUEST_METHOD'] === 'POST' && empty($erro)): ?>
<script>
// ---- Gráfico de Linha (espelha GraficoDeLinha.java: Evolução do Saldo no Período) ----
var graficoFluxoCaixa = null;
var dadosGrafico = <?= json_encode($graficoPontos ?? []) ?>;

function toggleGrafico() {
    var card = document.getElementById('cardGrafico');
    if (card.style.display === 'none') {
        if (!graficoFluxoCaixa && dadosGrafico.length) {
            graficoFluxoCaixa = new Chart(document.getElementById('chartFluxoCaixa'), {
                type: 'line',
                data: {
                    labels: dadosGrafico.map(function (p) { return p.data; }),
                    datasets: [{
                        label: 'Saldo',
                        data: dadosGrafico.map(function (p) { return p.saldo; }),
                        borderColor: '#0d6efd',
                        backgroundColor: 'rgba(13,110,253,0.1)',
                        pointBackgroundColor: dadosGrafico.map(function (p) { return p.saldo >= 0 ? '#198754' : '#dc3545'; }),
                        borderWidth: 2,
                        fill: true,
                        tension: 0.1
                    }]
                },
                options: {
                    responsive: true,
                    scales: {
                        x: { ticks: { maxRotation: 45, minRotation: 45 } },
                        y: {
                            beginAtZero: true,
                            ticks: { callback: function (v) { return 'R$ ' + v.toFixed(2).replace('.', ','); } }
                        }
                    },
                    plugins: { legend: { display: true } }
                }
            });
        }
        card.style.display = '';
        document.getElementById('btnGrafico').innerHTML = '<i class="bi bi-x-lg"></i> Ocultar Gráfico';
    } else {
        card.style.display = 'none';
        document.getElementById('btnGrafico').innerHTML = '<i class="bi bi-graph-up"></i> Gráfico de Linha';
    }
}

// ---- Exportar CSV (espelha jButtonExportarActionPerformed: Relatorio_Fluxo_Caixa.csv) ----
function exportarFluxoCaixa() {
    var tabela = document.getElementById('tblFluxoCaixa');
    if (!tabela || tabela.querySelectorAll('tbody tr').length === 0) {
        alert('Não há dados na tabela para exportar.');
        return;
    }
    var linhas = [];
    // Cabeçalhos (colunas visíveis — pula as d-none)
    var cab = [];
    tabela.querySelectorAll('thead th').forEach(function (th) {
        if (!th.classList.contains('d-none')) cab.push(th.textContent.trim());
    });
    linhas.push(cab.join(';'));
    // Linhas
    tabela.querySelectorAll('tbody tr').forEach(function (tr) {
        var cel = [];
        tr.querySelectorAll('td').forEach(function (td) {
            if (!td.classList.contains('d-none')) cel.push(td.textContent.trim().replace(/\s+/g, ' '));
        });
        linhas.push(cel.join(';'));
    });
    var csv = '\uFEFF' + linhas.join('\n');
    var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    var link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'Relatorio_Fluxo_Caixa.csv';
    link.click();
}
</script>
<?php endif; ?>

<?php include '../includes/footer.php';
