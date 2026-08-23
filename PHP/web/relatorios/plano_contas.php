<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$db = Database::getInstance()->getConnection();

$stmt = $db->query("SELECT * FROM gpprincipal ORDER BY cod_Geral");
$dados = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Relatório - Plano de Contas</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { font-family: 'Segoe UI', sans-serif; padding: 20px; }
        @media print { body { padding: 0; } .no-print { display: none !important; } }
        .header { text-align: center; margin-bottom: 20px; }
        .header h2 { margin: 0; }
        .header small { color: #666; }
        table { font-size: 13px; }
    </style>
</head>
<body>
    <div class="no-print text-end mb-3">
        <button class="btn btn-primary" onclick="window.print()"><i class="bi bi-printer"></i> Imprimir / PDF</button>
        <a href="../dashboard.php" class="btn btn-secondary">Voltar</a>
    </div>
    <div class="header">
        <h2>Plano de Contas</h2>
        <small>Emitido em: <?= date('d/m/Y H:i') ?></small>
    </div>
    <table class="table table-bordered table-striped">
        <thead>
            <tr>
                <th>Código</th>
                <th>Principal</th>
                <th>Sub</th>
                <th>Conta</th>
            </tr>
        </thead>
        <tbody>
            <?php foreach ($dados as $d): ?>
            <tr>
                <td><?= htmlspecialchars($d['cod_Geral']) ?></td>
                <td><?= htmlspecialchars($d['nome_P']) ?></td>
                <td><?= htmlspecialchars($d['nome_S']) ?></td>
                <td><?= htmlspecialchars($d['nome_C']) ?></td>
            </tr>
            <?php endforeach; ?>
        </tbody>
    </table>
</body>
</html>
