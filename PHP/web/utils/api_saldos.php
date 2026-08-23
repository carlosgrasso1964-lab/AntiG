<?php
session_start();
require_once '../config/database.php';
require_once '../includes/functions.php';

if (!isAuthenticated()) {
    http_response_code(401);
    echo json_encode(['error' => 'Unauthorized']);
    exit;
}

header('Content-Type: application/json');

try {
    // 1. Principal query: saldos por recurso (excluding loans)
    $sql = "
        SELECT 
            recurso, 
            Fonte AS nomebco, 
            vrecurso, 
            SUM(saldos) AS Saldos
        FROM vsaldos
        WHERE recurso NOT IN (
            SELECT DISTINCT recurso
            FROM view_movimento
            WHERE classif IN ('2.001.004', '2.002.001', '4.008.011')
              AND COALESCE(Prev, '') <> 'F'
        )
        GROUP BY recurso, Fonte, vrecurso
        ORDER BY CAST(SUBSTR(vrecurso, 1, 1) AS UNSIGNED), vrecurso
    ";
    
    $stmt = Database::query($sql);
    $rows = $stmt->fetchAll();
    
    // 2. Loans query:
    $sqlEmprestimos = "
        SELECT 
            (COALESCE(SUM(CASE WHEN classif = '3.003.005' AND dtApr IS NOT NULL THEN Valor ELSE 0 END), 0) +
            COALESCE(SUM(CASE WHEN classif = '4.008.011' AND dtApr IS NOT NULL THEN Valor ELSE 0 END), 0)) * -1 
            AS saldo_emprestimos
        FROM view_movimento
        WHERE COALESCE(Prev, '') <> 'F'
    ";
    
    $stmtEmp = Database::query($sqlEmprestimos);
    $saldoEmprestimos = (double)($stmtEmp->fetchColumn() ?: 0.0);
    
    // Format all Saldos to float
    foreach ($rows as &$row) {
        $row['Saldos'] = (double)$row['Saldos'];
    }
    unset($row);
    
    // Append loans row if it is non-zero
    if ($saldoEmprestimos != 0) {
        $rows[] = [
            'recurso' => '',
            'nomebco' => 'Empréstimos (saldo)',
            'vrecurso' => '',
            'Saldos' => $saldoEmprestimos
        ];
    }
    
    echo json_encode($rows);
} catch (Exception $e) {
    echo json_encode([]);
}
