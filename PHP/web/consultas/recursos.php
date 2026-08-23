<?php
session_start();
ini_set('display_errors', 1);
error_reporting(E_ALL);
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();
$title = 'Recursos';
include '../includes/header.php';

$db = Database::getInstance()->getConnection();
$erro = '';
$sucesso = '';

$stmtRec = $db->query("SELECT codigo, nomebco, fk_gpprinc FROM tbrecursos ORDER BY nomebco");
$recursos = $stmtRec->fetchAll();

$movimentos = [];
$saldoAnterior = 0;
$recursoSelecionado = '';
$recursoNome = '';
$recursoFk = '';
$dataIni = '';
$dataFim = '';
$dataPagto = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $recursoSelecionado = $_POST['recurso'] ?? '';
    $dataIni = $_POST['data_ini'] ?? '';
    $dataFim = $_POST['data_fim'] ?? '';
    $dataPagto = $_POST['data_pagto'] ?? '';
    $acao = $_POST['acao'] ?? 'pesquisar';

    if ($recursoSelecionado && $dataIni && $dataFim) {
        $dataIniSQL = dataParaSQL($dataIni);
        $dataFimSQL = dataParaSQL($dataFim);

        try {
            $stmtR = $db->prepare("SELECT nomebco, fk_gpprinc FROM tbrecursos WHERE codigo = ?");
            $stmtR->execute([$recursoSelecionado]);
            $recursoInfo = $stmtR->fetch();
            $recursoNome = $recursoInfo ? $recursoInfo['nomebco'] : '';
            $recursoFk = $recursoInfo ? $recursoInfo['fk_gpprinc'] : '';

            $sql = "SELECT * FROM tbmovimento WHERE dtVcto BETWEEN ? AND ? AND recurso = ?";
            if ($recursoFk == '1.002.001') {
                $sql .= " AND statusMov <> 'RC'";
            } elseif ($recursoFk == '2.001.002') {
                $sql .= " AND statusMov <> 'PG'";
            }
            $sql .= " ORDER BY dtVcto, idMov";

            $stmtMov = $db->prepare($sql);
            $stmtMov->execute([$dataIniSQL, $dataFimSQL, $recursoSelecionado]);
            $movimentos = $stmtMov->fetchAll();

            // Baixar Cartões (jButtonAtualizaApr)
            if ($acao === 'baixar_cartoes' && $recursoFk === '2.001.003') {
                if (!$dataPagto) {
                    throw new Exception("Informe a data de pagamento (dd/mm/yyyy)");
                }
                $dataPagtoSQL = dataParaSQL($dataPagto);

                $db->beginTransaction();
                try {
                    $stmtCartao = $db->prepare("UPDATE tbmovimento SET dtApr = ?, statusMov = 'PG' WHERE idMov = ?");
                    $stmtAnt = $db->prepare("UPDATE tbmovimento SET dtApr = ? WHERE idMov = ?");

                    $cartoesBaixados = 0;
                    $totalAtualizados = 0;

                    foreach ($movimentos as $m) {
                        $idMov = (int)$m['idMov'];

                        // Registro do cartão
                        $stmtCartao->execute([$dataPagtoSQL, $idMov]);
                        $totalAtualizados += $stmtCartao->rowCount();

                        // Dois anteriores (idMov-1, idMov-2)
                        for ($offset = 1; $offset <= 2; $offset++) {
                            $idAnt = $idMov - $offset;
                            if ($idAnt > 0) {
                                $stmtAnt->execute([$dataPagtoSQL, $idAnt]);
                                $totalAtualizados += $stmtAnt->rowCount();
                            }
                        }

                        $cartoesBaixados++;
                    }

                    $db->commit();

                    $sucesso = "Baixa / pagamento antecipado realizado com sucesso!<br>"
                        . "Data utilizada: {$dataPagto}<br>"
                        . "Cartões processados: {$cartoesBaixados}<br>"
                        . "Registros atualizados no total: {$totalAtualizados}<br>"
                        . "Data de pagamento sobrescrita nos registros relacionados.";

                    // Re-consulta após atualização
                    $stmtMov->execute([$dataIniSQL, $dataFimSQL, $recursoSelecionado]);
                    $movimentos = $stmtMov->fetchAll();
                } catch (Exception $e) {
                    $db->rollBack();
                    throw $e;
                }
            }

            // Saldo anterior (calculado após baixa, se houver)
            $stmtSaldo = $db->prepare("SELECT COALESCE(SUM(Valor), 0) FROM tbmovimento WHERE dtVcto < ? AND recurso = ?");
            $stmtSaldo->execute([$dataIniSQL, $recursoSelecionado]);
            $saldoAnterior = (float)$stmtSaldo->fetchColumn();
        } catch (Exception $e) {
            $erro = 'Erro: ' . $e->getMessage();
        }
    }
}
?>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-file-earmark-text"></i> Recursos</h4>
</div>

<?php if ($erro): ?>
<div class="alert alert-danger"><?= $erro ?></div>
<?php endif; ?>

<?php if ($sucesso): ?>
<div class="alert alert-success"><?= $sucesso ?></div>
<?php endif; ?>

<form method="POST" class="row g-3 mb-3" id="formRecursos">
    <input type="hidden" name="acao" id="acao" value="pesquisar">
    <div class="col-md-3">
        <label class="form-label">Recurso</label>
        <select name="recurso" class="form-select" required>
            <option value="">Selecione</option>
            <?php foreach ($recursos as $r): ?>
                <option value="<?= htmlspecialchars($r['codigo']) ?>" <?= $recursoSelecionado == $r['codigo'] ? 'selected' : '' ?>>
                    <?= htmlspecialchars($r['nomebco']) ?> (<?= htmlspecialchars($r['codigo']) ?>)
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

    <?php if ($recursoFk === '2.001.003'): ?>
    <div class="row g-3 mt-1">
        <div class="col-md-2">
            <label class="form-label">Data Pagamento</label>
            <input type="text" name="data_pagto" class="form-control" value="<?= htmlspecialchars($dataPagto) ?>" placeholder="dd/mm/yyyy">
        </div>
        <div class="col-md-3 d-flex align-items-end">
            <button type="button" class="btn btn-success" onclick="baixarCartoes()">
                <i class="bi bi-credit-card"></i> Baixar Cartões
            </button>
        </div>
    </div>
    <?php endif; ?>
</form>

<?php if ($_SERVER['REQUEST_METHOD'] === 'POST' && empty($movimentos) && !$erro && !$sucesso): ?>
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

<script>
function baixarCartoes() {
    var dataPagto = document.querySelector('input[name="data_pagto"]');
    if (!dataPagto || !dataPagto.value.trim()) {
        alert('Informe a data de pagamento (dd/mm/yyyy)');
        return;
    }
    if (confirm('Confirmar a baixa dos cartões com data de pagamento ' + dataPagto.value + '?')) {
        document.getElementById('acao').value = 'baixar_cartoes';
        document.getElementById('formRecursos').submit();
    }
}
</script>

<?php include '../includes/footer.php'; ?>
