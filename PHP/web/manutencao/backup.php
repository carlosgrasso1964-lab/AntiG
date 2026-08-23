<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$db = Database::getInstance()->getConnection();

// ====== Configuração do backup (espelha Exportar.java / Importar.java do jFIP) ======
// Pasta de backups FORA do docroot (não acessível via URL)
$backupDir = dirname(__DIR__, 2) . '/backups'; // C:\wamp64\www\jfip-web\web\backups
if (!is_dir($backupDir)) {
    @mkdir($backupDir, 0777, true);
}

// Localiza mysqldump/mysql no WAMP (fallback: PATH)
function findBin($name) {
    $candidates = [
        'C:/wamp64/bin/mysql/mysql9.1.0/bin/' . $name . '.exe',
        'C:/wamp64/bin/mysql/mysql8.4.0/bin/' . $name . '.exe',
        'C:/wamp64/bin/mysql/mysql8.3.0/bin/' . $name . '.exe',
        'C:/wamp64/bin/mysql/mysql8.0.30/bin/' . $name . '.exe',
        $name . '.exe',
    ];
    foreach ($candidates as $c) {
        if (file_exists($c)) {
            return $c;
        }
    }
    return null;
}

$mysqldump = findBin('mysqldump');
$mysqlCli  = findBin('mysql');

// Credenciais (mesmas do config/database.php)
$dbHost = getenv('DB_HOST') ?: 'localhost';
$dbUser = getenv('DB_USER') ?: 'root';
$dbPass = getenv('DB_PASSWORD') ?: '';
$dbName = 'jfin';

// ====== PROCESSAMENTO POST ======
$msg = '';
$msgType = 'danger';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $acao = $_POST['acao'] ?? '';

    // ---------- EXPORTAR (Backup) ----------
    if ($acao === 'exportar') {
        if (!$mysqldump) {
            $msg = 'mysqldump não encontrado no WAMP!';
        } else {
            $timeStamp = date('dmY_H_i_s');
            $nomeArquivo = 'mysql_backup-' . $timeStamp . '.sql';
            $destinoBackup = $backupDir . '/' . $nomeArquivo;

            // Monta comando igual ao Exportar.java (ProcessBuilder mysqldump)
            $cmd = '"' . $mysqldump . '"'
                . ' --host=' . $dbHost
                . ' --port=3306'
                . ' --user=' . $dbUser
                . ' --password=' . $dbPass
                . ' ' . $dbName
                . ' --result-file="' . $destinoBackup . '" 2>&1';
            exec($cmd, $out, $exitCode);

            if ($exitCode === 0 && file_exists($destinoBackup) && filesize($destinoBackup) > 0) {
                $msg = 'Backup realizado com sucesso: ' . $nomeArquivo . ' (' . number_format(filesize($destinoBackup) / 1024, 1, ',', '.') . ' KB)';
                $msgType = 'success';
            } else {
                $msg = 'Erro ao realizar o backup: ' . implode(' | ', array_slice($out, 0, 3));
            }
        }
    }

    // ---------- IMPORTAR (Restore) ----------
    if ($acao === 'importar') {
        if (!$mysqlCli) {
            $msg = 'mysql não encontrado no WAMP!';
        } elseif (!isset($_FILES['arquivo_sql']) || $_FILES['arquivo_sql']['error'] === UPLOAD_ERR_NO_FILE) {
            $msg = 'Selecione um arquivo .sql válido para importar!';
        } elseif ($_FILES['arquivo_sql']['error'] === UPLOAD_ERR_INI_SIZE || $_FILES['arquivo_sql']['error'] === UPLOAD_ERR_FORM_SIZE) {
            $msg = 'O arquivo excede o limite de upload do PHP (' . ini_get('upload_max_filesize') . '). Use um backup da lista abaixo ou aumente o upload_max_filesize no php.ini.';
        } elseif ($_FILES['arquivo_sql']['error'] !== UPLOAD_ERR_OK) {
            $msg = 'Erro no envio do arquivo (código ' . $_FILES['arquivo_sql']['error'] . ').';
        } else {
            $ext = strtolower(pathinfo($_FILES['arquivo_sql']['name'], PATHINFO_EXTENSION));
            if ($ext !== 'sql') {
                $msg = 'O arquivo deve ter extensão .sql!';
            } else {
                $nomeArquivo = 'restore_' . date('dmY_H_i_s') . '.sql';
                $destinoArquivo = $backupDir . '/' . $nomeArquivo;
                if (!move_uploaded_file($_FILES['arquivo_sql']['tmp_name'], $destinoArquivo)) {
                    $msg = 'Falha ao copiar o arquivo enviado!';
                } else {
                    // Monta comando igual ao Importar.java (mysql -e "source caminho")
                    $pathRestore = str_replace('\\', '/', $destinoArquivo);
                    $cmd = '"' . $mysqlCli . '"'
                        . ' --host=' . $dbHost
                        . ' --port=3306'
                        . ' --user=' . $dbUser
                        . ' --password=' . $dbPass
                        . ' ' . $dbName
                        . ' -e "source ' . $pathRestore . '" 2>&1';
                    exec($cmd, $out, $exitCode);

                    if ($exitCode === 0) {
                        $msg = 'Importação realizada com sucesso!';
                        $msgType = 'success';
                        @unlink($destinoArquivo); // limpa o arquivo temporário restaurado
                    } else {
                        $msg = 'Erro ao realizar a importação: ' . implode(' | ', array_slice($out, 0, 3));
                        @unlink($destinoArquivo);
                    }
                }
            }
        }
    }

    // ---------- RESTAURAR backup existente da lista ----------
    if ($acao === 'restaurar') {
        $nome = basename($_POST['arquivo'] ?? '');
        $caminho = $backupDir . '/' . $nome;
        if (!$mysqlCli) {
            $msg = 'mysql não encontrado no WAMP!';
        } elseif (!preg_match('/\.sql$/i', $nome) || !file_exists($caminho)) {
            $msg = 'Arquivo de backup inválido!';
        } else {
            $pathRestore = str_replace('\\', '/', $caminho);
            $cmd = '"' . $mysqlCli . '"'
                . ' --host=' . $dbHost
                . ' --port=3306'
                . ' --user=' . $dbUser
                . ' --password=' . $dbPass
                . ' ' . $dbName
                . ' -e "source ' . $pathRestore . '" 2>&1';
            exec($cmd, $out, $exitCode);

            if ($exitCode === 0) {
                $msg = 'Backup "' . $nome . '" restaurado com sucesso!';
                $msgType = 'success';
            } else {
                $msg = 'Erro ao restaurar: ' . implode(' | ', array_slice($out, 0, 3));
            }
        }
    }

    // ---------- EXCLUIR backup ----------
    if ($acao === 'excluir') {
        $nome = basename($_POST['arquivo'] ?? '');
        $caminho = $backupDir . '/' . $nome;
        if (preg_match('/\.sql$/i', $nome) && file_exists($caminho)) {
            @unlink($caminho);
            $msg = 'Backup "' . $nome . '" excluído.';
            $msgType = 'success';
        } else {
            $msg = 'Arquivo inválido!';
        }
    }
}

// ====== DOWNLOAD de backup (fora do docroot → readfile) ======
if (isset($_GET['download'])) {
    $nome = basename($_GET['download']);
    $caminho = $backupDir . '/' . $nome;
    if (preg_match('/\.sql$/i', $nome) && file_exists($caminho)) {
        header('Content-Type: application/sql');
        header('Content-Disposition: attachment; filename="' . $nome . '"');
        header('Content-Length: ' . filesize($caminho));
        readfile($caminho);
        exit;
    }
    $msg = 'Arquivo não encontrado!';
}

// Lista backups existentes (mais recentes primeiro)
$backups = [];
if (is_dir($backupDir)) {
    foreach (glob($backupDir . '/*.sql') ?: [] as $f) {
        $backups[] = [
            'nome' => basename($f),
            'tamanho' => filesize($f),
            'data' => date('d/m/Y H:i:s', filemtime($f)),
        ];
    }
    usort($backups, fn($a, $b) => strcmp($b['nome'], $a['nome']));
}

$title = 'Manutenção - Backup / Restore';
include '../includes/header.php';
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-database-gear"></i> Manutenção — Backup's MySQL</h4>
</div>

<?php if ($msg): ?>
<div class="alert alert-<?= $msgType ?> alert-dismissible fade show">
    <?= htmlspecialchars($msg) ?>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>
<?php endif; ?>

<div class="row">
    <!-- ===== EXPORTAR ===== -->
    <div class="col-md-6">
        <div class="card">
            <div class="card-header"><i class="bi bi-box-arrow-down"></i> Exportar (Backup)</div>
            <div class="card-body">
                <p class="text-muted">Gera um arquivo <code>.sql</code> completo do banco <strong><?= htmlspecialchars($dbName) ?></strong> com o <code>mysqldump</code> — igual ao jFIP desktop.</p>
                <form method="POST">
                    <input type="hidden" name="acao" value="exportar">
                    <button type="submit" class="btn btn-success w-100"
                            onclick="return confirm('Gerar backup do banco agora?')">
                        <i class="bi bi-database-down"></i> Exportar o Banco de Dados MySQL
                    </button>
                </form>
                <div class="mt-3">
                    <small class="text-muted">
                        <i class="bi bi-folder2-open"></i> Pasta: <code><?= htmlspecialchars(str_replace('C:/', 'C:\\', str_replace('/', '\\', $backupDir))) ?></code>
                    </small>
                </div>
            </div>
        </div>
    </div>

    <!-- ===== IMPORTAR ===== -->
    <div class="col-md-6">
        <div class="card">
            <div class="card-header"><i class="bi bi-box-arrow-up"></i> Importar (Restore)</div>
            <div class="card-body">
                <p class="text-muted">Restaura o banco a partir de um arquivo <code>.sql</code> — igual ao jFIP desktop (<code>mysql -e "source ..."</code>).</p>
                <form method="POST" enctype="multipart/form-data">
                    <input type="hidden" name="acao" value="importar">
                    <div class="mb-3">
                        <input type="file" name="arquivo_sql" class="form-control" accept=".sql" required>
                    </div>
                    <button type="submit" class="btn btn-warning w-100"
                            onclick="return confirm('ATENÇÃO: o banco atual será SUBSTITUÍDO pelo conteúdo do arquivo. Continuar?')">
                        <i class="bi bi-database-up"></i> Importar o Banco de Dados MySQL
                    </button>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- ===== LISTA DE BACKUPS ===== -->
<div class="card mt-2">
    <div class="card-header"><i class="bi bi-archive"></i> Backups Existentes (<?= count($backups) ?>)</div>
    <div class="card-body">
        <?php if (empty($backups)): ?>
            <p class="text-muted mb-0">Nenhum backup encontrado. Use "Exportar" para gerar o primeiro.</p>
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
                    <?php foreach ($backups as $b): ?>
                    <tr>
                        <td><i class="bi bi-file-earmark-sql"></i> <?= htmlspecialchars($b['nome']) ?></td>
                        <td class="text-end"><?= number_format($b['tamanho'] / 1024, 1, ',', '.') ?> KB</td>
                        <td><?= $b['data'] ?></td>
                        <td class="text-end">
                            <a href="?download=<?= urlencode($b['nome']) ?>" class="btn btn-sm btn-outline-primary" title="Baixar">
                                <i class="bi bi-download"></i>
                            </a>
                            <form method="POST" class="d-inline" onsubmit="return confirm('Restaurar este backup? O banco atual será SUBSTITUÍDO!')">
                                <input type="hidden" name="acao" value="restaurar">
                                <input type="hidden" name="arquivo" value="<?= htmlspecialchars($b['nome']) ?>">
                                <button type="submit" class="btn btn-sm btn-outline-success" title="Restaurar">
                                    <i class="bi bi-arrow-counterclockwise"></i>
                                </button>
                            </form>
                            <form method="POST" class="d-inline" onsubmit="return confirm('Excluir este backup?')">
                                <input type="hidden" name="acao" value="excluir">
                                <input type="hidden" name="arquivo" value="<?= htmlspecialchars($b['nome']) ?>">
                                <button type="submit" class="btn btn-sm btn-outline-danger" title="Excluir">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </form>
                        </td>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
        <?php endif; ?>
    </div>
</div>

<?php include '../includes/footer.php'; ?>
