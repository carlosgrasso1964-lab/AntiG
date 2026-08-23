<?php
session_start();
require_once 'config/database.php';
require_once 'includes/functions.php';
requireAuth();

$title = 'Dashboard';
include 'includes/header.php';

// === SALDO DISPONÍVEL (Bancos + Conta Poupança - saldo total por recurso) ===
$stmt = Database::query("SELECT COALESCE(SUM(i.Valor), 0) FROM csn_ijmov i JOIN tbrecursos r ON i.recurso = r.codigo WHERE r.fk_gpprinc IN ('1.001.001', '1.003.001')");
$saldoDisponivel = $stmt->fetchColumn();

// === A RECEBER (Contas à Receber - vrecurso 1.002.001) ===
$stmt = Database::query("SELECT COALESCE(SUM(Valor), 0) FROM tbmovimento WHERE vrecurso = '1.002.001' AND Prev <> 'F' AND statusMov NOT IN ('PG', 'RC')");
$totalReceber = $stmt->fetchColumn();

// === A PAGAR (Contas à Pagar - recurso 0079 / vrecurso 2.001.002) ===
$stmt = Database::query("SELECT COALESCE(SUM(Valor), 0) FROM tbmovimento WHERE vrecurso = '2.001.002' AND Prev <> 'F' AND statusMov NOT IN ('PG', 'SI', 'TO')");
$totalPagar = $stmt->fetchColumn();

// === CARTÕES DE CRÉDITO (Total em aberto) ===
$stmt = Database::query("SELECT COALESCE(SUM(Valor), 0) FROM csn_ijmov WHERE vrecurso = '2.001.003' AND recurso <> '0079'");
$totalCartoes = $stmt->fetchColumn();

// === CARTÕES DE CRÉDITO - A Vencer (7 dias) ===
$stmt = Database::prepare("SELECT idMov, documento, Descr, Valor, dtVcto, recurso FROM tbmovimento WHERE vrecurso = '2.001.003' AND recurso <> '0079' AND dtVcto BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY) AND Prev <> 'F' AND statusMov NOT IN ('PG', 'SI', 'TO', 'TD') ORDER BY dtVcto");
$stmt->execute();
$cartoesAVencer = $stmt->fetchAll();
$totalCartoesAVencer = array_sum(array_column($cartoesAVencer, 'Valor'));

// === VENCIDOS E A VENCER (apenas contas a receber e contas a pagar) ===
$filtroVencidos = "vrecurso IN ('1.002.001', '2.001.002') AND Prev <> 'F' AND statusMov NOT IN ('PG', 'RC', 'SI', 'TO')";

$stmt = Database::query("SELECT COUNT(*) FROM tbmovimento WHERE dtVcto < CURDATE() AND $filtroVencidos");
$vencidos = $stmt->fetchColumn();

$stmt = Database::query("SELECT COUNT(*) FROM tbmovimento WHERE dtVcto BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY) AND $filtroVencidos");
$avencer = $stmt->fetchColumn();

// === INVESTIMENTOS (excluindo poupança 1.003.001) ===
$stmt = Database::query("SELECT COALESCE(SUM(i.Valor), 0) FROM csn_ijmov i JOIN tbrecursos r ON i.recurso=r.codigo WHERE r.fk_gpprinc LIKE '1.003.%' AND r.fk_gpprinc <> '1.003.001'");
$totalInvestimentos = $stmt->fetchColumn();

// === INVESTIMENTOS - Detalhamento ===
$stmt = Database::prepare("SELECT i.recurso, i.nomebco, r.fk_gpprinc, SUM(i.Valor) as total FROM csn_ijmov i JOIN tbrecursos r ON i.recurso=r.codigo WHERE r.fk_gpprinc LIKE '1.003.%' AND r.fk_gpprinc <> '1.003.001' GROUP BY i.recurso ORDER BY r.fk_gpprinc");
$stmt->execute();
$investimentosDetalhe = $stmt->fetchAll();

// === IMOBILIZADO ===
$stmt = Database::query("SELECT COALESCE(SUM(i.Valor), 0) FROM csn_ijmov i JOIN tbrecursos r ON i.recurso=r.codigo WHERE r.fk_gpprinc = '1.006.002'");
$totalImobilizado = $stmt->fetchColumn();

// === IMOBILIZADO - Detalhamento ===
$stmt = Database::prepare("SELECT i.recurso, i.nomebco, SUM(i.Valor) as total FROM csn_ijmov i JOIN tbrecursos r ON i.recurso=r.codigo WHERE r.fk_gpprinc = '1.006.002' GROUP BY i.recurso ORDER BY i.recurso");
$stmt->execute();
$imobilizadoDetalhe = $stmt->fetchAll();

$stmt = Database::query("SELECT COUNT(*) FROM tbclifor");
$totalCliFor = $stmt->fetchColumn();

$stmt = Database::query("SELECT COUNT(*) FROM tbrecursos WHERE status = 'A'");
$totalRecursos = $stmt->fetchColumn();
?>

<div class="d-flex justify-content-between align-items-center mb-4">
    <h4 class="page-title mb-0"><i class="bi bi-speedometer2"></i> Dashboard</h4>
    <span class="text-muted"><i class="bi bi-calendar"></i> <?= date('d/m/Y H:i') ?></span>
</div>

<div class="row">
    <div class="col-md-3">
        <div class="card stat-card border-start border-primary border-4">
            <div class="card-body">
                <div class="stat-value text-primary"><?= formatMoeda($saldoDisponivel) ?></div>
                <div class="stat-label">Saldo Disponível</div>
                <i class="bi bi-cash-coin stat-icon position-absolute top-0 end-0 mt-2 me-2"></i>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card stat-card border-start border-success border-4">
            <div class="card-body">
                <div class="stat-value text-success"><?= formatMoeda($totalReceber) ?></div>
                <div class="stat-label">A Receber</div>
                <i class="bi bi-arrow-up-circle stat-icon position-absolute top-0 end-0 mt-2 me-2"></i>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card stat-card border-start border-danger border-4">
            <div class="card-body">
                <div class="stat-value text-danger"><?= formatMoeda(abs($totalPagar)) ?></div>
                <div class="stat-label">A Pagar</div>
                <i class="bi bi-arrow-down-circle stat-icon position-absolute top-0 end-0 mt-2 me-2"></i>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card stat-card border-start border-info border-4">
            <div class="card-body">
                <div class="stat-value text-info"><?= formatMoeda(abs($totalCartoes)) ?></div>
                <div class="stat-label">Cartões de Crédito</div>
                <i class="bi bi-credit-card stat-icon position-absolute top-0 end-0 mt-2 me-2"></i>
            </div>
        </div>
    </div>
</div>

<div class="row mt-2">
    <div class="col-md-6">
        <div class="card">
            <div class="card-header"><i class="bi bi-clock"></i> A Vencer (7 dias)</div>
            <div class="card-body">
                <?php
                $stmt = Database::prepare("SELECT idMov, documento, Descr, Valor, dtVcto, statusMov FROM tbmovimento WHERE dtVcto BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY) AND vrecurso IN ('1.002.001', '2.001.002') AND statusMov NOT IN ('PG', 'RC', 'SI', 'TO') AND Prev <> 'F' ORDER BY dtVcto LIMIT 10");
                $stmt->execute();
                $rows = $stmt->fetchAll();
                ?>
                <?php if (empty($rows)): ?>
                    <p class="text-muted mb-0">Nenhum título a vencer nos próximos 7 dias.</p>
                <?php else: ?>
                <div class="table-responsive">
                    <table class="table table-sm">
                        <thead>
                            <tr>
                                <th>Vencimento</th>
                                <th>Documento</th>
                                <th>Descrição</th>
                                <th class="text-end">Valor</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($rows as $r): ?>
                            <tr>
                                <td><?= formatData($r['dtVcto']) ?></td>
                                <td><?= htmlspecialchars($r['documento']) ?></td>
                                <td><?= htmlspecialchars($r['Descr']) ?></td>
                                <td class="text-end <?= $r['Valor'] < 0 ? 'valor-negativo' : 'valor-positivo' ?>">
                                    <?= formatMoeda($r['Valor']) ?>
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
    <div class="col-md-6">
        <div class="card">
            <div class="card-header"><i class="bi bi-credit-card"></i> Cartões - A Vencer (7 dias)</div>
            <div class="card-body">
                <?php if (empty($cartoesAVencer)): ?>
                    <p class="text-muted mb-0">Nenhuma fatura de cartão a vencer nos próximos 7 dias.</p>
                <?php else: ?>
                <div class="table-responsive">
                    <table class="table table-sm">
                        <thead>
                            <tr>
                                <th>Vencimento</th>
                                <th>Cartão</th>
                                <th>Descrição</th>
                                <th class="text-end">Valor</th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php foreach ($cartoesAVencer as $r): ?>
                            <tr>
                                <td><?= formatData($r['dtVcto']) ?></td>
                                <td><code><?= htmlspecialchars($r['recurso']) ?></code></td>
                                <td><?= htmlspecialchars($r['Descr']) ?></td>
                                <td class="text-end valor-negativo"><?= formatMoeda($r['Valor']) ?></td>
                            </tr>
                            <?php endforeach; ?>
                        </tbody>
                        <tfoot>
                            <tr class="table-dark">
                                <th colspan="3">Total</th>
                                <th class="text-end valor-negativo"><?= formatMoeda($totalCartoesAVencer) ?></th>
                            </tr>
                        </tfoot>
                    </table>
                </div>
                <?php endif; ?>
            </div>
        </div>
    </div>
</div>

<div class="row mt-2">
    <div class="col-md-3">
        <div class="card stat-card border-start border-secondary border-4" style="cursor:pointer" data-bs-toggle="collapse" data-bs-target="#detalheInvestimentos">
            <div class="card-body">
                <div class="stat-value text-secondary"><?= formatMoeda($totalInvestimentos) ?></div>
                <div class="stat-label">Investimentos <i class="bi bi-chevron-down" style="font-size:0.7rem"></i></div>
                <i class="bi bi-graph-up-arrow stat-icon position-absolute top-0 end-0 mt-2 me-2"></i>
            </div>
        </div>
        <div class="collapse" id="detalheInvestimentos">
            <div class="card card-body py-2 px-3">
                <table class="table table-sm mb-0">
                    <tbody>
                        <?php foreach ($investimentosDetalhe as $item): ?>
                        <tr>
                            <td class="ps-0"><?= htmlspecialchars($item['nomebco']) ?></td>
                            <td class="text-end pe-0"><?= formatMoeda($item['total']) ?></td>
                        </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card stat-card border-start border-secondary border-4" style="cursor:pointer" data-bs-toggle="collapse" data-bs-target="#detalheImobilizado">
            <div class="card-body">
                <div class="stat-value text-secondary"><?= formatMoeda($totalImobilizado) ?></div>
                <div class="stat-label">Imobilizado <i class="bi bi-chevron-down" style="font-size:0.7rem"></i></div>
                <i class="bi bi-building stat-icon position-absolute top-0 end-0 mt-2 me-2"></i>
            </div>
        </div>
        <div class="collapse" id="detalheImobilizado">
            <div class="card card-body py-2 px-3">
                <table class="table table-sm mb-0">
                    <tbody>
                        <?php foreach ($imobilizadoDetalhe as $item): ?>
                        <tr>
                            <td class="ps-0"><?= htmlspecialchars($item['nomebco']) ?></td>
                            <td class="text-end pe-0"><?= formatMoeda($item['total']) ?></td>
                        </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card">
            <div class="card-header"><i class="bi bi-people"></i> Clientes/Fornecedores</div>
            <div class="card-body text-center">
                <h2 class="text-primary"><?= $totalCliFor ?></h2>
                <p class="text-muted mb-0">cadastrados</p>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card">
            <div class="card-header"><i class="bi bi-bank"></i> Recursos Ativos</div>
            <div class="card-body text-center">
                <h2 class="text-success"><?= $totalRecursos ?></h2>
                <p class="text-muted mb-0">contas ativas</p>
            </div>
        </div>
    </div>
</div>

<div class="row mt-2">
    <div class="col-md-12">
        <div class="card">
            <div class="card-header"><i class="bi bi-pie-chart"></i> Saldo por Recurso</div>
            <div class="card-body">
                <canvas id="chartRecursos" height="200"></canvas>
            </div>
        </div>
    </div>
</div>

<script>
$(document).ready(function() {
    $.getJSON('utils/api_saldos.php', function(data) {
        if (data && data.length) {
            const labels = data.map(d => d.nomebco || d.vrecurso);
            const values = data.map(d => parseFloat(d.Saldos) || 0);
            const colors = values.map(v => v >= 0 ? '#198754' : '#dc3545');
            new Chart(document.getElementById('chartRecursos'), {
                type: 'doughnut',
                data: {
                    labels: labels,
                    datasets: [{
                        data: values,
                        backgroundColor: ['#0d6efd', '#198754', '#ffc107', '#dc3545', '#0dcaf0', '#6f42c1', '#fd7e14', '#20c997'],
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: { position: 'bottom', labels: { font: { size: 11 } } }
                    }
                }
            });
        }
    });
});
</script>

<?php include 'includes/footer.php'; ?>
