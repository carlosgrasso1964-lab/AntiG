<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

// Pastas onde os PDFs podem estar (espelha o TelaJFileChosse do Java, que abria a pasta "outprt")
$pastasPDF = [
    'Relatórios (web)'   => dirname(__DIR__, 2) . '/pdfs',        // C:\wamp64\www\jfip-web\web\pdfs
    'Saída do jFIP (outprt)' => 'C:/Users/Carlos/Documents/AntiG/jFIP/outprt',
    'Saída do jFinNslt (outprt)' => 'C:/Users/Carlos/Documents/AntiG/jFinNslt/outprt',
];

// Garante que a pasta web de PDFs exista
if (!is_dir($pastasPDF['Relatórios (web)'])) {
    @mkdir($pastasPDF['Relatórios (web)'], 0777, true);
}

// ====== ABRIR PDF (serve via PHP readfile — pasta fica fora do docroot, sem 404) ======
if (isset($_GET['abrir'])) {
    $nome = basename($_GET['abrir']);
    $pastaIdx = (int)($_GET['pasta'] ?? 0);
    $dirs = array_values($pastasPDF);
    $dir = $dirs[$pastaIdx] ?? $dirs[0];
    $caminho = $dir . '/' . $nome;
    if (preg_match('/\.pdf$/i', $nome) && file_exists($caminho)) {
        header('Content-Type: application/pdf');
        header('Content-Disposition: inline; filename="' . $nome . '"');
        header('Content-Length: ' . filesize($caminho));
        readfile($caminho);
        exit;
    }
    http_response_code(404);
    exit('Arquivo PDF não encontrado.');
}

$msg = '';
$msgType = 'danger';

// ====== UPLOAD de PDF (opcional — para quem quiser colocar PDFs pelo navegador) ======
if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['acao'] ?? '') === 'upload') {
    if (!isset($_FILES['arquivo_pdf']) || $_FILES['arquivo_pdf']['error'] !== UPLOAD_ERR_OK) {
        $msg = 'Selecione um arquivo PDF válido!';
    } else {
        $ext = strtolower(pathinfo($_FILES['arquivo_pdf']['name'], PATHINFO_EXTENSION));
        if ($ext !== 'pdf') {
            $msg = 'O arquivo deve ter extensão .pdf!';
        } else {
            $destino = $pastasPDF['Relatórios (web)'] . '/' . basename($_FILES['arquivo_pdf']['name']);
            if (move_uploaded_file($_FILES['arquivo_pdf']['tmp_name'], $destino)) {
                $msg = 'PDF "' . basename($_FILES['arquivo_pdf']['name']) . '" enviado!';
                $msgType = 'success';
            } else {
                $msg = 'Falha ao copiar o arquivo!';
            }
        }
    }
}

// ====== EXCLUIR PDF ======
if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['acao'] ?? '') === 'excluir') {
    $nome = basename($_POST['arquivo'] ?? '');
    $caminho = $pastasPDF['Relatórios (web)'] . '/' . $nome;
    if (preg_match('/\.pdf$/i', $nome) && file_exists($caminho)) {
        @unlink($caminho);
        $msg = 'PDF "' . $nome . '" excluído.';
        $msgType = 'success';
    }
}

// ====== LISTA PDFs ======
$lista = [];
$pastaIdx = 0;
foreach ($pastasPDF as $label => $dir) {
    $itens = [];
    if (is_dir($dir)) {
        foreach (glob($dir . '/*.pdf') ?: [] as $f) {
            $itens[] = [
                'nome' => basename($f),
                'tamanho' => filesize($f),
                'data' => date('d/m/Y H:i', filemtime($f)),
                'caminho' => str_replace('\\', '/', $f),
                'pasta' => $label,
                'idx' => $pastaIdx,
            ];
        }
        usort($itens, fn($a, $b) => strcmp($b['data'], $a['data']));
    }
    $lista[$label] = $itens;
    $pastaIdx++;
}

$title = 'Abrir PDF';
include '../includes/header.php';
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-file-earmark-pdf"></i> Abrir PDF</h4>
</div>

<?php if ($msg): ?>
<div class="alert alert-<?= $msgType ?> alert-dismissible fade show">
    <?= htmlspecialchars($msg) ?>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>
<?php endif; ?>

<!-- Upload -->
<div class="card mb-3">
    <div class="card-header"><i class="bi bi-cloud-arrow-up"></i> Enviar PDF para a pasta de relatórios</div>
    <div class="card-body">
        <form method="POST" enctype="multipart/form-data" class="row g-2 align-items-center">
            <input type="hidden" name="acao" value="upload">
            <div class="col-md-6">
                <input type="file" name="arquivo_pdf" class="form-control" accept=".pdf" required>
            </div>
            <div class="col-md-3">
                <button type="submit" class="btn btn-primary"><i class="bi bi-cloud-arrow-up"></i> Enviar</button>
            </div>
            <div class="col-md-3">
                <small class="text-muted">Limite: <?= ini_get('upload_max_filesize') ?></small>
            </div>
        </form>
    </div>
</div>

<?php foreach ($lista as $label => $itens): ?>
<div class="card mb-3">
    <div class="card-header"><i class="bi bi-folder2-open"></i> <?= htmlspecialchars($label) ?> (<?= count($itens) ?>)</div>
    <div class="card-body">
        <?php if (empty($itens)): ?>
            <p class="text-muted mb-0">Nenhum PDF nesta pasta.</p>
        <?php else: ?>
        <div class="table-responsive">
            <table class="table table-sm table-hover">
                <thead>
                    <tr>
                        <th>Arquivo</th>
                        <th class="text-end">Tamanho</th>
                        <th>Data</th>
                        <th class="text-end">Ações</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($itens as $p): ?>
                    <tr>
                        <td><i class="bi bi-file-earmark-pdf text-danger"></i> <?= htmlspecialchars($p['nome']) ?></td>
                        <td class="text-end"><?= number_format($p['tamanho'] / 1024, 1, ',', '.') ?> KB</td>
                        <td><?= $p['data'] ?></td>
                        <td class="text-end">
                            <!-- Abre via PHP readfile (inline) — funciona mesmo com a pasta fora do docroot -->
                            <a href="?abrir=<?= urlencode($p['nome']) ?>&pasta=<?= $p['idx'] ?>" target="_blank" class="btn btn-sm btn-outline-danger" title="Abrir PDF">
                                <i class="bi bi-eye"></i>
                            </a>
                            <?php if ($label === 'Relatórios (web)'): ?>
                            <form method="POST" class="d-inline" onsubmit="return confirm('Excluir este PDF?')">
                                <input type="hidden" name="acao" value="excluir">
                                <input type="hidden" name="arquivo" value="<?= htmlspecialchars($p['nome']) ?>">
                                <button type="submit" class="btn btn-sm btn-outline-secondary" title="Excluir"><i class="bi bi-trash"></i></button>
                            </form>
                            <?php endif; ?>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
        <?php endif; ?>
    </div>
</div>
<?php endforeach; ?>

<?php include '../includes/footer.php'; ?>
