<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$db = Database::getInstance()->getConnection();

$stmt = $db->query("
    SELECT r.*, g.nome_C as nomeConta
    FROM tbrecursos r
    LEFT JOIN gpprincipal g ON r.fk_gpprinc = g.cod_Geral
    ORDER BY r.codigo
");
$dados = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <title>Relatório - Recursos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body { font-family: 'Segoe UI', sans-serif; padding: 20px; }
        @media print { body { padding: 0; } .no-print { display: none !important; } }
        .header { text-align: center; margin-bottom: 20px; }
        table { font-size: 13px; }
    </style>
</head>
<body>
    <div class="no-print text-end mb-3">
        <button class="btn btn-primary" onclick="window.print()">Imprimir / PDF</button>
        <a href="../dashboard.php" class="btn btn-secondary">Voltar</a>
    </div>
    <div class="header">
        <h2>Recursos Financeiros</h2>
        <small>Emitido em: <?= date('d/m/Y H:i') ?></small>
    </div>
    <table class="table table-bordered table-striped">
        <thead>
            <tr>
                <th>Código</th>
                <th>Nome</th>
                <th>Agência</th>
                <th>Fluxo</th>
                <th class="text-end">Limite</th>
                <th>Abertura</th>
                <th>Status</th>
                <th>Conta Plano</th>
            </tr>
        </thead>
        <tbody>
            <?php foreach ($dados as $d): ?>
            <tr>
                <td><?= htmlspecialchars($d['codigo']) ?></td>
                <td><?= htmlspecialchars($d['nomebco']) ?></td>
                <td><?= htmlspecialchars($d['agencia']) ?></td>
                <td><?= $d['fluxo'] === 'E' ? 'Entrada' : 'Saída' ?></td>
                <td class="text-end"><?= formatMoeda($d['limite']) ?></td>
                <td><?= formatData($d['abertura']) ?></td>
                <td><?= $d['status'] === 'A' ? 'Ativo' : 'Inativo' ?></td>
                <td><?= htmlspecialchars($d['nomeConta'] ?? '-') ?></td>
            </tr>
            <?php endforeach; ?>
        </tbody>
    </table>
</body>
</html>
