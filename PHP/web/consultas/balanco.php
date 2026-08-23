<?php
session_start();
ini_set('display_errors', 1);
error_reporting(E_ALL);
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$title = 'Balanço';
include '../includes/header.php';

$db = Database::getInstance()->getConnection();
$erro = '';

// Parâmetros (igual VConsBalanc.java): dataIni/dataFim + regime (vcto/emissao/apresent)
$dataIni = $_POST['data_ini'] ?? '';
$dataFim = $_POST['data_fim'] ?? '';
$regime  = $_POST['regime'] ?? 'vcto';

$linhas = [];
$totais = ['ativo' => 0.0, 'passivo' => 0.0];
$contaTotais = [];
$atividade = ['receitas' => 0.0, 'despesas' => 0.0, 'resultado' => 0.0, 'subPassivo' => 0.0, 'patrLiq' => 0.0, 'saFinal' => 0.0, 'subPrincipal' => 0.0];

$campoData = $regime === 'emissao' ? 'dtemi' : ($regime === 'apresent' ? 'dtapr' : 'dtvcto');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (strlen($dataIni) != 10 || strlen($dataFim) != 10) {
        $erro = 'Atenção! Favor preencher corretamente as datas (dd/mm/yyyy).';
    } else {
        try {
            $di = dataParaSQL($dataIni);
            $df = dataParaSQL($dataFim);

            // 1) Consulta principal (view_movimento) — espelha VConsBalanc.java linhas 297-334
            $sql = "
                SELECT
                    nome_P AS Princ,
                    nome_S AS Sub,
                    CASE
                        WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS A RECEBER%' THEN 'Contas a Receber'
                        WHEN UPPER(TRIM(nomebco)) = 'CONTAS A RECEBER' AND UPPER(TRIM(nome_C)) = 'BANCOS' THEN 'Contas a Receber'
                        ELSE nome_C
                    END AS Conta,
                    CASE
                        WHEN UPPER(TRIM(nomebco)) = 'CONTAS A RECEBER' THEN 'CONTAS A RECEBER'
                        WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS A RECEBER%' THEN 'CONTAS A RECEBER'
                        ELSE nomebco
                    END AS Nome,
                    CASE
                        WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS A RECEBER%' THEN '1.002.001'
                        WHEN UPPER(TRIM(nomebco)) = 'CONTAS A RECEBER' AND UPPER(TRIM(nome_C)) = 'BANCOS' THEN '1.002.001'
                        ELSE vrecurso
                    END AS vrecurso,
                    recurso,
                    SUM(Valor) AS Total
                FROM view_movimento
                WHERE COALESCE(Prev, '') <> 'F'
                  AND dtlancto <= ?
                  AND TRIM(nome_C) <> 'Contas a Pagar'
                  AND NOT (nomebco = 'CONTAS A PAGAR' AND nome_C NOT IN ('Contas a Pagar'))
                GROUP BY 1, 2, 3, 4, 5, 6
                ORDER BY
                    CAST(SUBSTRING(
                        CASE WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS A RECEBER%' THEN '1.002.001'
                             WHEN UPPER(TRIM(nomebco)) = 'CONTAS A RECEBER' AND UPPER(TRIM(nome_C)) = 'BANCOS' THEN '1.002.001'
                             ELSE vrecurso END, 1, 1) AS UNSIGNED),
                    CASE WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS A RECEBER%' THEN '1.002.001'
                         WHEN UPPER(TRIM(nomebco)) = 'CONTAS A RECEBER' AND UPPER(TRIM(nome_C)) = 'BANCOS' THEN '1.002.001'
                         ELSE vrecurso END";
            $stmt = $db->prepare($sql);
            $stmt->execute([$df]);
            $rows = $stmt->fetchAll();

            // Processamento principal (espelha linhas 356-418 do Java)
            $prin = 'a'; $sub = ' '; $cta = ' '; $titulo = 's';
            $totalPrincipal = 0.0; $totalGrupo = 0.0; $totalConta = 0.0; $sa = 0.0; $subPrincipal = 0.0;
            $saNoFinalDoAtivo = 0.0;

            foreach ($rows as $m) {
                $currentPrincipal = $m['Princ'];
                $currentSub = $m['Sub'];
                $currentConta = $m['Conta'];
                $valor = (float)$m['Total'];

                if ($titulo === 's') {
                    $linhas[] = ['', '', '', '', '', ''];
                    $linhas[] = ['', '-- BALANÇO PATRIMONIAL --', 'Realizado em - ' . $dataFim, '', 0, 0];
                    $linhas[] = ['', '', '', '', '', ''];
                    $titulo = 'n';
                }

                if ($cta !== $currentConta && $cta !== ' ') {
                    $linhas[] = ['', '', '', 'TOTAL DA CONTA ' . $cta, $totalConta, $sa];
                    $linhas[] = ['', '', '', '', '', ''];
                    $contaTotais[strtoupper(str_replace(' ', '_', $cta))] = round($totalConta * 100) / 100;
                    $totalConta = 0.0;
                }

                if ($sub !== $currentSub) {
                    if ($sub !== ' ') {
                        $linhas[] = ['', '', '', 'TOTAL DO GRUPO ' . $sub, $totalGrupo, $sa];
                        $subPrincipal = $totalGrupo;
                        $linhas[] = ['', '', '', '', '', ''];
                    }
                    $totalGrupo = 0.0;
                }

                if ($prin !== $currentPrincipal) {
                    if ($prin !== 'a') {
                        if ($prin === 'ATIVO') { $saNoFinalDoAtivo = $sa; $totais['ativo'] = $sa; }
                        $linhas[] = ['', '', '', 'TOTAL DO ' . $prin, '', $sa];
                        $linhas[] = ['', '', '', '', '', ''];
                    }
                    $totalPrincipal = 0.0;
                    $subPrincipal = 0.0;
                }

                $sa += $valor;
                $totalPrincipal += $valor;
                $totalGrupo += $valor;
                $totalConta += $valor;

                $linhas[] = [
                    $prin === $currentPrincipal ? '' : $currentPrincipal,
                    $sub === $currentSub ? '' : $currentSub,
                    $cta === $currentConta ? '' : $currentConta,
                    $m['Nome'],
                    $valor,
                    $sa
                ];

                $prin = $currentPrincipal;
                $sub = $currentSub;
                $cta = $currentConta;
            }

            // Última conta do SQL (linhas 420-430)
            if ($cta !== ' ' && $cta !== 'Contas a Pagar') {
                $linhas[] = ['', '', '', 'TOTAL DA CONTA ' . $cta, $totalConta, $sa];
                $linhas[] = ['', '', '', '', '', ''];
                $contaTotais[strtoupper(str_replace(' ', '_', $cta))] = round($totalConta * 100) / 100;
                $totalConta = 0.0;
            }

            // 2) Contas a Pagar especial (linhas 432-466)
            $sqlCP = "SELECT COALESCE(SUM(Valor), 0) AS TotalCP FROM view_movimento WHERE recurso = '0079' AND vrecurso = '2.001.002' AND Prev <> 'F' AND dtlancto <= ? AND (dtApr IS NULL OR dtApr >= ?)";
            $stmtCP = $db->prepare($sqlCP);
            $stmtCP->execute([$df, $di]);
            $saldoContasPagarCorreto = 0.0;
            if ($r = $stmtCP->fetch()) { $saldoContasPagarCorreto = round((float)$r['TotalCP'] * 100) / 100; }

            $sa += $saldoContasPagarCorreto;
            $totalGrupo += $saldoContasPagarCorreto;
            $totalPrincipal += $saldoContasPagarCorreto;

            $linhas[] = ['', '', 'Contas a Pagar', 'CONTAS A PAGAR', $saldoContasPagarCorreto, $sa];
            $linhas[] = ['', '', '', 'TOTAL DA CONTA Contas a Pagar', $saldoContasPagarCorreto, $sa];
            $contaTotais['CONTAS_A_PAGAR'] = $saldoContasPagarCorreto;

            // Fechamento do Grupo (PASSIVO CIRCULANTE) e Principal (PASSIVO)
            $linhas[] = ['', '', '', '', '', ''];
            $linhas[] = ['', '', '', 'TOTAL DO GRUPO ' . $sub, $totalGrupo, $sa];
            $linhas[] = ['', '', '', '', '', ''];
            $linhas[] = ['', '', '', 'SUB DO ' . $prin, '', $sa];

            $totais['passivo'] = $totalPrincipal;

            // 3) Receitas e Despesas (linhas 481-492)
            $sql2 = "SELECT SUM(CASE WHEN nome_P = 'RECEITAS' THEN Valor ELSE 0 END) AS TotalReceitas,
                            SUM(CASE WHEN nome_P = 'DESPESAS' THEN Valor ELSE 0 END) AS TotalDespesas
                     FROM viewmovrd
                     WHERE $campoData BETWEEN ? AND ? AND nome_P <> 'NULO' AND vRecurso NOT IN ('2.001.004')";
            $stmt2 = $db->prepare($sql2);
            $stmt2->execute([$di, $df]);
            $Receitas = 0.0; $Despesas = 0.0;
            if ($r2 = $stmt2->fetch()) {
                $Receitas = (float)$r2['TotalReceitas'];
                $Despesas = (float)$r2['TotalDespesas'];
            }

            // Cálculos finais (linhas 494-497)
            $Resultado = -($Receitas + $Despesas);
            $SubPassivo = -$totais['ativo'];
            $PatrLiq = -($sa);

            // Exibição Final (PL e Resultados) — linhas 499-508
            $linhas[] = ['', '', '', '', '', ''];
            $linhas[] = ['', 'PATRIMONIO_LIQUIDO', 'Patrimonio_Acumulado', '', $PatrLiq, ''];
            $linhas[] = ['', '', '', '', '', ''];
            $linhas[] = ['', '', '', 'RECEITAS NO PERIODO', $Receitas, ''];
            $linhas[] = ['', '', '', 'DESPESAS NO PERIODO', $Despesas, ''];
            $linhas[] = ['', '', '', 'RESULTADO DO PERIODO', -$Resultado, ''];
            $linhas[] = ['', '', '', '', '', ''];
            $linhas[] = ['', '', '', 'TOTAL DO PASSIVO', '', $SubPassivo];
            $linhas[] = ['', '', '', '', '', ''];

            // Guarda para a view/análises
            $atividade = [
                'receitas' => $Receitas, 'despesas' => $Despesas, 'resultado' => $Resultado,
                'subPassivo' => $SubPassivo, 'patrLiq' => $PatrLiq, 'saFinal' => $sa,
                'subPrincipal' => $subPrincipal, 'ativo' => $totais['ativo'], 'passivo' => $totais['passivo']
            ];
            $totais['saFinal'] = $sa;

        } catch (Exception $e) {
            $erro = 'Erro na consulta: ' . $e->getMessage();
        }
    }
}

function rdx_money_b($v) { return formatMoeda($v); }
function rdx_pct_b($v) { return ($v === '' || $v === null) ? '' : number_format((float)$v, 4, ',', '') . '%'; }
?>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-file-earmark-text"></i> Balanço Patrimonial</h4>
</div>

<?php if ($erro): ?>
<div class="alert alert-danger"><?= htmlspecialchars($erro) ?></div>
<?php endif; ?>

<form method="POST" class="row g-3 mb-3">
    <div class="col-md-2">
        <label class="form-label">Data Inicial</label>
        <input type="text" name="data_ini" class="form-control datepicker" value="<?= htmlspecialchars($dataIni) ?>" placeholder="dd/mm/yyyy" required>
    </div>
    <div class="col-md-2">
        <label class="form-label">Data Final</label>
        <input type="text" name="data_fim" class="form-control datepicker" value="<?= htmlspecialchars($dataFim) ?>" placeholder="dd/mm/yyyy" required>
    </div>
    <div class="col-md-5">
        <label class="form-label d-block">Regime</label>
        <div class="btn-group" role="group">
            <input type="radio" class="btn-check" name="regime" id="regVcto" value="vcto" <?= $regime==='vcto'?'checked':'' ?>>
            <label class="btn btn-outline-secondary" for="regVcto">Vencimento</label>
            <input type="radio" class="btn-check" name="regime" id="regEmis" value="emissao" <?= $regime==='emissao'?'checked':'' ?>>
            <label class="btn btn-outline-secondary" for="regEmis">Emissão</label>
            <input type="radio" class="btn-check" name="regime" id="regApres" value="apresent" <?= $regime==='apresent'?'checked':'' ?>>
            <label class="btn btn-outline-secondary" for="regApres">Apresentação</label>
        </div>
    </div>
    <div class="col-md-1 d-flex align-items-end">
        <button type="submit" class="btn btn-primary w-100">Pesquisar</button>
    </div>
</form>

<?php if ($_SERVER['REQUEST_METHOD'] === 'POST' && empty($erro)): ?>
<div class="card">
    <div class="card-header"><i class="bi bi-table"></i> Balanço (Principal / Grupo / Conta / Nome / Total / Acumulado)</div>
    <div class="card-body">
        <?php if (empty($linhas)): ?>
            <div class="alert alert-info mb-0">Nenhum registro encontrado para o balanço.</div>
        <?php else: ?>
        <div class="table-responsive">
            <table class="table table-sm table-hover datatable">
                <thead>
                    <tr>
                        <th>PRINCIPAL</th>
                        <th>GRUPO</th>
                        <th>CONTA</th>
                        <th>NOME</th>
                        <th class="text-end">TOTAL</th>
                        <th class="text-end">ACUMULADO</th>
                    </tr>
                </thead>
                <tbody>
                <?php foreach ($linhas as $l):
                    $vazio = ($l[0]==='' && $l[1]==='' && $l[2]==='' && $l[3]==='' && $l[4]==='' && $l[5]==='');
                    if ($vazio): ?><tr class="table-secondary"><td colspan="6"></td></tr><?php continue; endif;
                    $isResumo = ($l[0]==='' && $l[1]!=='' && $l[2]==='' && $l[3]==='');
                    $isTitulo = (strpos($l[1] ?? '', 'BALANÇO') !== false);
                ?>
                    <tr class="<?= $isResumo ? 'table-light fw-semibold' : '' ?><?= $isTitulo ? ' table-info text-center' : '' ?>">
                        <td><?= htmlspecialchars($l[0]) ?></td>
                        <td><?= htmlspecialchars($l[1]) ?></td>
                        <td><?= htmlspecialchars($l[2]) ?></td>
                        <td><?= htmlspecialchars($l[3]) ?></td>
                        <td class="text-end <?= ($l[4] ?? 0) < 0 ? 'valor-negativo' : 'valor-positivo' ?>"><?= rdx_money_b($l[4]) ?></td>
                        <td class="text-end <?= ($l[5] ?? 0) < 0 ? 'valor-negativo' : 'valor-positivo' ?>"><?= rdx_money_b($l[5]) ?></td>
                    </tr>
                <?php endforeach; ?>
                </tbody>
            </table>
        </div>
        <?php endif; ?>
    </div>
</div>
<?php endif; ?>

<?php if ($_SERVER['REQUEST_METHOD'] === 'POST' && empty($erro) && !empty($linhas)): ?>
<?php
// === ANÁLISES (espelha VConsBalanc.java linhas 510-698) ===
$A = $atividade;
$ct = $contaTotais;

// Passivo Circulante vem do PRÓPRIO corpo do balanço (TOTAL DO GRUPO PASSIVO CIRCULANTE),
// nao da heuristica, p/ espelhar exatamente o que e exibido (ex.: -5.348,29).
$pcCorpo = 0.0;
foreach ($linhas as $l) {
    $txt = $l[3] ?? '';
    if (stripos($txt, 'TOTAL DO GRUPO') !== false && stripos($txt, 'PASSIVO CIRCULANTE') !== false) {
        $pcCorpo = (float)($l[4] ?? 0);
        break;
    }
}

// Ativo Circulante tambem vem do corpo (TOTAL DO GRUPO ATIVO CIRCULANTE) p/ nao depender de heuristica.
$acCorpo = 0.0;
foreach ($linhas as $l) {
    $txt = $l[3] ?? '';
    if (stripos($txt, 'TOTAL DO GRUPO') !== false && stripos($txt, 'ATIVO CIRCULANTE') !== false) {
        $acCorpo = (float)($l[4] ?? 0);
        break;
    }
}

// Soma por categoria varrendo $contaTotais (chaves = NOME_C normalizado maiúsculo c/ _)
// Usa heurística de palavras-chave p/ não depender de nome exato do banco.
// ATENÇÃO: as chaves usam '_' no lugar de espaço (strtoupper(str_replace(' ','_',...))),
// então a palavra de busca também é normalizada (espaço -> '_').
$cat = function($palavras) use ($ct) {
    $soma = 0.0;
    foreach ($ct as $nome => $val) {
        $nomeU = strtoupper(str_replace(' ', '_', $nome));
        foreach ((array)$palavras as $p) {
            $pU = strtoupper(str_replace(' ', '_', $p));
            if (stripos($nomeU, $pU) !== false) { $soma += $val; break; }
        }
    }
    return $soma;
};

$atCirculante = $acCorpo;   // do corpo (TOTAL DO GRUPO ATIVO CIRCULANTE)
$pasNaoCiclico = $cat(['EMPRESTIMO DE LONGO PRAZO','LONGO PRAZO']);
$anCiclico = $cat(['IMOBILIZADO']);
$pnCiclico = $A['patrLiq'];
$paErratico = $cat(['EMPRESTIMO OBTIDO']);
$atCiclico = $cat(['CONTAS A RECEBER']);
$atErratico = $atCirculante - $atCiclico;   // identidade do Java (linha 529): atCirculante = atCiclico + atErratico
$pasCiclico = $pcCorpo;   // usa o Total do Grupo PASSIVO CIRCULANTE do corpo (nao a heuristica)
$passivoTotalCirculanteENaoCirculante = $pasCiclico;
$CDG = -$A['patrLiq'] - $anCiclico;
$NCG = $atCiclico + $pasCiclico;
$ST = $atErratico + $paErratico;   // corrigido: Java linha 586 usa atErratico + paErratico

$sinalCDG = $CDG >= 0 ? '+' : '-';
$sinalNCG = $NCG >= 0 ? '+' : '-';
$sinalST = $ST >= 0 ? '+' : '-';

$roe = $A['patrLiq'] != 0 ? ($A['resultado'] / $A['patrLiq']) : 'N/A';
$roa = ($A['saFinal'] - $A['subPassivo'] + $A['patrLiq']) != 0 ? ($A['resultado'] / (-($A['saFinal'] - $A['subPassivo'] + $A['patrLiq']))) : 'N/A';
$consumo = $A['receitas'] != 0 ? (-$A['despesas'] / $A['receitas']) : 'N/A';
$pct = $A['subPassivo'] != 0 ? (abs($passivoTotalCirculanteENaoCirculante) / abs($A['subPassivo'])) : 'N/A';
$liquidez = $pasCiclico != 0 ? (($atCirculante / -$pasCiclico) / 100) : 'N/A';
$cobertura = $A['despesas'] != 0 ? (($atErratico + $atCiclico) / -$A['despesas']) : 'N/A';
$coberturaDias = $A['despesas'] != 0 ? (($atErratico + $atCiclico) / -$A['despesas']) * 30 : 'N/A';
$endividamento = $A['subPassivo'] != 0 ? ($pasCiclico / $A['subPassivo']) : 'N/A';
$poupanca = $A['receitas'] != 0 ? (-$A['resultado'] / $A['receitas']) : 'N/A';

// Planejamento Financeiro (Java linhas 596-601)
$Despesas = $A['despesas'];
$pms = -$Despesas * 6;
$pmr = -$Despesas * 20;
$pi = (-$Despesas * 12 * 0.1 * 60);
$pnif = -$Despesas * 12 / 0.06;
$ate = function($v) use ($atCirculante) { return is_numeric($v) && $v != 0 ? ($atCirculante / $v) : 'N/A'; };

// Diagnóstico do Sistema (Java linhas 624-698)
$liqValor = is_numeric($liquidez) ? $liquidez : 0;
if ($liqValor > 1.5) { $labelLiq='EXCELENTE:'; $descLiq='Você tem forte folga financeira para honrar compromissos.'; }
elseif ($liqValor >= 1.0) { $labelLiq='BOM:'; $descLiq='Seus recursos cobrem suas dívidas, mas sem grande margem.'; }
else { $labelLiq='CRÍTICO:'; $descLiq='Você pode precisar de crédito para pagar contas imediatas.'; }

if ($CDG >= 0 && $ST >= 0) { $labelCG='ESTRATÉGIA:'; $descCG='Posição sólida. Momento ideal para investimentos ou novos aportes.'; }
elseif ($ST < 0) { $labelCG='ATENÇÃO:'; $descCG='Sua tesouraria está negativa. Cuidado com juros de cheque especial/cartão.'; }
else { $labelCG='REVISÃO:'; $descCG='Verifique se seus prazos de pagamento estão muito curtos em relação aos recebimentos.'; }

$poupValor = is_numeric($poupanca) ? (float)$poupanca * 100 : 0;
if ($poupValor > 20) { $labelPoup='ALTA PERFORMANCE:'; $descPoup='Você retém uma excelente fatia da sua receita.'; }
elseif ($poupValor >= 10) { $labelPoup='DENTRO DA META:'; $descPoup='Sua taxa de poupança está saudável (acima de 10%).'; }
else { $labelPoup='RISCO:'; $descPoup='Margem de sobra muito baixa. Qualquer imprevisto pode gerar endividamento.'; }

$endivValor = is_numeric($endividamento) ? (float)str_replace(',','.',str_replace('%','',$endividamento)) : 0;
if ($endivValor < 0.3) { $labelDica='OPORTUNIDADE:'; $descDica='Baixo endividamento. Tem espaço para alavancar projetos com capital de terceiros.'; }
else { $labelDica='PRIORIDADE:'; $descDica='Foque na redução de custos fixos e quitação de dívidas de curto prazo.'; }

function sitFinanceira($cdg, $ncg, $st) {
    if ($cdg==='+' && $ncg==='-' && $st==='+') return 'Excelente';
    if ($cdg==='+' && $ncg==='+' && $st==='+') return 'Sólida';
    if ($cdg==='+' && $ncg==='+' && $st==='-') return 'Insatisfatória';
    if ($cdg==='-' && $ncg==='+' && $st==='-') return 'Péssima';
    if ($cdg==='-' && $ncg==='-' && $st==='-') return 'Muito Ruim';
    if ($cdg==='-' && $ncg==='-' && $st==='+') return 'Alto Risco';
    return 'Não Classificada';
}
$situacao = sitFinanceira($sinalCDG, $sinalNCG, $sinalST);

$fmtPct = function($v) { return is_numeric($v) ? rdx_pct_b($v*100) : $v; };
$fmtNum = function($v) { return is_numeric($v) ? rdx_money_b($v) : $v; };
$fmtIdade = function($v) { return is_numeric($v) ? rdx_pct_b((float)$v * 100) : $v; };
?>

<div class="card mt-3">
    <div class="card-header"><i class="bi bi-graph-up"></i> Análises Financeiras (Fleuriet / Índices)</div>
    <div class="card-body table-responsive">
        <table class="table table-sm">
            <thead><tr><th>Indicador</th><th>Descrição</th><th>Fórmula</th><th class="text-end">Valor</th></tr></thead>
            <tbody>
                <tr class="table-secondary"><td colspan="4"><strong>ANÁLISES</strong></td></tr>
                <tr><td>ROE</td><td>RETORNO SOBRE CAPITAL PRÓPRIO</td><td>Resultado/Patrimônio Liq.</td><td class="text-end"><?= $fmtPct($roe) ?></td></tr>
                <tr><td>ROA</td><td>RETORNO SOBRE O ATIVO</td><td>Resultado/Ativo Total</td><td class="text-end"><?= $fmtPct($roa) ?></td></tr>
                <tr><td>CONSUMO</td><td>TAXA DE CONSUMO</td><td>Despesas/Receitas</td><td class="text-end"><?= $fmtPct($consumo) ?></td></tr>
                <tr><td>PCT</td><td>PARTICIPAÇÃO DO CAPITAL DE TERCEIROS</td><td>Passivo C.+N.C./Passivo Total</td><td class="text-end"><?= $fmtPct($pct) ?></td></tr>
                <tr class="table-secondary"><td colspan="4"><strong>ATIVO E PASSIVO CÍCLICO</strong></td></tr>
                <tr><td>AC</td><td>ATIVO CÍCLICO</td><td>Ctas. a Receber + Empréstimos C.Prazo</td><td class="text-end"><?= $fmtNum($atCiclico) ?></td></tr>
                <tr><td>PC</td><td>PASSIVO CÍCLICO</td><td>Ctas. a Pagar + Cartões + Fornecedores</td><td class="text-end"><?= $fmtNum(-$pasCiclico) ?></td></tr>
                <tr class="table-secondary"><td colspan="4"><strong>ATIVO E PASSIVO NÃO CÍCLICOS</strong></td></tr>
                <tr><td>ANC</td><td>ATIVO NÃO CÍCLICO</td><td>Realiz. Longo Prazo + Imobilizado</td><td class="text-end"><?= $fmtNum($anCiclico) ?></td></tr>
                <tr><td>PNC</td><td>PASSIVO NÃO CÍCLICO</td><td>Financ. Longo Prazo + Capital Social</td><td class="text-end"><?= $fmtNum(-$pnCiclico) ?></td></tr>
                <tr class="table-secondary"><td colspan="4"><strong>ATIVO E PASSIVO ERRÁTICOS</strong></td></tr>
                <tr><td>AE</td><td>ATIVO ERRÁTICO</td><td>Caixa + Equiv. de caixa</td><td class="text-end"><?= $fmtNum($atErratico) ?></td></tr>
                <tr><td>PE</td><td>PASSIVO ERRÁTICO</td><td>Outros Financiamentos de C.Prazo</td><td class="text-end"><?= $fmtNum(-$paErratico) ?></td></tr>
                <tr class="table-secondary"><td colspan="4"><strong>ANÁLISE FINANCEIRA (Fleuriet)</strong></td></tr>
                <tr><td>CDG</td><td>Capital de Giro</td><td>PNC - ANC</td><td class="text-end"><?= $fmtNum($CDG) ?></td></tr>
                <tr><td>NCG</td><td>Necessidade de Capital de Giro</td><td>AC - PC</td><td class="text-end"><?= $fmtNum($NCG) ?></td></tr>
                <tr><td>ST</td><td>Situação de Tesouraria</td><td>AE - PE</td><td class="text-end"><?= $fmtNum($atErratico + $paErratico) ?></td></tr>
                <tr><td>Situação</td><td></td><td><?= $sinalCDG ?> <?= $sinalNCG ?> <?= $sinalST ?></td><td class="text-end"><strong><?= $situacao ?></strong></td></tr>
                <tr class="table-secondary"><td colspan="4"><strong>ÍNDICES</strong></td></tr>
                <tr><td>LIQUIDEZ</td><td>Ideal > 1</td><td>Ativo C.Prazo/Passivo C.Prazo</td><td class="text-end"><?= $fmtPct($liquidez) ?></td></tr>
                <tr><td>COBERTURA</td><td>Ideal > 6</td><td>Ativo C.Prazo/Desp. Mensais</td><td class="text-end"><?= $fmtNum($cobertura) ?> (<?= round($coberturaDias) ?> dias)</td></tr>
                <tr><td>ENDIVIDAMENTO</td><td>Ideal ~0</td><td>Passivo Exigível/Ativo Total</td><td class="text-end"><?= $fmtPct(-$pasCiclico / -($A['subPassivo'] + $A['subPrincipal'])) ?></td></tr>
                <tr><td>POUPANÇA</td><td>Ideal > 10%</td><td>Resultado Disponível/Receitas</td><td class="text-end"><?= $fmtPct($poupanca) ?></td></tr>
                <tr class="table-secondary"><td colspan="4"><strong>PLANEJAMENTO FINANCEIRO</strong></td></tr>
                <tr><td>PMS</td><td>Patrimônio Mínimo de Sobrevivência</td><td>6 x Desp.Mensais</td><td class="text-end"><?= $fmtNum($pms) ?> (<?= $fmtIdade($ate(-$Despesas*6)) ?>)</td></tr>
                <tr><td>PMR</td><td>Patrimônio Mínimo Recomendado</td><td>20 x Desp.Mensais</td><td class="text-end"><?= $fmtNum($pmr) ?> (<?= $fmtIdade($ate(-$Despesas*20)) ?>)</td></tr>
                <tr><td>PI</td><td>Patrimônio Ideal</td><td>12 x Desp.Mensais x 10% x Idade</td><td class="text-end"><?= $fmtNum($pi) ?> (<?= $fmtIdade($ate((-$Despesas*12*0.1)*60)) ?>)</td></tr>
                <tr><td>PNIF</td><td>Patrim. Nec. p/ Indep. Financeira</td><td>12 x Desp.Mensais / 6%</td><td class="text-end"><?= $fmtNum($pnif) ?> (<?= $fmtIdade($ate(-$Despesas*12/0.06)) ?>)</td></tr>
                <tr class="table-secondary"><td colspan="4"><strong>DIAGNÓSTICO DO SISTEMA</strong></td></tr>
                <tr><td>SAÚDE FINANCEIRA</td><td><?= $labelLiq ?></td><td colspan="2"><?= $descLiq ?></td></tr>
                <tr><td>CAPITAL DE GIRO</td><td><?= $labelCG ?></td><td colspan="2"><?= $descCG ?></td></tr>
                <tr><td>CAPACIDADE DE ACÚMULO</td><td><?= $labelPoup ?></td><td colspan="2"><?= $descPoup ?></td></tr>
                <tr><td>DICA DO CONSULTOR</td><td><?= $labelDica ?></td><td colspan="2"><?= $descDica ?></td></tr>
            </tbody>
        </table>
    </div>
</div>
<?php endif; ?>

<?php include '../includes/footer.php'; ?>
