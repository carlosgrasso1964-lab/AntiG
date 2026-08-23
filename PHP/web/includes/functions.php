<?php

function formatMoeda($valor) {
    if ($valor === '' || $valor === null || $valor === false) return '';
    return 'R$ ' . number_format((float)$valor, 2, ',', '.');
}

function formatData($data) {
    if (!$data || $data == '0000-00-00') return '';
    return date('d/m/Y', strtotime($data));
}

function formatDataHora($data) {
    if (!$data) return '';
    return date('d/m/Y H:i', strtotime($data));
}

function dataParaSQL($data) {
    if (!$data) return null;
    $parts = explode('/', $data);
    if (count($parts) === 3) {
        return "{$parts[2]}-{$parts[1]}-{$parts[0]}";
    }
    return $data;
}

function mask($val, $mask) {
    $maskared = '';
    $k = 0;
    for ($i = 0; $i < strlen($mask); $i++) {
        if ($mask[$i] == '#') {
            if (isset($val[$k])) $maskared .= $val[$k++];
        } else {
            if (isset($mask[$i])) $maskared .= $mask[$i];
        }
    }
    return $maskared;
}

function formatCpfCnpj($cpf) {
    $cpf = preg_replace('/\D/', '', $cpf ?? '');
    if (strlen($cpf) === 11) {
        return mask($cpf, '###.###.###-##');
    }
    return $cpf;
}

function formatCep($cep) {
    $cep = preg_replace('/\D/', '', $cep ?? '');
    return mask($cep, '#####-###');
}

function formatTelefone($tel) {
    $tel = preg_replace('/\D/', '', $tel ?? '');
    if (strlen($tel) === 11) {
        return mask($tel, '(##) #####-####');
    }
    if (strlen($tel) === 10) {
        return mask($tel, '(##) ####-####');
    }
    return $tel;
}

function redirect($url) {
    header("Location: $url");
    exit;
}

function isAuthenticated() {
    return isset($_SESSION['usuario_id']);
}

function requireAuth() {
    if (!isAuthenticated()) {
        redirect('../index.php');
    }
}

function flashMessage($type, $message) {
    $_SESSION['flash'] = ['type' => $type, 'message' => $message];
}

function getFlash() {
    if (isset($_SESSION['flash'])) {
        $flash = $_SESSION['flash'];
        unset($_SESSION['flash']);
        return $flash;
    }
    return null;
}

function gerarCodigo($tabela, $campo, $prefixo, $digitos = 4) {
    $stmt = Database::prepare("SELECT MAX(CAST(SUBSTRING($campo, LENGTH(?)+1) AS UNSIGNED)) as max FROM $tabela WHERE $campo LIKE ?");
    $stmt->execute([$prefixo, $prefixo . '%']);
    $row = $stmt->fetch();
    $next = ($row['max'] ?? 0) + 1;
    return $prefixo . str_pad($next, $digitos, '0', STR_PAD_LEFT);
}

function registrarLog($usuario, $acao) {
    $stmt = Database::prepare("INSERT INTO logs (usul, data, action) VALUES (?, NOW(), ?)");
    $stmt->execute([$usuario, $acao]);
}

function statusBadge($status) {
    $map = [
        'A' => ['Aberto', 'warning'],
        'P' => ['Pago', 'success'],
        'PP' => ['Pago Parcial', 'info'],
        'AA' => ['Aberto', 'danger'],
        'AB' => ['Aberto', 'warning'],
        'PG' => ['Pago', 'success'],
        'RC' => ['Recebido', 'success'],
        'SI' => ['Saldo Inicial', 'secondary'],
        'TO' => ['Transf. Origem', 'warning'],
        'TD' => ['Transf. Destino', 'info'],
        '' => ['---', 'light'],
    ];
    $s = $map[$status] ?? [$status, 'secondary'];
    return "<span class='badge bg-{$s[1]}'>{$s[0]}</span>";
}

/**
 * Calcula o dígito verificador (DV) baseado na data atual no formato ddMMyyyy.
 * Réplica do algoritmo Java `Algarismo.Alga()`.
 * 
 * @return int
 */
/**
 * Avança uma data preservando o último dia do mês (comportamento idêntico ao Java Calendar.add).
 * Ex: 31/08 + 1 mês = 30/09 (não 01/10 como faz DateTime::modify padrão)
 *
 * CORREÇÃO: Em vez de usar PHP modify("+X month") que tem bug no fim do mês,
 * calculamos manualmente o novo mês/ano e ajustamos o dia.
 * - Se a data original era o último dia do mês, o resultado será o último dia do novo mês
 * - Caso contrário, o dia original é preservado (limitado ao último dia do novo mês)
 *
 * @param DateTime $date Data a ser avançada
 * @param string $field 'month', 'year', 'week', 'day'
 * @param int $amount Quantidade a avançar
 */
function avancarDataPreservandoFimMes(DateTime $date, string $field, int $amount): void {
    if (in_array($field, ['month', 'year'])) {
        $originalDay = (int)$date->format('d');
        $originalMonth = (int)$date->format('n');
        $originalYear = (int)$date->format('Y');
        $isLastDay = ($originalDay === (int)$date->format('t'));

        // Calcular novo mês e ano
        if ($field === 'year') {
            $newYear = $originalYear + $amount;
            $newMonth = $originalMonth;
        } else {
            $totalMonths = ($originalYear * 12 + $originalMonth - 1) + $amount;
            $newYear = (int)floor($totalMonths / 12);
            $newMonth = ($totalMonths % 12) + 1;
        }

        // Último dia do mês alvo
        $lastDay = (int)date('t', mktime(0, 0, 0, $newMonth, 1, $newYear));

        // Se era último dia do mês, preserva último dia; senão, limita ao último dia válido
        $day = $isLastDay ? $lastDay : min($originalDay, $lastDay);

        $date->setDate($newYear, $newMonth, $day);
    } elseif ($field === 'week') {
        $date->modify("+{$amount} week");
    } elseif ($field === 'day') {
        $date->modify("+{$amount} day");
    }
}

function calcularAlgarismo() {
    // Configura fuso horário padrão para garantir data local correta
    date_default_timezone_set('America/Sao_Paulo');
    
    // Obtém data atual no formato ddMMyyyy
    $pega = date('dmY');
    $num = intval($pega);
    $soma = 0;
    
    // Primeira soma dos dígitos da data
    while ($num > 0) {
        $soma += ($num % 10);
        $num = intval($num / 10);
    }
    
    // Soma dos dígitos até obter um único dígito
    while ($soma > 9) {
        $temp = $soma;
        $soma = 0;
        while ($temp > 0) {
            $soma += ($temp % 10);
            $temp = intval($temp / 10);
        }
    }
    
    return $soma;
}

