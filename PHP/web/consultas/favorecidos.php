<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();
$title = 'Favorecidos';
include '../includes/header.php';

$db = Database::getInstance()->getConnection();
$erro = '';

$stmtFav = $db->query("SELECT codCliFor, apelidoCliFor, Tipo FROM tbclifor WHERE Tipo IN ('C','F') ORDER BY apelidoCliFor");
$favorecidos = $stmtFav->fetchAll();

$movimentos = [];
$saldoAnterior = 0;
$favSelecionado = '';
$favNome = '';
$dataIni = '';
$dataFim = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $favSelecionado = $_POST['favorecido'] ?? '';
    $dataIni = $_POST['data_ini'] ?? '';
    $dataFim = $_POST['data_fim'] ?? '';

    if ($favSelecionado && $dataIni && $dataFim) {
        $dataIniSQL = dataParaSQL($dataIni);
        $dataFimSQL = dataParaSQL($dataFim);

        try {
            $stmtF = $db->prepare("SELECT apelidoCliFor FROM tbclifor WHERE codCliFor = ?");
            $stmtF->execute([$favSelecionado]);
            $favInfo = $stmtF->fetch();
            $favNome = $favInfo ? $favInfo['apelidoCliFor'] : '';

            $stmtSaldo = $db->prepare("SELECT COALESCE(SUM(Valor), 0) FROM tbmovimento WHERE dtVcto < ? AND clifor = ?");
            $stmtSaldo->execute([$dataIniSQL, $favSelecionado]);
            $saldoAnterior = (float)$stmtSaldo->fetchColumn();

            $sql = "SELECT * FROM tbmovimento WHERE dtVcto BETWEEN ? AND ? AND clifor = ? ORDER BY dtVcto, idMov";
            $stmtMov = $db->prepare($sql);
            $stmtMov->execute([$dataIniSQL, $dataFimSQL, $favSelecionado]);
            $movimentos = $stmtMov->fetchAll();
        } catch (Exception $e) {
            $erro = 'Erro na consulta: ' . $e->getMessage();
        }
    }
}
?>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-people"></i> Favorecidos</h4>
</div>

<?php if ($erro): ?>
<div class="alert alert-danger"><?= htmlspecialchars($erro) ?></div>
<?php endif; ?>

<form method="POST" class="row g-3 mb-3">
    <div class="col-md-3">
        <label class="form-label">Favorecido</label>
        <select name="favorecido" class="form-select" required>
            <option value="">Selecione</option>
            <?php foreach ($favorecidos as $f): ?>
                <option value="<?= htmlspecialchars($f['codCliFor']) ?>" <?= $favSelecionado == $f['codCliFor'] ? 'selected' : '' ?>>
                    <?= htmlspecialchars($f['apelidoCliFor']) ?> (<?= htmlspecialchars($f['codCliFor']) ?>)
                </option>
            <?php endforeach; ?>
        </select>
    </div>
    <div class="col-md-2">
        <label class="form-label">Data Inicial</label>
        <input type="text" name="data_ini" class="form-control" value="<?= htmlspecialchars($dataIni) ?>" placeholder="dd/mm/yyyy" required>
    </div>
    <div class="col-md-2">
        <label class="form-label">Data Final</label>
        <input type="text" name="data_fim" class="form-control" value="<?= htmlspecialchars($dataFim) ?>" placeholder="dd/mm/yyyy" required>
    </div>
    <div class="col-md-2">
        <label class="form-label">Saldo Anterior</label>
        <input type="text" class="form-control" value="<?= formatMoeda($saldoAnterior) ?>" readonly>
    </div>
    <div class="col-md-2 d-flex align-items-end">
        <button type="submit" class="btn btn-primary w-100">Pesquisar</button>
    </div>
</form>

<?php if ($_SERVER['REQUEST_METHOD'] === 'POST' && empty($movimentos) && !$erro): ?>
<div class="alert alert-info">Nenhum registro encontrado no período informado.</div>
<?php endif; ?>

<?php if (!empty($movimentos)): ?>
<div class="card">
    <div class="card-body">
        <div class="table-responsive">
            <table class="table datatable table-hover">
                <thead>
                    <tr>
                        <th>Reg.</th>
                        <th>Recurso</th>
                        <th>Favorecido</th>
                        <th>Lançto.</th>
                        <th>Emissão</th>
                        <th>Vencimento</th>
                        <th>Documento</th>
                        <th>Classif.</th>
                        <th>Descrição</th>
                        <th class="text-end">Valor</th>
                        <th>Apresent.</th>
                        <th>Status</th>
                        <th>Prev.</th>
                        <th class="text-end">Saldo</th>
                    </tr>
                </thead>
                <tbody>
                    <?php
                    $saldo = $saldoAnterior;
                    foreach ($movimentos as $m):
                        $saldo += (float)($m['Valor'] ?? 0);
                    ?>
                    <tr>
                        <td><?= htmlspecialchars($m['idMov'] ?? '') ?></td>
                        <td><?= htmlspecialchars($m['recurso'] ?? '') ?></td>
                        <td><?= htmlspecialchars($m['clifor'] ?? '') ?></td>
                        <td><?= formatData($m['dtlancto'] ?? '') ?></td>
                        <td><?= formatData($m['dtEmi'] ?? '') ?></td>
                        <td><?= formatData($m['dtVcto'] ?? '') ?></td>
                        <td><?= htmlspecialchars($m['documento'] ?? '') ?></td>
                        <td><?= htmlspecialchars($m['classif'] ?? '') ?></td>
                        <td><?= htmlspecialchars(mb_substr($m['Descr'] ?? '', 0, 60)) ?></td>
                        <td class="text-end <?= ($m['Valor'] ?? 0) < 0 ? 'valor-negativo' : 'valor-positivo' ?>">
                            <?= formatMoeda($m['Valor'] ?? 0) ?>
                        </td>
                        <td><?= formatData($m['dtApr'] ?? '') ?></td>
                        <td><?= statusBadge($m['statusMov'] ?? '') ?></td>
                        <td><?= htmlspecialchars($m['Prev'] ?? '') ?></td>
                        <td class="text-end <?= $saldo >= 0 ? 'valor-positivo' : 'valor-negativo' ?>">
                            <?= formatMoeda($saldo) ?>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>
<?php endif; ?>

<?php include '../includes/footer.php'; ?>
