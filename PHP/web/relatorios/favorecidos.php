<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$db = Database::getInstance()->getConnection();

$tipo = $_GET['tipo'] ?? '';
$where = '';
if ($tipo === 'C') $where = "WHERE c.Tipo = 'C'";
elseif ($tipo === 'F') $where = "WHERE c.Tipo = 'F'";

$stmt = $db->query("
    SELECT c.*, g.nome_C as nomeConta
    FROM tbclifor c
    LEFT JOIN gpprincipal g ON c.fkCliForGp = g.cod_Geral
    $where
    ORDER BY c.nomeCliFor
");
$dados = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Relatório - Favorecidos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { font-family: 'Segoe UI', sans-serif; padding: 20px; }
        @media print { body { padding: 0; } .no-print { display: none !important; } }
        .header { text-align: center; margin-bottom: 20px; }
        table { font-size: 12px; }
    </style>
</head>
<body>
    <div class="no-print text-end mb-3">
        <button class="btn btn-primary" onclick="window.print()">Imprimir / PDF</button>
        <a href="../dashboard.php" class="btn btn-secondary">Voltar</a>
        <div class="btn-group">
            <a href="?tipo=C" class="btn btn-sm btn-outline-info">Clientes</a>
            <a href="?tipo=F" class="btn btn-sm btn-outline-warning">Fornecedores</a>
            <a href="?" class="btn btn-sm btn-outline-secondary">Todos</a>
        </div>
    </div>
    <div class="header">
        <h2>Clientes e Fornecedores</h2>
        <small>Emitido em: <?= date('d/m/Y H:i') ?></small>
    </div>
    <table class="table table-bordered table-striped">
        <thead>
            <tr>
                <th>Código</th>
                <th>Tipo</th>
                <th>Nome</th>
                <th>Apelido</th>
                <th>Cidade/UF</th>
                <th>Contato</th>
                <th>Classificação</th>
            </tr>
        </thead>
        <tbody>
            <?php foreach ($dados as $d): ?>
            <tr>
                <td><?= htmlspecialchars($d['codCliFor']) ?></td>
                <td><?= $d['Tipo'] === 'C' ? 'Cliente' : 'Fornecedor' ?></td>
                <td><?= htmlspecialchars($d['nomeCliFor']) ?></td>
                <td><?= htmlspecialchars($d['apelidoCliFor']) ?></td>
                <td><?= htmlspecialchars($d['cidade'] . '/' . $d['estado']) ?></td>
                <td><?= htmlspecialchars($d['contatoCliFor']) ?></td>
                <td><?= htmlspecialchars($d['nomeConta'] ?? '-') ?></td>
            </tr>
            <?php endforeach; ?>
        </tbody>
    </table>
</body>
</html>
