<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$db = Database::getInstance()->getConnection();

// Cria a tabela de tarefas sob demanda (igual ao padrão do Mercantil)
$db->exec("CREATE TABLE IF NOT EXISTS tb_tarefas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    data DATE NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    concluida TINYINT(1) NOT NULL DEFAULT 0,
    criado_em DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

// ====== PROCESSAMENTO POST (CRUD de tarefas) ======
$msg = '';
$msgType = 'danger';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $acao = $_POST['acao'] ?? '';

    if ($acao === 'adicionar') {
        $data = $_POST['data'] ?? '';
        $descricao = trim($_POST['descricao'] ?? '');
        if ($data && $descricao) {
            $st = $db->prepare("INSERT INTO tb_tarefas (data, descricao) VALUES (?, ?)");
            $st->execute([$data, $descricao]);
            $msg = 'Tarefa adicionada!';
            $msgType = 'success';
        } else {
            $msg = 'Informe a data e a descrição da tarefa!';
        }
    }

    if ($acao === 'alternar') {
        $id = (int)($_POST['id'] ?? 0);
        $st = $db->prepare("UPDATE tb_tarefas SET concluida = 1 - concluida WHERE id = ?");
        $st->execute([$id]);
    }

    if ($acao === 'excluir') {
        $id = (int)($_POST['id'] ?? 0);
        $st = $db->prepare("DELETE FROM tb_tarefas WHERE id = ?");
        $st->execute([$id]);
        $msg = 'Tarefa excluída.';
        $msgType = 'success';
    }
}

// ====== NAVEGAÇÃO DO CALENDÁRIO ======
$hoje = new DateTime('today');
$ano = isset($_GET['ano']) ? (int)$_GET['ano'] : (int)$hoje->format('Y');
$mes = isset($_GET['mes']) ? (int)$_GET['mes'] : (int)$hoje->format('n');
if ($mes < 1) { $mes = 1; } if ($mes > 12) { $mes = 12; }
if ($ano < 1900) { $ano = 1900; } if ($ano > 2100) { $ano = 2100; }

$primeiroDia = new DateTime(sprintf('%04d-%02d-01', $ano, $mes));
$ultimoDia = new DateTime($primeiroDia->format('Y-m-t'));
$diasNoMes = (int)$ultimoDia->format('j');
$diaSemanaInicio = (int)$primeiroDia->format('N'); // 1=Seg ... 7=Dom

// Navegação
$prev = (clone $primeiroDia)->modify('-1 month');
$next = (clone $primeiroDia)->modify('+1 month');

$mesesNomes = [1 => 'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
    'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
$diasNomes = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'];

// Busca tarefas do mês (todas de uma vez, agrupadas por dia)
$tarefasPorDia = [];
$st = $db->prepare("SELECT id, data, descricao, concluida FROM tb_tarefas
    WHERE data BETWEEN ? AND ? ORDER BY data, id");
$st->execute([$primeiroDia->format('Y-m-d'), $ultimoDia->format('Y-m-d')]);
foreach ($st->fetchAll() as $t) {
    $tarefasPorDia[$t['data']][] = $t;
}

$title = 'Calendário To-Do';
include '../includes/header.php';
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-calendar3"></i> Calendário To-Do</h4>
</div>

<?php if ($msg): ?>
<div class="alert alert-<?= $msgType ?> alert-dismissible fade show">
    <?= htmlspecialchars($msg) ?>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>
<?php endif; ?>

<div class="row">
    <!-- ===== CALENDÁRIO ===== -->
    <div class="col-lg-8">
        <div class="card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <a href="?ano=<?= $prev->format('Y') ?>&mes=<?= $prev->format('n') ?>" class="btn btn-sm btn-outline-secondary"><i class="bi bi-chevron-left"></i></a>
                <h5 class="mb-0"><?= $mesesNomes[$mes] ?> <?= $ano ?></h5>
                <a href="?ano=<?= $next->format('Y') ?>&mes=<?= $next->format('n') ?>" class="btn btn-sm btn-outline-secondary"><i class="bi bi-chevron-right"></i></a>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-bordered text-center calendario-tabela">
                        <thead>
                            <tr>
                                <?php foreach ($diasNomes as $d): ?>
                                <th class="bg-secondary text-white"><?= $d ?></th>
                                <?php endforeach; ?>
                            </tr>
                        </thead>
                        <tbody>
                            <?php
                            // Células em branco antes do dia 1 (calendário inicia na segunda)
                            $celulas = ($diaSemanaInicio - 1);
                            $diaAtual = 1;
                            echo '<tr>';
                            for ($i = 0; $i < $celulas; $i++) {
                                echo '<td class="bg-light"></td>';
                            }
                            $dataHojeStr = $hoje->format('Y-m-d');
                            while ($diaAtual <= $diasNoMes) {
                                if (($celulas + $diaAtual - 1) % 7 == 0 && $diaAtual > 1) {
                                    echo '</tr><tr>';
                                }
                                $dataStr = sprintf('%04d-%02d-%02d', $ano, $mes, $diaAtual);
                                $qtde = isset($tarefasPorDia[$dataStr]) ? count($tarefasPorDia[$dataStr]) : 0;
                                $pendentes = isset($tarefasPorDia[$dataStr])
                                    ? count(array_filter($tarefasPorDia[$dataStr], fn($t) => !$t['concluida']))
                                    : 0;
                                $class = $dataStr === $dataHojeStr ? 'table-primary' : '';
                                ?>
                                <td class="calendario-dia <?= $class ?>" style="height:90px; vertical-align:top; cursor:pointer"
                                    onclick="selecionarDia('<?= $dataStr ?>', <?= $diaAtual ?>)">
                                    <div class="d-flex justify-content-between">
                                        <strong><?= $diaAtual ?></strong>
                                        <?php if ($pendentes > 0): ?>
                                        <span class="badge bg-warning text-dark"><?= $pendentes ?></span>
                                        <?php endif; ?>
                                    </div>
                                    <?php if ($qtde > 0): ?>
                                    <div class="text-start small">
                                        <?php foreach (array_slice($tarefasPorDia[$dataStr], 0, 2) as $t): ?>
                                        <div class="<?= $t['concluida'] ? 'text-decoration-line-through text-muted' : '' ?>">
                                            <i class="bi bi-check2-square"></i> <?= htmlspecialchars(mb_substr($t['descricao'], 0, 18)) ?><?= mb_strlen($t['descricao']) > 18 ? '…' : '' ?>
                                        </div>
                                        <?php endforeach; ?>
                                        <?php if ($qtde > 2): ?>
                                        <div class="text-muted">+<?= $qtde - 2 ?> mais</div>
                                        <?php endif; ?>
                                    </div>
                                    <?php endif; ?>
                                </td>
                                <?php
                                $diaAtual++;
                            }
                            // Células finais
                            while (($celulas + $diasNoMes) % 7 != 0) {
                                echo '<td class="bg-light"></td>';
                                $celulas++;
                            }
                            echo '</tr>';
                            ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <!-- ===== PAINEL DE TAREFAS ===== -->
    <div class="col-lg-4">
        <div class="card">
            <div class="card-header"><i class="bi bi-list-check"></i> Tarefas do Dia</div>
            <div class="card-body">
                <h6 id="tituloDia" class="mb-3">Selecione um dia no calendário</h6>

                <form method="POST" class="mb-3">
                    <input type="hidden" name="acao" value="adicionar">
                    <input type="hidden" name="data" id="inputData" value="">
                    <div class="mb-2">
                        <input type="text" name="descricao" id="inputDescricao" class="form-control" placeholder="Nova tarefa..." maxlength="255" required>
                    </div>
                    <button type="submit" class="btn btn-primary w-100" id="btnAdd" disabled>
                        <i class="bi bi-plus-circle"></i> Adicionar Tarefa
                    </button>
                </form>

                <div id="listaTarefas" class="list-group">
                    <p class="text-muted mb-0">Clique em um dia para ver/adicionar tarefas.</p>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
// Tarefas do mês carregadas do PHP (JSON)
const tarefasMes = <?= json_encode($tarefasPorDia, JSON_UNESCAPED_UNICODE) ?>;
const hojeStr = '<?= $dataHojeStr ?>';

function formatarDataBR(dataStr) {
    const p = dataStr.split('-');
    return p[2] + '/' + p[1] + '/' + p[0];
}

function selecionarDia(dataStr, dia) {
    document.getElementById('inputData').value = dataStr;
    document.getElementById('inputDescricao').value = '';
    document.getElementById('btnAdd').disabled = false;

    const meses = ['', 'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
        'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'];
    const p = dataStr.split('-');
    document.getElementById('tituloDia').textContent = dia + ' de ' + meses[parseInt(p[1])] + ' de ' + p[0] + ' (' + formatarDataBR(dataStr) + ')';

    const lista = document.getElementById('listaTarefas');
    const tarefas = tarefasMes[dataStr] || [];
    if (tarefas.length === 0) {
        lista.innerHTML = '<p class="text-muted mb-0">Nenhuma tarefa para este dia.</p>';
        return;
    }
    lista.innerHTML = tarefas.map(t => {
        const risco = t.concluida ? 'text-decoration-line-through text-muted' : '';
        const icone = t.concluida ? 'bi-check-circle-fill text-success' : 'bi-circle text-secondary';
        return '<div class="list-group-item d-flex justify-content-between align-items-center ' + risco + '">'
            + '<span><i class="bi ' + icone + '"></i> ' + t.descricao.replace(/</g, '&lt;') + '</span>'
            + '<span class="d-flex gap-1">'
            + '<form method="POST" class="d-inline">'
            + '<input type="hidden" name="acao" value="alternar">'
            + '<input type="hidden" name="id" value="' + t.id + '">'
            + '<button type="submit" class="btn btn-sm btn-outline-success" title="Concluir/Reabrir"><i class="bi bi-check2"></i></button>'
            + '</form>'
            + '<form method="POST" class="d-inline" onsubmit="return confirm(\'Excluir tarefa?\')">'
            + '<input type="hidden" name="acao" value="excluir">'
            + '<input type="hidden" name="id" value="' + t.id + '">'
            + '<button type="submit" class="btn btn-sm btn-outline-danger" title="Excluir"><i class="bi bi-trash"></i></button>'
            + '</form>'
            + '</span></div>';
    }).join('');
}

// Seleciona o dia de hoje ao carregar
if (tarefasMes[hojeStr] !== undefined) {
    const p = hojeStr.split('-');
    selecionarDia(hojeStr, parseInt(p[2]));
}
</script>

<?php include '../includes/footer.php'; ?>
