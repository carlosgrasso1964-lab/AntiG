<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$db = Database::getInstance()->getConnection();
$action = $_GET['action'] ?? 'list';
$cod = $_GET['cod'] ?? '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    try {
        $dados = [
            'Tipo' => $_POST['Tipo'],
            'nomeCliFor' => mb_strtoupper(trim($_POST['nomeCliFor'])),
            'apelidoCliFor' => mb_strtoupper(trim($_POST['apelidoCliFor'])),
            'contatoCliFor' => $_POST['contatoCliFor'] ?? '',
            'obs' => $_POST['obs'] ?? '',
            'fkCliForGp' => $_POST['fkCliForGp'],
            'email' => $_POST['email'] ?? '',
            'celular' => preg_replace('/\D/', '', $_POST['celular'] ?? ''),
            'telefone' => preg_replace('/\D/', '', $_POST['telefone'] ?? ''),
            'cep' => preg_replace('/\D/', '', $_POST['cep'] ?? ''),
            'endereco' => mb_strtoupper(trim($_POST['endereco'] ?? '')),
            'numero' => $_POST['numero'] ?? null,
            'complemento' => mb_strtoupper(trim($_POST['complemento'] ?? '')),
            'bairro' => mb_strtoupper(trim($_POST['bairro'] ?? '')),
            'cidade' => mb_strtoupper(trim($_POST['cidade'] ?? '')),
            'estado' => mb_strtoupper(trim($_POST['estado'] ?? '')),
            'rg' => preg_replace('/\D/', '', $_POST['rg'] ?? ''),
            'cpf' => preg_replace('/\D/', '', $_POST['cpf'] ?? ''),
        ];

        if ($action === 'edit' && $cod) {
            $sql = "UPDATE tbclifor SET Tipo=?, nomeCliFor=?, apelidoCliFor=?, contatoCliFor=?, obs=?, fkCliForGp=?, email=?, celular=?, telefone=?, cep=?, endereco=?, numero=?, complemento=?, bairro=?, cidade=?, estado=?, rg=?, cpf=? WHERE codCliFor=?";
            $dados[] = $cod;
            $db->prepare($sql)->execute(array_values($dados));
            flashMessage('success', 'Registro atualizado com sucesso!');
            redirect('clifor.php');
        }

        if ($action === 'add') {
            $codCliFor = trim($_POST['codCliFor']);
            if (empty($codCliFor)) {
                throw new Exception('O código é obrigatório.');
            }
            $stmt = $db->prepare("SELECT COUNT(*) FROM tbclifor WHERE codCliFor=?");
            $stmt->execute([$codCliFor]);
            if ($stmt->fetchColumn() > 0) {
                throw new Exception('Este código já existe.');
            }
            $dados['codCliFor'] = $codCliFor;
            $sql = "INSERT INTO tbclifor (codCliFor, Tipo, nomeCliFor, apelidoCliFor, contatoCliFor, obs, fkCliForGp, email, celular, telefone, cep, endereco, numero, complemento, bairro, cidade, estado, rg, cpf) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            $stmt = $db->prepare($sql);
            $stmt->execute([
                $dados['codCliFor'], $dados['Tipo'], $dados['nomeCliFor'], $dados['apelidoCliFor'],
                $dados['contatoCliFor'], $dados['obs'], $dados['fkCliForGp'], $dados['email'],
                $dados['celular'], $dados['telefone'], $dados['cep'], $dados['endereco'],
                $dados['numero'], $dados['complemento'], $dados['bairro'], $dados['cidade'],
                $dados['estado'], $dados['rg'], $dados['cpf']
            ]);
            flashMessage('success', 'Registro cadastrado com sucesso!');
            redirect('clifor.php');
        }
    } catch (Exception $e) {
        flashMessage('danger', 'Erro: ' . $e->getMessage());
    }
}

if ($action === 'delete' && $cod) {
    try {
        $db->prepare("DELETE FROM tbclifor WHERE codCliFor=?")->execute([$cod]);
        flashMessage('success', 'Registro excluído com sucesso!');
    } catch (Exception $e) {
        flashMessage('danger', 'Não é possível excluir: ' . $e->getMessage());
    }
    redirect('clifor.php');
}

$title = 'Clientes e Fornecedores';
include '../includes/header.php';

$stmt = $db->query("SELECT c.*, g.nome_C as nomeConta FROM tbclifor c LEFT JOIN gpprincipal g ON c.fkCliForGp = g.cod_Geral ORDER BY c.nomeCliFor");
$registros = $stmt->fetchAll();

$editReg = null;
if ($action === 'edit' && $cod) {
    $stmt = $db->prepare("SELECT * FROM tbclifor WHERE codCliFor=?");
    $stmt->execute([$cod]);
    $editReg = $stmt->fetch();
}

$planos = $db->query("SELECT cod_Geral, nome_C FROM gpprincipal ORDER BY cod_Geral")->fetchAll();
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-people"></i> Clientes e Fornecedores</h4>
    <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#modalCliFor">
        <i class="bi bi-plus-lg"></i> Novo
    </button>
</div>

<div class="card">
    <div class="card-body">
        <div class="table-responsive">
            <table class="table datatable table-hover">
                <thead>
                    <tr>
                        <th>Código</th>
                        <th>Tipo</th>
                        <th>Nome</th>
                        <th>Apelido</th>
                        <th>Cidade</th>
                        <th>Contato</th>
                        <th width="120">Ações</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($registros as $r): ?>
                    <tr>
                        <td><code><?= htmlspecialchars($r['codCliFor'] ?? '') ?></code></td>
                        <td><?= ($r['Tipo'] ?? '') === 'C' ? '<span class="badge bg-info">Cliente</span>' : '<span class="badge bg-warning text-dark">Fornecedor</span>' ?></td>
                        <td><?= htmlspecialchars($r['nomeCliFor'] ?? '') ?></td>
                        <td><?= htmlspecialchars($r['apelidoCliFor'] ?? '') ?></td>
                        <td><?= htmlspecialchars($r['cidade'] ?? '') ?></td>
                        <td><?= htmlspecialchars($r['contatoCliFor'] ?? '') ?></td>
                        <td>
                            <a href="?action=edit&cod=<?= urlencode($r['codCliFor']) ?>" class="btn btn-sm btn-outline-primary"><i class="bi bi-pencil"></i></a>
                            <a href="?action=delete&cod=<?= urlencode($r['codCliFor']) ?>" class="btn btn-sm btn-outline-danger" data-confirm="Excluir este registro?"><i class="bi bi-trash"></i></a>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div class="modal fade" id="modalCliFor" tabindex="-1" data-bs-backdrop="static">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <form method="POST" action="?action=<?= $editReg ? 'edit&cod=' . urlencode($editReg['codCliFor']) : 'add' ?>">
                <div class="modal-header">
                    <h5 class="modal-title"><?= $editReg ? 'Editar' : 'Novo' ?> Cadastro</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="row">
                        <div class="col-md-4 mb-3">
                            <label class="form-label required">Tipo</label>
                            <select name="Tipo" class="form-select" required>
                                <option value="C" <?= ($editReg['Tipo'] ?? '') === 'C' ? 'selected' : '' ?>>Cliente</option>
                                <option value="F" <?= ($editReg['Tipo'] ?? '') === 'F' ? 'selected' : '' ?>>Fornecedor</option>
                            </select>
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label required">Código</label>
                            <?php if ($editReg): ?>
                            <input type="text" class="form-control" value="<?= htmlspecialchars($editReg['codCliFor']) ?>" readonly>
                            <?php else: ?>
                            <input type="text" name="codCliFor" class="form-control" placeholder="Ex: C1001 ou F005" required autocomplete="off">
                            <?php endif; ?>
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label required">Nome</label>
                            <input type="text" name="nomeCliFor" class="form-control" value="<?= htmlspecialchars($editReg['nomeCliFor'] ?? '') ?>" required>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Apelido</label>
                            <input type="text" name="apelidoCliFor" class="form-control" value="<?= htmlspecialchars($editReg['apelidoCliFor'] ?? '') ?>">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Contato</label>
                            <input type="text" name="contatoCliFor" class="form-control" value="<?= htmlspecialchars($editReg['contatoCliFor'] ?? '') ?>">
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">CPF</label>
                            <input type="text" name="cpf" class="form-control" value="<?= $editReg ? formatCpfCnpj($editReg['cpf']) : '' ?>">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">RG</label>
                            <input type="text" name="rg" class="form-control" value="<?= htmlspecialchars($editReg['rg'] ?? '') ?>">
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-4 mb-3">
                            <label class="form-label">CEP</label>
                            <input type="text" name="cep" class="form-control" value="<?= $editReg ? formatCep($editReg['cep']) : '' ?>">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Endereço</label>
                            <input type="text" name="endereco" class="form-control" value="<?= htmlspecialchars($editReg['endereco'] ?? '') ?>">
                        </div>
                        <div class="col-md-2 mb-3">
                            <label class="form-label">Número</label>
                            <input type="text" name="numero" class="form-control" value="<?= htmlspecialchars($editReg['numero'] ?? '') ?>">
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-4 mb-3">
                            <label class="form-label">Complemento</label>
                            <input type="text" name="complemento" class="form-control" value="<?= htmlspecialchars($editReg['complemento'] ?? '') ?>">
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label">Bairro</label>
                            <input type="text" name="bairro" class="form-control" value="<?= htmlspecialchars($editReg['bairro'] ?? '') ?>">
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label">Cidade</label>
                            <input type="text" name="cidade" class="form-control" value="<?= htmlspecialchars($editReg['cidade'] ?? '') ?>">
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Estado</label>
                            <select name="estado" class="form-select">
                                <option value="">Selecione...</option>
                                <?php
                                $estados = ['AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO'];
                                $estadoAtual = $editReg['estado'] ?? '';
                                foreach ($estados as $uf): ?>
                                <option value="<?= $uf ?>" <?= $estadoAtual === $uf ? 'selected' : '' ?>><?= $uf ?></option>
                                <?php endforeach; ?>
                            </select>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label required">Classificação (Plano de Contas)</label>
                            <select name="fkCliForGp" class="form-select select2" required>
                                <option value="">Selecione...</option>
                                <?php foreach ($planos as $p): ?>
                                <option value="<?= htmlspecialchars($p['cod_Geral']) ?>" <?= ($editReg['fkCliForGp'] ?? '') === $p['cod_Geral'] ? 'selected' : '' ?>>
                                    <?= htmlspecialchars($p['cod_Geral'] . ' - ' . $p['nome_C']) ?>
                                </option>
                                <?php endforeach; ?>
                            </select>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-4 mb-3">
                            <label class="form-label">Celular</label>
                            <input type="text" name="celular" class="form-control" value="<?= $editReg ? formatTelefone($editReg['celular']) : '' ?>">
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label">Telefone</label>
                            <input type="text" name="telefone" class="form-control" value="<?= $editReg ? formatTelefone($editReg['telefone']) : '' ?>">
                        </div>
                        <div class="col-md-4 mb-3">
                            <label class="form-label">Email</label>
                            <input type="email" name="email" class="form-control" value="<?= htmlspecialchars($editReg['email'] ?? '') ?>">
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Observações</label>
                        <textarea name="obs" class="form-control" rows="2"><?= htmlspecialchars($editReg['obs'] ?? '') ?></textarea>
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
<?php if ($editReg): ?>
<script>
document.addEventListener('DOMContentLoaded', function() {
    new bootstrap.Modal(document.getElementById('modalCliFor')).show();
});
</script>
<?php endif; ?>
