<?php
header('Content-Type: application/json; charset=utf-8');
$input = json_decode(file_get_contents('php://input'), true);
$resposta = trim($input['resposta'] ?? '');
if (!$resposta) {
    echo json_encode(['erro' => 'Resposta vazia.']);
    exit;
}
$pasta = __DIR__ . '/../analises_lia';
if (!is_dir($pasta)) @mkdir($pasta, 0755, true);
$arquivo = $pasta . '/Parecer_' . date('Ymd_Hi') . '.txt';
$conteudo = "AUDITORIA LIA - JFIP WEB\n";
$conteudo .= "DATA: " . date('d/m/Y H:i:s') . "\n";
$conteudo .= "------------------------------------------\n\n";
$conteudo .= $resposta;
@file_put_contents($arquivo, $conteudo);
echo json_encode(['ok' => true, 'arquivo' => $arquivo]);
