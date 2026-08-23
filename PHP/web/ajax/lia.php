<?php
set_time_limit(130);
header('Content-Type: application/json; charset=utf-8');

$input = json_decode(file_get_contents('php://input'), true);
$prompt = $input['prompt'] ?? '';

if (!$prompt) {
    echo json_encode(['erro' => 'Prompt vazio.']);
    exit;
}

// Pré-checa se o Ollama está escutando (conexão rápida via socket)
$ollamaHost = '127.0.0.1';
$ollamaPort = 11434;
$fp = @fsockopen($ollamaHost, $ollamaPort, $errno, $errstr, 3);
if (!$fp) {
    echo json_encode(['erro' => "Lia está offline (Ollama não respondeu em {$ollamaHost}:{$ollamaPort})."]);
    exit;
}
fclose($fp);

// Chama o Ollama via POST
$url = "http://{$ollamaHost}:{$ollamaPort}/api/generate";
$payload = json_encode([
    'model'  => 'gemma2:2b',
    'prompt' => $prompt,
    'stream' => false
]);

$ctx = stream_context_create([
    'http' => [
        'method'  => 'POST',
        'header'  => "Content-Type: application/json\r\n",
        'content' => $payload,
        'timeout' => 120,
        'ignore_errors' => true
    ]
]);

$resp = @file_get_contents($url, false, $ctx);

if ($resp === false) {
    echo json_encode(['erro' => 'Lia está exausta (Ollama não retornou resposta a tempo).']);
    exit;
}

$data = json_decode($resp, true);
$resposta = trim($data['response'] ?? '');

if (!$resposta) {
    echo json_encode(['erro' => 'Resposta vazia da Lia.']);
    exit;
}

// Salva parecer automaticamente
$pasta = __DIR__ . '/../analises_lia';
if (!is_dir($pasta)) @mkdir($pasta, 0755, true);
$arquivo = $pasta . '/Parecer_' . date('Ymd_Hi') . '.txt';
$conteudo = "AUDITORIA LIA - JFIP WEB\n";
$conteudo .= "DATA: " . date('d/m/Y H:i:s') . "\n";
$conteudo .= "------------------------------------------\n\n";
$conteudo .= $resposta;
@file_put_contents($arquivo, $conteudo);

echo json_encode(['resposta' => $resposta, 'arquivo' => $arquivo]);
