<?php
session_start();
ini_set('display_errors', 1);
error_reporting(E_ALL);
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$title = 'Planejamento - Plano Diretor';
include '../includes/header.php';

$db = Database::getInstance()->getConnection();
$msg = '';

// === ADD ===
if (isset($_POST['btnAdicionar'])) {
    $conta = trim($_POST['conta'] ?? '');
    $ano = (int)($_POST['ano'] ?? date('Y'));
    $mes = (int)($_POST['mes'] ?? 0);
    $valor = str_replace(',', '.', str_replace('.', '', $_POST['valor'] ?? '0'));
    $inflacao = str_replace(',', '.', $_POST['inflacao'] ?? '5.0');
    if ($conta && $ano >= 2020 && $mes >= 1 && $mes <= 12) {
        $stmt = $db->prepare("INSERT INTO tb_plano_diretor (ano_referencia, mes, conta, valor_planejado, inflacao_premissa) VALUES (?, ?, ?, ?, ?)");
        $stmt->execute([$ano, $mes, $conta, (float)$valor, (float)$inflacao]);
        $msg = '<div class="alert alert-success">Registro adicionado com sucesso!</div>';
    } else { $msg = '<div class="alert alert-danger">Preencha todos os campos corretamente.</div>'; }
}

// === UPDATE ===
if (isset($_POST['btnAtualizar'])) {
    $id = (int)($_POST['id'] ?? 0);
    $conta = trim($_POST['conta'] ?? '');
    $ano = (int)($_POST['ano'] ?? date('Y'));
    $mes = (int)($_POST['mes'] ?? 0);
    $valor = str_replace(',', '.', str_replace('.', '', $_POST['valor'] ?? '0'));
    $inflacao = str_replace(',', '.', $_POST['inflacao'] ?? '5.0');
    if ($id && $conta && $ano >= 2020 && $mes >= 1 && $mes <= 12) {
        $stmt = $db->prepare("UPDATE tb_plano_diretor SET ano_referencia=?, mes=?, conta=?, valor_planejado=?, inflacao_premissa=? WHERE id=?");
        $stmt->execute([$ano, $mes, $conta, (float)$valor, (float)$inflacao, $id]);
        $msg = '<div class="alert alert-success">Registro atualizado com sucesso!</div>';
    } else { $msg = '<div class="alert alert-danger">Selecione um registro ou preencha os campos.</div>'; }
}

// === DELETE ===
if (isset($_POST['btnExcluir'])) {
    $id = (int)($_POST['id'] ?? 0);
    if ($id) {
        $stmt = $db->prepare("DELETE FROM tb_plano_diretor WHERE id=?");
        $stmt->execute([$id]);
        $msg = '<div class="alert alert-success">Registro excluído com sucesso!</div>';
    } else { $msg = '<div class="alert alert-danger">Selecione um registro para excluir.</div>'; }
}

// === FILTER ===
$dataIni = $_GET['dataIni'] ?? $_POST['dataIni'] ?? '';
$dataFim = $_GET['dataFim'] ?? $_POST['dataFim'] ?? '';
$rows = [];
if ($dataIni && $dataFim) {
    $partsIni = explode('/', $dataIni);
    $partsFim = explode('/', $dataFim);
    if (count($partsIni) === 3 && count($partsFim) === 3) {
        $anoIni = (int)$partsIni[2]; $mesIni = (int)$partsIni[1];
        $anoFim = (int)$partsFim[2]; $mesFim = (int)$partsFim[1];
        $sql = "SELECT id, conta, ano_referencia, mes, valor_planejado, inflacao_premissa
                FROM tb_plano_diretor
                WHERE ((ano_referencia = ? AND mes >= ?) OR (ano_referencia > ?))
                  AND ((ano_referencia = ? AND mes <= ?) OR (ano_referencia < ?))
                ORDER BY ano_referencia DESC, mes DESC, conta";
        $stmt = $db->prepare($sql);
        $stmt->execute([$anoIni, $mesIni, $anoFim, $anoFim, $mesFim, $anoIni]);
        $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);
    }
}
?>
<div class="container-fluid mt-3">
    <h4>Planejamento - Plano Diretor</h4>
    <?= $msg ?>
    <form method="GET" class="row g-2 mb-3 align-items-end">
        <div class="col-auto">
            <label class="form-label small">Data Inicial</label>
            <input type="text" name="dataIni" class="form-control form-control-sm" placeholder="dd/mm/aaaa"
                   value="<?= htmlspecialchars($dataIni) ?>" style="width:120px">
        </div>
        <div class="col-auto">
            <label class="form-label small">Data Final</label>
            <input type="text" name="dataFim" class="form-control form-control-sm" placeholder="dd/mm/aaaa"
                   value="<?= htmlspecialchars($dataFim) ?>" style="width:120px">
        </div>
        <div class="col-auto">
            <button type="submit" class="btn btn-primary btn-sm">Carregar</button>
        </div>
    </form>

    <form method="POST" class="row g-2 mb-3 p-2 bg-light border rounded align-items-end">
        <input type="hidden" name="id" id="editId" value="0">
        <div class="col-auto">
            <label class="form-label small">Conta</label>
            <input type="text" name="conta" id="editConta" class="form-control form-control-sm" required style="width:200px">
        </div>
        <div class="col-auto">
            <label class="form-label small">Ano</label>
            <select name="ano" id="editAno" class="form-select form-select-sm" style="width:90px">
                <?php for ($y = 2020; $y <= 2030; $y++): ?>
                <option value="<?= $y ?>" <?= $y === (int)date('Y') ? 'selected' : '' ?>><?= $y ?></option>
                <?php endfor; ?>
            </select>
        </div>
        <div class="col-auto">
            <label class="form-label small">Mês</label>
            <select name="mes" id="editMes" class="form-select form-select-sm" style="width:80px">
                <?php for ($m = 1; $m <= 12; $m++): ?>
                <option value="<?= $m ?>"><?= $m ?></option>
                <?php endfor; ?>
            </select>
        </div>
        <div class="col-auto">
            <label class="form-label small">Valor</label>
            <input type="text" name="valor" id="editValor" class="form-control form-control-sm" style="width:130px">
        </div>
        <div class="col-auto">
            <label class="form-label small">Inflação %</label>
            <input type="text" name="inflacao" id="editInflacao" class="form-control form-control-sm" value="5.0" style="width:80px">
        </div>
        <div class="col-auto">
            <button type="submit" name="btnAdicionar" class="btn btn-success btn-sm">Adicionar</button>
            <button type="submit" name="btnAtualizar" class="btn btn-primary btn-sm">Atualizar</button>
            <button type="submit" name="btnExcluir" class="btn btn-danger btn-sm">Excluir</button>
            <button type="button" class="btn btn-secondary btn-sm" onclick="limparCampos()">Limpar</button>
        </div>
    </form>

    <div class="table-responsive">
        <table class="table table-sm table-bordered table-striped">
            <thead class="table-light">
                <tr>
                    <th>#</th><th>Conta</th><th>Ano</th><th>Mês</th>
                    <th>Valor Planejado</th><th>Inflação %</th><th>Ação</th>
                </tr>
            </thead>
            <tbody>
                <?php if ($rows): ?>
                <?php foreach ($rows as $r): ?>
                <tr>
                    <td><?= $r['id'] ?></td>
                    <td><?= htmlspecialchars($r['conta']) ?></td>
                    <td><?= $r['ano_referencia'] ?></td>
                    <td><?= $r['mes'] ?></td>
                    <td class="text-end"><?= number_format($r['valor_planejado'], 2, ',', '.') ?></td>
                    <td class="text-end"><?= number_format($r['inflacao_premissa'], 2, ',', '.') ?></td>
                    <td>
                        <button class="btn btn-outline-primary btn-sm" onclick="editar(<?= $r['id'] ?>, '<?= htmlspecialchars(addslashes($r['conta']), ENT_QUOTES) ?>', <?= $r['ano_referencia'] ?>, <?= $r['mes'] ?>, <?= $r['valor_planejado'] ?>, <?= $r['inflacao_premissa'] ?>)">Editar</button>
                    </td>
                </tr>
                <?php endforeach; ?>
                <?php else: ?>
                <tr><td colspan="7" class="text-center text-muted">Nenhum registro. Informe o período e clique Carregar.</td></tr>
                <?php endif; ?>
            </tbody>
        </table>
    </div>
</div>

<script>
function editar(id, conta, ano, mes, valor, inflacao) {
    document.getElementById('editId').value = id;
    document.getElementById('editConta').value = conta;
    document.getElementById('editAno').value = ano;
    document.getElementById('editMes').value = mes;
    document.getElementById('editValor').value = valor.toLocaleString('pt-BR', {minimumFractionDigits:2, maximumFractionDigits:2});
    document.getElementById('editInflacao').value = inflacao.toLocaleString('pt-BR', {minimumFractionDigits:2, maximumFractionDigits:2});
    window.scrollTo({top: 0, behavior: 'smooth'});
}
function limparCampos() {
    document.getElementById('editId').value = 0;
    document.getElementById('editConta').value = '';
    document.getElementById('editValor').value = '';
    document.getElementById('editInflacao').value = '5.0';
}
</script>

<?php include '../includes/footer.php'; ?>
