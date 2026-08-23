<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();
$title = 'Painel Diário';
include '../includes/header.php';

$db = Database::getInstance()->getConnection();

$hoje = date('Y-m-d');
$stmt = $db->query("
    SELECT COUNT(*) as total
    FROM tbmovimento 
    WHERE DATE(dtVcto) = ?
    AND Prev <> 'F'
");
$total = $stmt->fetchColumn();

$stmtEntradas = $db->prepare("SELECT COALESCE(SUM(Valor), 0) as total FROM tbmovimento WHERE dtVcto >= ? AND dtVcto <= ? AND Valor > 0 AND Prev <> 'F'");
$stmtEntradas->execute([$hoje . ' 00:00:00', $hoje . ' 23:59:59']);
$totalEntradas = $stmtEntradas->fetchColumn();

$stmtSaidas = $db->prepare("SELECT COALESCE(SUM(ABS(Valor)), 0) as total FROM tbmovimento WHERE dtVcto >= ? AND dtVcto <= ? AND Valor < 0 AND Prev <> 'F'");
$stmtSaidas->execute([$hoje . ' 00:00:00', $hoje . ' 23:59:59']);
$totalSaidas = $stmtSaidas->fetchColumn();

$saldoFinal = $totalEntradas - $totalSaidas;
?>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-speedometer-2"></i> Painel Diário</h4>
    <div class="text-muted">Data: <?= date('d/m/Y') ?></div>
</div>

<div class="row mb-3">
    <div class="col-md-4">
        <div class="card bg-info text-white">
            <div class="card-body">
                <div class="stat-label">Total de Movimentos</div>
                <div class="stat-value"><?= $total ?></div>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card bg-success text-white">
            <div class="card-body">
                <div class="stat-label">Total Entradas</div>
                <div class="stat-value"><?= formatMoeda($totalEntradas) ?></div>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card bg-danger text-white">
            <div class="card-body">
                <div class="stat-label">Total Saídas</div>
                <div class="stat-value"><?= formatMoeda($totalSaidas) ?></div>
            </div>
        </div>
    </div>
</div>

<div class="card mt-3">
    <div class="card-header"><i class="bi bi-list"></i> Movimentos de Hoje</div>
    <div class="card-body">
        <div class="table-responsive">
            <table class="table table-sm">
                <thead>
                    <tr>
                        <th>Data</th>
                        <th>Documento</th>
                        <th>Descrição</th>
                        <th>Valor</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    <?php
                    $stmtMov = $db->query("
                        SELECT dtVcto, documento, Descr, Valor, statusMov
                        FROM tbmovimento 
                        WHERE dtVcto >= ? AND dtVcto <= ? AND Prev <> 'F'
                        ORDER BY dtVcto DESC
                    ");
                    $stmtMov->execute([$hoje . ' 00:00:00', $hoje . ' 23:59:59']);
                    $movimentos = $stmtMov->fetchAll();
                    if ($movimentos):
                    ?>
                    <tbody>
                        <?php foreach ($movimentos as $m): ?>
                        <tr class="<?= $m['Valor'] < 0 ? 'table-danger' : 'table-success' ?>">
                            <td><?= formatData($m['dtVcto']) ?></td>
                            <td><?= htmlspecialchars($m['documento']) ?></td>
                            <td><?= htmlspecialchars(mb_substr($m['Descr'], 0, 60)) ?></td>
                            <td class="text-end <?= $m['Valor'] < 0 ? 'valor-negativo' : 'valor-positivo' ?>">
                                <?= formatMoeda($m['Valor']) ?>
                            </td>
                            <td><?= statusBadge($m['statusMov']) ?></td>
                        </tr>
                        <?php endforeach; ?>
                    </tbody>
                </div>
            </table>
        </div>
    </div>
</div>

<?php include '../includes/footer.php'; ?>