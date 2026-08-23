<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$db = Database::getInstance()->getConnection();

// ====== PROCESSAMENTO POST (CRUD em testbirth — espelha Alart_Body.java) ======
$msg = '';
$msgType = 'danger';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $acao = $_POST['acao'] ?? '';

    if ($acao === 'adicionar') {
        $nome = trim($_POST['nome'] ?? '');
        $bdate = $_POST['bdate'] ?? '';
        if ($nome && $bdate) {
            $st = $db->prepare("INSERT INTO testbirth (Name, Bdate) VALUES (?, ?)");
            $st->execute([$nome, $bdate]);
            $msg = 'Aniversariante "' . $nome . '" adicionado!';
            $msgType = 'success';
        } else {
            $msg = 'Informe o nome e a data de nascimento!';
        }
    }

    if ($acao === 'excluir') {
        $nome = trim($_POST['nome'] ?? '');
        $st = $db->prepare("DELETE FROM testbirth WHERE Name = ?");
        $st->execute([$nome]);
        $msg = 'Registro excluído.';
        $msgType = 'success';
    }
}

// ====== CONSULTA ======
// Lista completa ordenada por mês/dia (igual ao Java: ORDER BY MONTH(Bdate), DAY(Bdate))
$todos = $db->query("SELECT Name, Bdate FROM testbirth ORDER BY MONTH(Bdate), DAY(Bdate)")->fetchAll();

// Aniversariantes do dia (alerta — igual ao alart() do Java: compara MM-DD com hoje)
$hoje = new DateTime('today');
$hojeMMDD = $hoje->format('m-d');
$doDia = array_filter($todos, fn($t) => date('m-d', strtotime($t['Bdate'])) === $hojeMMDD);

// Próximos 7 dias
$proximos = [];
for ($i = 1; $i <= 7; $i++) {
    $d = (clone $hoje)->modify("+$i day");
    $proximos = array_merge($proximos, array_filter(
        $todos,
        fn($t) => date('m-d', strtotime($t['Bdate'])) === $d->format('m-d')
    ));
}

$title = 'Aniversariantes';
include '../includes/header.php';
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-balloon-heart"></i> Aniversariantes</h4>
</div>

<?php if ($msg): ?>
<div class="alert alert-<?= $msgType ?> alert-dismissible fade show">
    <?= htmlspecialchars($msg) ?>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>
<?php endif; ?>

<div class="row mb-3">
    <!-- Alerta do dia -->
    <div class="col-md-4">
        <div class="card <?= $doDia ? 'border-danger' : '' ?>">
            <div class="card-header"><i class="bi bi-bell"></i> Aniversariantes de Hoje (<?= $hoje->format('d/m') ?>)</div>
            <div class="card-body">
                <?php if ($doDia): ?>
                    <ul class="list-unstyled mb-0">
                        <?php foreach ($doDia as $a): ?>
                        <li class="mb-1"><i class="bi bi-balloon-heart text-danger"></i>
                            <strong><?= htmlspecialchars($a['Name']) ?></strong>
                            <span class="text-muted">(<?= htmlspecialchars($a['Bdate']) ?>)</span>
                        </li>
                        <?php endforeach; ?>
                    </ul>
                <?php else: ?>
                    <p class="text-muted mb-0">Nenhum aniversariante hoje. 🎈</p>
                <?php endif; ?>
            </div>
        </div>
    </div>
    <!-- Próximos 7 dias -->
    <div class="col-md-4">
        <div class="card">
            <div class="card-header"><i class="bi bi-calendar-heart"></i> Próximos 7 Dias</div>
            <div class="card-body">
                <?php if ($proximos): ?>
                    <ul class="list-unstyled mb-0">
                        <?php foreach ($proximos as $a): ?>
                        <li class="mb-1">
                            <i class="bi bi-gift text-warning"></i>
                            <strong><?= htmlspecialchars($a['Name']) ?></strong>
                            <span class="text-muted">(<?= date('d/m', strtotime($a['Bdate'])) ?>)</span>
                        </li>
                        <?php endforeach; ?>
                    </ul>
                <?php else: ?>
                    <p class="text-muted mb-0">Nenhum aniversário nos próximos 7 dias.</p>
                <?php endif; ?>
            </div>
        </div>
    </div>
    <!-- Cadastro -->
    <div class="col-md-4">
        <div class="card">
            <div class="card-header"><i class="bi bi-person-plus"></i> Cadastrar</div>
            <div class="card-body">
                <form method="POST">
                    <input type="hidden" name="acao" value="adicionar">
                    <div class="mb-2">
                        <label class="form-label">Nome</label>
                        <input type="text" name="nome" class="form-control" required>
                    </div>
                    <div class="mb-2">
                        <label class="form-label">Data de Nascimento</label>
                        <input type="date" name="bdate" class="form-control" required>
                    </div>
                    <button type="submit" class="btn btn-primary w-100"><i class="bi bi-plus-circle"></i> Adicionar</button>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- Tabela completa -->
<div class="card">
    <div class="card-header"><i class="bi bi-table"></i> Todos os Aniversariantes (<?= count($todos) ?>)</div>
    <div class="card-body">
        <div class="table-responsive">
            <table class="table table-sm table-hover">
                <thead>
                    <tr>
                        <th>Nome</th>
                        <th>Data de Nascimento</th>
                        <th>Mês</th>
                        <th class="text-end">Ação</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($todos as $a): ?>
                    <tr>
                        <td>
                            <?php if (date('m-d', strtotime($a['Bdate'])) === $hojeMMDD): ?>
                                <i class="bi bi-balloon-heart text-danger" title="Aniversariante hoje!"></i>
                            <?php endif; ?>
                            <?= htmlspecialchars($a['Name']) ?>
                        </td>
                        <td><?= htmlspecialchars($a['Bdate']) ?></td>
                        <td><?= date('m/Y', strtotime($a['Bdate'])) ?></td>
                        <td class="text-end">
                            <form method="POST" class="d-inline" onsubmit="return confirm('Excluir <?= htmlspecialchars($a['Name'], ENT_QUOTES) ?>?')">
                                <input type="hidden" name="acao" value="excluir">
                                <input type="hidden" name="nome" value="<?= htmlspecialchars($a['Name']) ?>">
                                <button type="submit" class="btn btn-sm btn-outline-danger" title="Excluir"><i class="bi bi-trash"></i></button>
                            </form>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </div>
</div>

<?php include '../includes/footer.php'; ?>
