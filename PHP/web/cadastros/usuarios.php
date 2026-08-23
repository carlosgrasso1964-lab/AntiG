<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$db = Database::getInstance()->getConnection();

$action = $_GET['action'] ?? 'list';
$id = $_GET['id'] ?? '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $usuario = trim($_POST['usuario']);
        $senha = $_POST['senha'] ?? '';

        if ($action === 'edit' && $id) {
            if (!empty($senha)) {
                $senhaMD5 = md5($senha);
                $stmt = $db->prepare("UPDATE dados_senhas SET usuario=?, senha=? WHERE id=?");
                $stmt->execute([$usuario, $senhaMD5, $id]);
            } else {
                $stmt = $db->prepare("UPDATE dados_senhas SET usuario=? WHERE id=?");
                $stmt->execute([$usuario, $id]);
            }
            flashMessage('success', 'Usuário atualizado com sucesso!');
            redirect('usuarios.php');
        }

        if ($action === 'add') {
            if (empty($senha)) {
                throw new Exception('A senha é obrigatória para novos usuários.');
            }
            $stmt = $db->prepare("SELECT COUNT(*) FROM dados_senhas WHERE usuario=?");
            $stmt->execute([$usuario]);
            if ($stmt->fetchColumn() > 0) {
                throw new Exception('Este usuário já existe.');
            }
            $senhaMD5 = md5($senha);
            $stmt = $db->prepare("INSERT INTO dados_senhas(usuario, senha) VALUES (?, ?)");
            $stmt->execute([$usuario, $senhaMD5]);
            flashMessage('success', 'Usuário cadastrado com sucesso!');
            redirect('usuarios.php');
        }
    } catch (Exception $e) {
        flashMessage('danger', 'Erro: ' . $e->getMessage());
    }
}

if ($action === 'delete' && $id) {
    try {
        $db->prepare("DELETE FROM dados_senhas WHERE id=?")->execute([$id]);
        flashMessage('success', 'Usuário excluído com sucesso!');
    } catch (Exception $e) {
        flashMessage('danger', 'Erro ao excluir: ' . $e->getMessage());
    }
    redirect('usuarios.php');
}

$stmt = $db->query("SELECT id, usuario FROM dados_senhas ORDER BY id");
$usuarios = $stmt->fetchAll();

$editUser = null;
if ($action === 'edit' && $id) {
    $stmt = $db->prepare("SELECT * FROM dados_senhas WHERE id=?");
    $stmt->execute([$id]);
    $editUser = $stmt->fetch();
}

$title = 'Usuários';
include '../includes/header.php';
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-people"></i> Usuários</h4>
    <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#modalUsuario">
        <i class="bi bi-plus-lg"></i> Novo Usuário
    </button>
</div>

<div class="card">
    <div class="card-body">
        <div class="table-responsive">
            <table class="table datatable table-hover">
                <thead>
                    <tr>
                        <th width="80">ID</th>
                        <th>Usuário</th>
                        <th width="120">Ações</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($usuarios as $u): ?>
                    <tr>
                        <td><code><?= htmlspecialchars($u['id']) ?></code></td>
                        <td><?= htmlspecialchars($u['usuario']) ?></td>
                        <td>
                            <a href="?action=edit&id=<?= urlencode($u['id']) ?>" class="btn btn-sm btn-outline-primary"><i class="bi bi-pencil"></i></a>
                            <a href="?action=delete&id=<?= urlencode($u['id']) ?>" class="btn btn-sm btn-outline-danger" data-confirm="Excluir este usuário?"><i class="bi bi-trash"></i></a>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div class="modal fade" id="modalUsuario" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog">
        <div class="modal-content">
            <form method="POST" action="?action=<?= $editUser ? 'edit&id=' . urlencode($editUser['id']) : 'add' ?>">
                <div class="modal-header">
                    <h5 class="modal-title"><?= $editUser ? 'Editar' : 'Novo' ?> Usuário</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label required">Usuário</label>
                        <input type="text" name="usuario" class="form-control" value="<?= htmlspecialchars($editUser['usuario'] ?? '') ?>" required autocomplete="off">
                    </div>
                    <div class="mb-3">
                        <label class="form-label <?= $editUser ? '' : 'required' ?>">Senha</label>
                        <input type="password" name="senha" class="form-control" <?= $editUser ? 'placeholder="Deixe em branco para manter a atual"' : 'required' ?> autocomplete="off">
                        <?php if ($editUser): ?>
                        <small class="text-muted">Deixe em branco para manter a senha atual.</small>
                        <?php endif; ?>
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
<?php if ($editUser): ?>
<script>
document.addEventListener('DOMContentLoaded', function() {
    new bootstrap.Modal(document.getElementById('modalUsuario')).show();
});
</script>
<?php endif; ?>
