<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$title = 'Saldos';
include '../includes/header.php';

$db = Database::getInstance()->getConnection();

try {
    // Principal query (espelha VConsSaldos.java: vsaldos já vem agrupada/ordenada por principal/subprincipal/conta/Fonte/vrecurso/recurso)
    $stmt = $db->query("
        SELECT principal, subprincipal, conta, Fonte, vrecurso, recurso, SUM(saldos) AS saldos
        FROM vsaldos
        WHERE recurso NOT IN (
            SELECT DISTINCT recurso
            FROM view_movimento
            WHERE classif IN ('2.001.004', '2.002.001', '4.008.011')
              AND COALESCE(Prev, '') <> 'F'
        )
        GROUP BY principal, subprincipal, conta, Fonte, vrecurso, recurso
        ORDER BY
            CAST(SUBSTRING_INDEX(vrecurso, '.', 1) AS UNSIGNED),
            CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(vrecurso, '.', 2), '.', -1) AS UNSIGNED),
            CAST(SUBSTRING_INDEX(vrecurso, '.', -1) AS UNSIGNED)
    ");
    $saldos = $stmt->fetchAll();

    // Empréstimos (saldo)
    $stmtEmp = $db->query("
        SELECT 
            (COALESCE(SUM(CASE WHEN classif = '3.003.005' AND dtApr IS NOT NULL THEN Valor ELSE 0 END), 0) +
            COALESCE(SUM(CASE WHEN classif = '4.008.011' AND dtApr IS NOT NULL THEN Valor ELSE 0 END), 0)) * -1 
            AS saldo_emprestimos
        FROM view_movimento
        WHERE COALESCE(Prev, '') <> 'F'
    ");
    $saldoEmprestimos = (double)($stmtEmp->fetchColumn() ?: 0.0);

if ($saldoEmprestimos != 0) {
    $saldos[] = [
        'principal'    => '',
        'subprincipal' => '',
        'conta'        => 'Empréstimos (saldo)',
        'Fonte'        => '',
        'vrecurso'     => '',
        'recurso'      => '',
        'saldos'       => $saldoEmprestimos
    ];
}
} catch (Exception $e) {
    // fallback: sem grupos (mantém compatibilidade)
    $rows = $db->query("
        SELECT r.nomebco, m.vrecurso, m.recurso, SUM(m.Valor) AS saldo_m
        FROM tbmovimento m
        JOIN tbrecursos r ON m.recurso = r.codigo
        WHERE m.Prev <> 'F'
        GROUP BY r.nomebco, m.vrecurso, m.recurso
        ORDER BY m.vrecurso
    ")->fetchAll();
    $saldos = array_map(function ($r) {
        return [
            'principal'   => '',
            'subprincipal'=> '',
            'conta'       => $r['nomebco'],
            'Fonte'       => '',
            'vrecurso'    => $r['vrecurso'],
            'recurso'     => $r['recurso'],
            'saldos'      => $r['saldo_m'],
        ];
    }, $rows);
}

/* ---- Prepara a tabela estilo Java (grupos de contas + acumulado) ---- */
$tableRows = [];       // type: data | blank | endblank | emprestimos
$runningTotal = 0.0;    // acumulado progressivo (igual "sa" do Java)
$prevP = $prevS = $prevC = null;

foreach ($saldos as $s) {
    $saldoAtual = (float)$s['saldos'];
    $runningTotal += $saldoAtual;

    $p = $s['principal'];
    $ss = $s['subprincipal'];
    $c = $s['conta'];

    // estado: só mostra principal/sub/conta na 1ª ocorrência (igual Java)
    $showP = ($p !== null && $p !== $prevP) ? $p : '';
    $showS = ($ss !== null && $ss !== $prevS) ? $ss : '';
    $showC = ($c !== null && $c !== $prevC) ? $c : '';

    // nova conta => linha em branco antes (espelha Java)
    if ($p !== null && $c !== null && $c !== $prevC) {
        $tableRows[] = ['type' => 'blank'];
    }

    $tableRows[] = [
        'type'  => 'data',
        'p'     => $showP,
        's'     => $showS,
        'c'     => $showC,
        'fonte' => $s['Fonte'],
        'vre'   => $s['vrecurso'],
        'id'    => $s['recurso'],
        'saldo' => $saldoAtual,
        'acum'  => $runningTotal,
    ];

    $prevP = $p; $prevS = $ss; $prevC = $c;
}
$tableRows[] = ['type' => 'endblank'];
$tableRows[] = ['type' => 'emprestimos', 'valor' => $saldoEmprestimos, 'acum' => $runningTotal + $saldoEmprestimos];

/* ---- Totais para os cards ---- */
$totalGeral = array_sum(array_column($saldos, 'saldos'));
$totalPositivo = array_sum(array_filter(array_column($saldos, 'saldos'), fn($v) => $v > 0));
$totalNegativo = array_sum(array_filter(array_column($saldos, 'saldos'), fn($v) => $v < 0));
$totalImobilizado = array_sum(array_map(fn($s) => (isset($s['vrecurso']) && $s['vrecurso'] === '1.006.002') ? (float)$s['saldos'] : 0.0, $saldos));
$totalReceber = $totalPositivo - $totalImobilizado;
$totalPagar = abs($totalNegativo);
$saldoLiquido = $totalReceber - $totalPagar;
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-wallet2"></i> Saldos por Grupo de Contas</h4>
    <div class="d-flex align-items-center gap-2">
        <button class="btn btn-outline-secondary btn-sm" onclick="window.print()" title="Imprimir relatório">
            <i class="bi bi-printer"></i> Imprimir
        </button>
        <h5 class="mb-0">Total: <strong class="<?= $totalGeral >= 0 ? 'valor-positivo' : 'valor-negativo' ?>"><?= formatMoeda($totalGeral) ?></strong></h5>
    </div>
</div>

<div class="row mb-3">
    <div class="col-md-4">
        <div class="card bg-success text-white">
            <div class="card-body">
                <div class="stat-label">Total a Receber</div>
                <div class="stat-value"><?= formatMoeda($totalReceber) ?></div>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card bg-danger text-white">
            <div class="card-body">
                <div class="stat-label">Total a Pagar</div>
                <div class="stat-value"><?= formatMoeda($totalPagar) ?></div>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card bg-primary text-white">
            <div class="card-body">
                <div class="stat-label">Saldo Líquido</div>
                <div class="stat-value"><?= formatMoeda($saldoLiquido) ?></div>
            </div>
        </div>
    </div>
</div>

<div class="card">
    <div class="card-body">
        <div class="table-responsive">
            <table class="table table-sm table-hover">
                <thead>
                    <tr>
                        <th>Principal</th>
                        <th>Sub</th>
                        <th>Conta</th>
                        <th>Fonte</th>
                        <th>Vinc.Fonte</th>
                        <th>Id</th>
                        <th class="text-end">Saldo</th>
                        <th class="text-end">Acumulado</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($tableRows as $r): ?>
                        <?php if ($r['type'] === 'data'): ?>
                        <tr>
                            <td><?= htmlspecialchars((string)$r['p']) ?></td>
                            <td><?= htmlspecialchars((string)$r['s']) ?></td>
                            <td><?= htmlspecialchars((string)$r['c']) ?></td>
                            <td><?= htmlspecialchars($r['fonte']) ?></td>
                            <td><code><?= htmlspecialchars($r['vre']) ?></code></td>
                            <td><code><?= htmlspecialchars($r['id']) ?></code></td>
                            <td class="text-end <?= $r['saldo'] >= 0 ? 'valor-positivo' : 'valor-negativo' ?>"><?= formatMoeda($r['saldo']) ?></td>
                            <td class="text-end"><?= formatMoeda($r['acum']) ?></td>
                        </tr>
                        <?php elseif ($r['type'] === 'blank' || $r['type'] === 'endblank'): ?>
                        <tr><td colspan="8">&nbsp;</td></tr>
                        <?php elseif ($r['type'] === 'emprestimos'): ?>
                        <tr class="table-warning">
                            <td colspan="6"><strong>Empréstimos (saldo)</strong></td>
                            <td class="text-end <?= $r['valor'] >= 0 ? 'valor-positivo' : 'valor-negativo' ?>"><strong><?= formatMoeda($r['valor']) ?></strong></td>
                            <td class="text-end"><strong><?= formatMoeda($r['acum']) ?></strong></td>
                        </tr>
                        <?php endif; ?>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div class="card mt-3">
    <div class="card-header"><i class="bi bi-graph-up"></i> Distribuição dos Saldos</div>
    <div class="card-body">
        <canvas id="chartSaldos" height="100"></canvas>
    </div>
</div>

<script>
$(document).ready(function() {
    const data = <?= json_encode(array_map(fn($s) => [
        'nome' => ($s['conta'] ? $s['conta'] : ($s['principal'] ? $s['principal'] : 'recurso')), 
        'valor' => (float)$s['saldos']
    ], $saldos)) ?>;
    if (data.length) {
        const labels = data.map(d => d.nome);
        const values = data.map(d => d.valor);
        new Chart(document.getElementById('chartSaldos'), {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Saldo',
                    data: values,
                    backgroundColor: values.map(v => v >= 0 ? 'rgba(25,135,84,0.7)' : 'rgba(220,53,69,0.7)'),
                    borderColor: values.map(v => v >= 0 ? '#198754' : '#dc3545'),
                    borderWidth: 1
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                scales: {
                    x: { beginAtZero: true, ticks: { callback: v => 'R$ ' + v.toFixed(2).replace('.', ',') } }
                },
                plugins: { legend: { display: false } }
            }
        });
    }
});
</script>

<?php include '../includes/footer.php'; ?>
