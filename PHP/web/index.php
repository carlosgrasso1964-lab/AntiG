<?php
session_start();
if (isset($_SESSION['usuario_id'])) {
    header('Location: dashboard.php');
    exit;
}
require_once 'config/database.php';
require_once 'includes/functions.php';

$error = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $usuario = trim($_POST['usuario'] ?? '');
    $senha = trim($_POST['senha'] ?? '');

    if ($usuario && $senha) {
        $dv = substr($senha, -1);
        $senhaSemDV = substr($senha, 0, -1);
        $comparardv = calcularAlgarismo();

        if (is_numeric($dv) && intval($dv) === $comparardv) {
            $stmt = Database::prepare("SELECT id, usuario, senha FROM dados_senhas WHERE usuario = ?");
            $stmt->execute([$usuario]);
            $user = $stmt->fetch();

            if ($user) {
                $senhaMD5 = md5($senhaSemDV);
                if ($user['senha'] === $senhaMD5) {
                    $_SESSION['usuario_id'] = $user['id'];
                    $_SESSION['usuario_nome'] = $user['usuario'];
                    registrarLog($user['usuario'], 'Login');
                    header('Location: dashboard.php');
                    exit;
                }
            }
        }
        $error = 'Usuário ou senha inválidos!';
    } else {
        $error = 'Preencha todos os campos!';
    }
}
?>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - jFIP Web</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="assets/css/style.css" rel="stylesheet">
</head>
<body>
    <div class="login-container">
        <div class="card login-card">
            <div class="card-header">
                <div class="login-logo"><i class="bi bi-calculator"></i></div>
                <h3>Sistema Financeiro</h3>
                <p class="mb-0 opacity-75">jFIP Web</p>
            </div>
            <div class="card-body">
                <?php if ($error): ?>
                    <div class="alert alert-danger"><?= $error ?></div>
                <?php endif; ?>
                <form method="POST">
                    <div class="mb-3">
                        <label class="form-label">Usuário</label>
                        <input type="text" name="usuario" class="form-control" required autofocus>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Senha</label>
                        <input type="password" name="senha" class="form-control" required>
                    </div>
                    <button type="submit" class="btn btn-primary w-100">Entrar</button>
                </form>
            </div>
        </div>
    </div>
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
