<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$db = Database::getInstance()->getConnection();

$action = $_GET['action'] ?? 'list';
$codigo = $_GET['cod'] ?? '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $dados = [
            'nomebco' => $_POST['nomebco'],
            'agencia' => $_POST['agencia'],
            'fluxo' => $_POST['fluxo'],
            'limite' => str_replace(['.', ','], ['', '.'], $_POST['limite']),
            'abertura' => dataParaSQL($_POST['abertura']),
            'encerramento' => !empty($_POST['encerramento']) ? dataParaSQL($_POST['encerramento']) : null,
            'status' => $_POST['status'],
            'fk_gpprinc' => $_POST['fk_gpprinc'],
        ];

        if ($action === 'edit' && $codigo) {
            $sql = "UPDATE tbrecursos SET nomebco=?, agencia=?, fluxo=?, limite=?, abertura=?, encerramento=?, status=?, fk_gpprinc=? WHERE codigo=?";
            $dados[] = $codigo;
            $stmt = $db->prepare($sql);
            $stmt->execute(array_values($dados));
            flashMessage('success', 'Recurso atualizado com sucesso!');
            redirect('recursos.php');
        }

        if ($action === 'add') {
            $codigo = trim($_POST['codigo']);
            if (empty($codigo)) {
                throw new Exception('O código é obrigatório.');
            }
            $stmt = $db->prepare("SELECT COUNT(*) FROM tbrecursos WHERE codigo=?");
            $stmt->execute([$codigo]);
            if ($stmt->fetchColumn() > 0) {
                throw new Exception('Este código já existe.');
            }
            $sql = "INSERT INTO tbrecursos (codigo, nomebco, agencia, fluxo, limite, abertura, encerramento, status, fk_gpprinc) VALUES (?,?,?,?,?,?,?,?,?)";
            $stmt = $db->prepare($sql);
            $stmt->execute([$codigo, $dados['nomebco'], $dados['agencia'], $dados['fluxo'], $dados['limite'], $dados['abertura'], $dados['encerramento'], $dados['status'], $dados['fk_gpprinc']]);
            flashMessage('success', 'Recurso cadastrado com sucesso!');
            redirect('recursos.php');
        }
    } catch (Exception $e) {
        flashMessage('danger', 'Erro: ' . $e->getMessage());
    }
}

if ($action === 'delete' && $codigo) {
    try {
        $db->prepare("DELETE FROM tbrecursos WHERE codigo=?")->execute([$codigo]);
        flashMessage('success', 'Recurso excluído com sucesso!');
    } catch (Exception $e) {
        flashMessage('danger', 'Não é possível excluir: ' . $e->getMessage());
    }
    redirect('recursos.php');
}

$stmt = $db->query("SELECT r.*, g.nome_C as nomeConta FROM tbrecursos r LEFT JOIN gpprincipal g ON r.fk_gpprinc = g.cod_Geral ORDER BY r.codigo");
$recursos = $stmt->fetchAll();

$editRec = null;
if ($action === 'edit' && $codigo) {
    $stmt = $db->prepare("SELECT * FROM tbrecursos WHERE codigo=?");
    $stmt->execute([$codigo]);
    $editRec = $stmt->fetch();
}

$stmtPlanos = $db->query("SELECT cod_Geral, nome_C FROM gpprincipal ORDER BY cod_Geral");
$planos = $stmtPlanos->fetchAll();

$title = 'Recursos';
include '../includes/header.php';
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-bank"></i> Recursos</h4>
    <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#modalRecurso">
        <i class="bi bi-plus-lg"></i> Novo Recurso
    </button>
</div>

<div class="card">
    <div class="card-body">
        <div class="table-responsive">
            <table class="table datatable table-hover">
                <thead>
                    <tr>
                        <th>Código</th>
                        <th>Nome</th>
                        <th>Agência</th>
                        <th>Fluxo</th>
                        <th>Limite</th>
                        <th>Status</th>
                        <th>Conta Plano</th>
                        <th width="120">Ações</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($recursos as $r): ?>
                    <tr>
                        <td><code><?= htmlspecialchars($r['codigo']) ?></code></td>
                        <td><?= htmlspecialchars($r['nomebco']) ?></td>
                        <td><?= htmlspecialchars($r['agencia']) ?></td>
                        <td><?= $r['fluxo'] === 'S' ? 'Sim' : 'Não' ?></td>
                        <td class="text-end"><?= formatMoeda($r['limite']) ?></td>
                        <td><?= $r['status'] === 'A' ? '<span class="badge bg-success">Ativo</span>' : '<span class="badge bg-danger">Inativo</span>' ?></td>
                        <td><?= htmlspecialchars($r['nomeConta'] ?? '-') ?></td>
                        <td>
                            <a href="?action=edit&cod=<?= urlencode($r['codigo']) ?>" class="btn btn-sm btn-outline-primary"><i class="bi bi-pencil"></i></a>
                            <a href="?action=delete&cod=<?= urlencode($r['codigo']) ?>" class="btn btn-sm btn-outline-danger" data-confirm="Excluir este recurso?"><i class="bi bi-trash"></i></a>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div class="modal fade" id="modalRecurso" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog">
        <div class="modal-content">
            <form method="POST" action="?action=<?= $editRec ? 'edit&cod=' . urlencode($editRec['codigo']) : 'add' ?>">
                <div class="modal-header">
                    <h5 class="modal-title"><?= $editRec ? 'Editar' : 'Novo' ?> Recurso</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label required">Código</label>
                        <?php if ($editRec): ?>
                        <input type="text" class="form-control" value="<?= htmlspecialchars($editRec['codigo']) ?>" readonly>
                        <?php else: ?>
                        <input type="text" name="codigo" class="form-control" placeholder="Ex: 0080" required autocomplete="off">
                        <?php endif; ?>
                    </div>
                    <div class="mb-3">
                        <label class="form-label required">Nome do Recurso</label>
                        <input type="text" name="nomebco" class="form-control" value="<?= htmlspecialchars($editRec['nomebco'] ?? '') ?>" required>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Agência</label>
                            <input type="text" name="agencia" class="form-control" value="<?= htmlspecialchars($editRec['agencia'] ?? '') ?>">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label required">Fluxo</label>
                            <select name="fluxo" class="form-select" required>
                                <option value="S" <?= ($editRec['fluxo'] ?? 'S') === 'S' ? 'selected' : '' ?>>Sim</option>
                                <option value="N" <?= ($editRec['fluxo'] ?? '') === 'N' ? 'selected' : '' ?>>Não</option>
                            </select>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-4 mb-3">
                            <label class="form-label">Limite</label>
                            <input type="text" name="limite" class="form-control money" value="<?= isset($editRec['limite']) ? number_format($editRec['limite'], 2, ',', '.') : '0,00' ?>">
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label required">Abertura</label>
                            <input type="text" name="abertura" class="form-control datepicker" value="<?= $editRec ? formatData($editRec['abertura']) : '' ?>" required placeholder="dd/mm/aaaa">
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label">Encerramento</label>
                            <input type="text" name="encerramento" class="form-control datepicker" value="<?= $editRec ? formatData($editRec['encerramento']) : '' ?>" placeholder="dd/mm/aaaa">
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label required">Status</label>
                            <select name="status" class="form-select" required>
                                <option value="A" <?= ($editRec['status'] ?? 'A') === 'A' ? 'selected' : '' ?>>Ativo</option>
                                <option value="I" <?= ($editRec['status'] ?? '') === 'I' ? 'selected' : '' ?>>Inativo</option>
                            </select>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label required">Conta no Plano</label>
                            <select name="fk_gpprinc" class="form-select select2" required>
                                <option value="">Selecione...</option>
                                <?php foreach ($planos as $p): ?>
                                <option value="<?= htmlspecialchars($p['cod_Geral']) ?>" <?= ($editRec['fk_gpprinc'] ?? '') === $p['cod_Geral'] ? 'selected' : '' ?>>
                                    <?= htmlspecialchars($p['cod_Geral'] . ' - ' . $p['nome_C']) ?>
                                </option>
                                <?php endforeach; ?>
                            </select>
                        </div>
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
<?php if ($editRec): ?>
<script>
document.addEventListener('DOMContentLoaded', function() {
    new bootstrap.Modal(document.getElementById('modalRecurso')).show();
});
</script>
<?php endif; ?>
