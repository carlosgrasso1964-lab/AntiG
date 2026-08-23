<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$db = Database::getInstance()->getConnection();

$action = $_GET['action'] ?? 'list';
$codGeral = $_GET['cod'] ?? '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        if ($action === 'edit' && $codGeral) {
            $stmt = $db->prepare("UPDATE gpprincipal SET nome_P=?, nome_S=?, nome_C=? WHERE cod_Geral=?");
            $stmt->execute([$_POST['nome_P'], $_POST['nome_S'], $_POST['nome_C'], $codGeral]);
            flashMessage('success', 'Conta atualizada com sucesso!');
        } elseif ($action === 'add') {
            $codGeral = trim($_POST['cod_Geral']);
            if (empty($codGeral)) {
                throw new Exception('O código é obrigatório.');
            }
            $stmt = $db->prepare("SELECT COUNT(*) FROM gpprincipal WHERE cod_Geral=?");
            $stmt->execute([$codGeral]);
            if ($stmt->fetchColumn() > 0) {
                throw new Exception('Este código já existe.');
            }
            $stmt = $db->prepare("INSERT INTO gpprincipal (cod_Geral, nome_P, nome_S, nome_C) VALUES (?, ?, ?, ?)");
            $stmt->execute([$codGeral, $_POST['nome_P'], $_POST['nome_S'], $_POST['nome_C']]);
            flashMessage('success', 'Conta cadastrada com sucesso!');
        }
        redirect('plano_contas.php');
    } catch (Exception $e) {
        flashMessage('danger', 'Erro: ' . $e->getMessage());
    }
}

if ($action === 'delete' && $codGeral) {
    try {
        $stmt = $db->prepare("DELETE FROM gpprincipal WHERE cod_Geral=?");
        $stmt->execute([$codGeral]);
        flashMessage('success', 'Conta excluída com sucesso!');
    } catch (Exception $e) {
        flashMessage('danger', 'Não é possível excluir: ' . $e->getMessage());
    }
    redirect('plano_contas.php');
}

$stmt = $db->query("SELECT * FROM gpprincipal ORDER BY cod_Geral");
$contas = $stmt->fetchAll();

$editConta = null;
if ($action === 'edit' && $codGeral) {
    $stmt = $db->prepare("SELECT * FROM gpprincipal WHERE cod_Geral=?");
    $stmt->execute([$codGeral]);
    $editConta = $stmt->fetch();
}

$title = 'Plano de Contas';
include '../includes/header.php';
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-bookmark"></i> Plano de Contas</h4>
    <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#modalConta">
        <i class="bi bi-plus-lg"></i> Nova Conta
    </button>
</div>

<div class="card">
    <div class="card-body">
        <div class="table-responsive">
            <table class="table datatable table-hover">
                <thead>
                    <tr>
                        <th>Código</th>
                        <th>Principal</th>
                        <th>Sub</th>
                        <th>Conta</th>
                        <th width="120">Ações</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($contas as $c): ?>
                    <tr>
                        <td><code><?= htmlspecialchars($c['cod_Geral']) ?></code></td>
                        <td><?= htmlspecialchars($c['nome_P']) ?></td>
                        <td><?= htmlspecialchars($c['nome_S']) ?></td>
                        <td><?= htmlspecialchars($c['nome_C']) ?></td>
                        <td>
                            <a href="?action=edit&cod=<?= urlencode($c['cod_Geral']) ?>" class="btn btn-sm btn-outline-primary" title="Editar">
                                <i class="bi bi-pencil"></i>
                            </a>
                            <a href="?action=delete&cod=<?= urlencode($c['cod_Geral']) ?>" class="btn btn-sm btn-outline-danger" data-confirm="Excluir esta conta?" title="Excluir">
                                <i class="bi bi-trash"></i>
                            </a>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div class="modal fade" id="modalConta" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog">
        <div class="modal-content">
            <form method="POST" action="?action=<?= $editConta ? 'edit&cod=' . urlencode($editConta['cod_Geral']) : 'add' ?>">
                <div class="modal-header">
                    <h5 class="modal-title"><?= $editConta ? 'Editar' : 'Nova' ?> Conta</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label required">Código</label>
                        <?php if ($editConta): ?>
                        <input type="text" class="form-control" value="<?= htmlspecialchars($editConta['cod_Geral']) ?>" readonly>
                        <?php else: ?>
                        <input type="text" name="cod_Geral" class="form-control" placeholder="Ex: 1.001.000" required autocomplete="off">
                        <?php endif; ?>
                    </div>
                    <div class="mb-3">
                        <label class="form-label required">Nome Principal</label>
                        <input type="text" name="nome_P" class="form-control" value="<?= htmlspecialchars($editConta['nome_P'] ?? '') ?>" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label required">Nome Sub</label>
                        <input type="text" name="nome_S" class="form-control" value="<?= htmlspecialchars($editConta['nome_S'] ?? '') ?>" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label required">Nome Conta</label>
                        <input type="text" name="nome_C" class="form-control" value="<?= htmlspecialchars($editConta['nome_C'] ?? '') ?>" required>
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

<?php include '../includes/footer.php'; ?>
<?php if ($editConta): ?>
<script>
document.addEventListener('DOMContentLoaded', function() {
    new bootstrap.Modal(document.getElementById('modalConta')).show();
});
</script>
<?php endif; ?>
