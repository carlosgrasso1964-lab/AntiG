<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

// Pasta de notas FORA do docroot (não acessível via URL) — espelha o BlocNotas do Java
$notasDir = dirname(__DIR__, 2) . '/notas';
if (!is_dir($notasDir)) {
    @mkdir($notasDir, 0777, true);
}

function nomeArquivoSeguro($nome) {
    $nome = trim($nome);
    $nome = preg_replace('/[^A-Za-z0-9 _.\-]/u', '', $nome);
    $nome = preg_replace('/\.+$/', '', $nome);
    if ($nome === '') $nome = 'nota';
    return $nome;
}

$msg = '';
$msgType = 'danger';
$conteudoAtual = '';
$arquivoAtual = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $acao = $_POST['acao'] ?? '';

    if ($acao === 'salvar') {
        $nome = nomeArquivoSeguro($_POST['nome'] ?? '');
        $conteudo = $_POST['conteudo'] ?? '';
        if (!preg_match('/\.txt$/i', $nome)) $nome .= '.txt';
        $caminho = $notasDir . '/' . $nome;
        if (@file_put_contents($caminho, $conteudo) !== false) {
            $msg = 'Nota "' . $nome . '" salva com sucesso!';
            $msgType = 'success';
            $conteudoAtual = $conteudo;
            $arquivoAtual = $nome;
        } else {
            $msg = 'Erro ao salvar a nota!';
        }
    }

    if ($acao === 'abrir') {
        $nome = basename($_POST['arquivo'] ?? '');
        $caminho = $notasDir . '/' . $nome;
        if (preg_match('/\.txt$/i', $nome) && file_exists($caminho)) {
            $conteudoAtual = @file_get_contents($caminho) ?: '';
            $arquivoAtual = $nome;
        } else {
            $msg = 'Arquivo não encontrado!';
        }
    }

    if ($acao === 'excluir') {
        $nome = basename($_POST['arquivo'] ?? '');
        $caminho = $notasDir . '/' . $nome;
        if (preg_match('/\.txt$/i', $nome) && file_exists($caminho)) {
            @unlink($caminho);
            $msg = 'Nota "' . $nome . '" excluída.';
            $msgType = 'success';
        }
    }

    if ($acao === 'novo') {
        $conteudoAtual = '';
        $arquivoAtual = '';
    }
}

// Lista notas existentes
$notas = [];
if (is_dir($notasDir)) {
    foreach (glob($notasDir . '/*.txt') ?: [] as $f) {
        $notas[] = [
            'nome' => basename($f),
            'tamanho' => filesize($f),
            'data' => date('d/m/Y H:i', filemtime($f)),
        ];
    }
    usort($notas, fn($a, $b) => strcmp($b['data'], $a['data']));
}

$title = 'Bloco de Notas';
include '../includes/header.php';
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-journal-text"></i> Bloco de Notas</h4>
</div>

<?php if ($msg): ?>
<div class="alert alert-<?= $msgType ?> alert-dismissible fade show">
    <?= htmlspecialchars($msg) ?>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>
<?php endif; ?>

<div class="row">
    <!-- ===== EDITOR ===== -->
    <div class="col-lg-8">
        <div class="card">
            <div class="card-header"><i class="bi bi-pencil-square"></i> Editor</div>
            <div class="card-body">
                <form method="POST">
                    <input type="hidden" name="acao" value="salvar">
                    <div class="mb-2">
                        <label class="form-label">Nome da nota</label>
                        <input type="text" name="nome" class="form-control" placeholder="minha_nota" required
                               value="<?= htmlspecialchars($arquivoAtual ? preg_replace('/\.txt$/i', '', $arquivoAtual) : '') ?>">
                    </div>
                    <div class="mb-2">
                        <label class="form-label">Conteúdo</label>
                        <textarea name="conteudo" class="form-control" rows="16" style="font-family: Consolas, monospace;"
                                  placeholder="Escreva aqui suas anotações..."><?= htmlspecialchars($conteudoAtual) ?></textarea>
                    </div>
                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-success"><i class="bi bi-save"></i> Salvar</button>
                        <button type="submit" class="btn btn-outline-secondary" formaction="?acao=novo"
                                name="acao" value="novo" formnovalidate><i class="bi bi-file-earmark"></i> Novo</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- ===== LISTA DE NOTAS ===== -->
    <div class="col-lg-4">
        <div class="card">
            <div class="card-header"><i class="bi bi-folder2-open"></i> Notas Salvas (<?= count($notas) ?>)</div>
            <div class="card-body">
                <?php if (empty($notas)): ?>
                    <p class="text-muted mb-0">Nenhuma nota salva ainda.</p>
                <?php else: ?>
                <div class="list-group">
                    <?php foreach ($notas as $n): ?>
                    <div class="list-group-item d-flex justify-content-between align-items-center">
                        <div class="text-truncate">
                            <i class="bi bi-file-earmark-text"></i>
                            <form method="POST" class="d-inline">
                                <input type="hidden" name="acao" value="abrir">
                                <input type="hidden" name="arquivo" value="<?= htmlspecialchars($n['nome']) ?>">
                                <button type="submit" class="btn btn-link btn-sm p-0 align-baseline text-decoration-none">
                                    <?= htmlspecialchars($n['nome']) ?>
                                </button>
                            </form>
                            <div class="small text-muted"><?= $n['data'] ?> · <?= number_format($n['tamanho'] / 1024, 1, ',', '.') ?> KB</div>
                        </div>
                        <form method="POST" class="d-inline" onsubmit="return confirm('Excluir esta nota?')">
                            <input type="hidden" name="acao" value="excluir">
                            <input type="hidden" name="arquivo" value="<?= htmlspecialchars($n['nome']) ?>">
                            <button type="submit" class="btn btn-sm btn-outline-danger" title="Excluir"><i class="bi bi-trash"></i></button>
                        </form>
                    </div>
                    <?php endforeach; ?>
                </div>
                <?php endif; ?>
            </div>
        </div>
    </div>
</div>

<?php include '../includes/footer.php'; ?>
