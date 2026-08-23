<?php
session_start();
ini_set('display_errors', 1);
error_reporting(E_ALL);
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$title = 'Receitas x Despesas';
include '../includes/header.php';

$db = Database::getInstance()->getConnection();
$erro = '';

// Período (dataIni/dataFim) e Regime (Vencimento=dtVcto / Caixa=dtApr / Competencia=dtEmi)
$dataIni = $_POST['data_ini'] ?? '';
$dataFim = $_POST['data_fim'] ?? '';
$regime  = $_POST['regime'] ?? 'vcto';   // vcto | caixa | competencia

$linhas = [];
$totais = [
    'receitasOperacionais' => 0.0, 'receitasNaoOperacionais' => 0.0,
    'despesasOperacionais' => 0.0, 'despesasNaoOperacionais' => 0.0,
    'totalReceitas' => 0.0, 'totalDespesas' => 0.0
];
$percentuaisHistoricos = [];
$diasPeriodo = 0;
$textoReceitaDias = '';
$textoDespesaDias = '';
$taxaConsumo = 'N/A';

$campoData = $regime === 'caixa' ? 'dtApr' : ($regime === 'competencia' ? 'dtEmi' : 'dtVcto');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (strlen($dataIni) != 10 || strlen($dataFim) != 10) {
        $erro = 'Atenção! Favor preencher corretamente as datas (dd/mm/yyyy).';
    } else {
        try {
            $di = dataParaSQL($dataIni);
            $df = dataParaSQL($dataFim);
            $dtIni = DateTime::createFromFormat('d/m/Y', $dataIni);
            $dtFim = DateTime::createFromFormat('d/m/Y', $dataFim);

            // 1) Totais do período por nome_S (espelha VConsRD.java linhas 344-396)
            // Normaliza maiúsculo + sem acento para bater independente de case/acento do banco.
            $norm = function($s){
                $s = mb_strtoupper(trim($s ?? ''), 'UTF-8');
                $map = [
                    'À'=>'A','Á'=>'A','Â'=>'A','Ã'=>'A','Ä'=>'A','Å'=>'A','Ç'=>'C',
                    'È'=>'E','É'=>'E','Ê'=>'E','Ë'=>'E','Ì'=>'I','Í'=>'I','Î'=>'I','Ï'=>'I',
                    'Ñ'=>'N','Ò'=>'O','Ó'=>'O','Ô'=>'O','Õ'=>'O','Ö'=>'O','Ù'=>'U','Ú'=>'U',
                    'Û'=>'U','Ü'=>'U','Ý'=>'Y','ª'=>'A','º'=>'O',
                    'à'=>'A','á'=>'A','â'=>'A','ã'=>'A','ä'=>'A','å'=>'A','ç'=>'C',
                    'è'=>'E','é'=>'E','ê'=>'E','ë'=>'E','ì'=>'I','í'=>'I','î'=>'I','ï'=>'I',
                    'ñ'=>'N','ò'=>'O','ó'=>'O','ô'=>'O','õ'=>'O','ö'=>'O','ù'=>'U','ú'=>'U',
                    'û'=>'U','ü'=>'U','ý'=>'Y'
                ];
                return strtr($s, $map);
            };
            $stmt2 = $db->prepare("
                SELECT nome_S, SUM(Valor) AS Total FROM viewmovrd
                WHERE $campoData BETWEEN ? AND ?
                  AND nome_P <> 'NULO'
                  AND classif NOT IN ('3.003.005','4.008.011')
                GROUP BY nome_S");
            $stmt2->execute([$di, $df]);
            while ($r = $stmt2->fetch()) {
                $nomeS = $norm($r['nome_S']);
                $total = (float)$r['Total'];
                if ($nomeS === 'RECEITAS OPERACIONAIS') {
                    $totais['receitasOperacionais'] += $total; $totais['totalReceitas'] += $total;
                } elseif (strpos($nomeS, 'RECEITAS N') === 0) {
                    $totais['receitasNaoOperacionais'] += $total; $totais['totalReceitas'] += $total;
                } elseif ($nomeS === 'DESPESAS OPERACIONAIS') {
                    $totais['despesasOperacionais'] += $total; $totais['totalDespesas'] += $total;
                } elseif (strpos($nomeS, 'DESPESAS N') === 0) {
                    $totais['despesasNaoOperacionais'] += $total; $totais['totalDespesas'] += $total;
                }
            }

            // 2) Média histórica (últimos 24 meses) por nome_C (linhas 403-449)
            // clone para não mutar $dtFim (DateTime é mutável)
            $dataInicioHist = (clone $dtFim)->modify('-24 months')->format('Y-m-d');
            $stmtH = $db->prepare("SELECT nome_S, SUM(Valor) AS Total FROM viewmovrd WHERE nome_P <> 'NULO' AND $campoData >= ? GROUP BY nome_S");
            $stmtH->execute([$dataInicioHist]);
            $totRecHist = 0.0; $totDespHist = 0.0;
            while ($r = $stmtH->fetch()) {
                $nomeS = mb_strtoupper(trim($r['nome_S'] ?? ''));
                $tot = (float)$r['Total'];
                if (strpos($nomeS, 'RECEITAS') === 0) $totRecHist += $tot;
                elseif (strpos($nomeS, 'DESPESAS') === 0) $totDespHist += $tot;
            }
            $stmtHC = $db->prepare("SELECT nome_C, SUM(Valor) AS Total FROM viewmovrd WHERE nome_P <> 'NULO' AND $campoData >= ? GROUP BY nome_C");
            $stmtHC->execute([$dataInicioHist]);
            while ($r = $stmtHC->fetch()) {
                $nomeC = $r['nome_C'] ?? '';
                $tot = (float)$r['Total'];
                if ($tot > 0 && $totRecHist != 0) $percentuaisHistoricos[$nomeC] = ($tot / $totRecHist) * 100;
                elseif ($tot < 0 && $totDespHist != 0) $percentuaisHistoricos[$nomeC] = ($tot / $totDespHist) * 100;
                else $percentuaisHistoricos[$nomeC] = 0.0;
            }

            // 3) Consulta principal (linhas 468-480)
            $stmt = $db->prepare("
                SELECT nome_P, nome_S, nome_C, SUM(Valor) AS Total
                FROM viewmovrd
                WHERE $campoData BETWEEN ? AND ?
                  AND nome_P <> 'NULO'
                  AND classif NOT IN ('3.003.005','4.008.011')
                GROUP BY nome_P, nome_S, nome_C
                ORDER BY classif");
            $stmt->execute([$di, $df]);
            $rows = $stmt->fetchAll();

            // Renderização (linhas 505-547)
            $sa = 0.0; $lastPrin = ''; $prin = ''; $sub = '';
            $linhas = [];
            foreach ($rows as $m) {
                $nomeC = $m['nome_C'] ?? '';
                $total = (float)$m['Total'];
                $percent = 0.0;
                if ($total > 0) $percent = $totais['totalReceitas'] != 0 ? ($total / $totais['totalReceitas']) * 100 : 0.0;
                elseif ($total < 0) $percent = $totais['totalDespesas'] != 0 ? ($total / $totais['totalDespesas']) * 100 : 0.0;
                $sa += $total;

                $p_prin = '';
                $p_sub = '';
                if ($prin === ($m['nome_P'] ?? '')) $p_prin = ' '; else $p_prin = $m['nome_P'] ?? '';
                if ($sub === ($m['nome_S'] ?? '')) $p_sub = ' '; else $p_sub = $m['nome_S'] ?? '';

                // linha separadora quando muda Principal
                if ($lastPrin !== '' && $lastPrin !== ($m['nome_P'] ?? '')) {
                    $linhas[] = ['', '', '', '', '', '', ''];
                }

                $linhas[] = [
                    $p_prin,
                    $p_sub,
                    $nomeC,
                    $total,
                    $sa,
                    $percent,
                    $percentuaisHistoricos[$nomeC] ?? 0.0
                ];
                $lastPrin = $m['nome_P'] ?? '';
                $prin = $m['nome_P'] ?? '';
                $sub = $m['nome_S'] ?? '';
            }

            // Rodapé (linhas 549-578) — valores capturados em variaveis dedicadas
            $rodape = [];
            $taxaConsumo = $totais['totalReceitas'] != 0
                ? number_format((-$totais['totalDespesas'] / $totais['totalReceitas']) * 100, 4, '.', '')
                : 'N/A';
            $diasPeriodo = $dtIni->diff($dtFim)->days + 1;
            $receitaPorDia = $totais['totalReceitas'] != 0 ? $totais['totalReceitas'] / $diasPeriodo : 0.0;
            $diasConsumidos = $receitaPorDia != 0 ? $totais['totalDespesas'] / $receitaPorDia : 0.0;
            $textoReceitaDias = (int)round($receitaPorDia) . ' dias';
            $textoDespesaDias = -(int)round($diasConsumidos) . ' dias';

            $rodape[] = ['', '', 'TAXA DE CONSUMO', '', $taxaConsumo, ''];
            $rodape[] = ['', '', '', '', '', ''];
            $rodape[] = ['', '', 'TOTAL DIAS NO PERÍODO: ' . $diasPeriodo . ' dias', '', '', ''];
            $rodape[] = ['', '', 'DIAS EQUIVALENTES CONSUMIDOS: ' . $textoDespesaDias, '', '', ''];
            $rodape[] = ['', '', '', '', '', ''];
            $rodape[] = ['', '', 'RECEITAS OPERACIONAIS', formatMoeda($totais['receitasOperacionais']), '', ''];
            $rodape[] = ['', '', 'DESPESAS OPERACIONAIS', formatMoeda($totais['despesasOperacionais']), '', ''];
            $rodape[] = ['', '', 'RESULTADO OPERACIONAL (RECEITAS-DESPESAS)', formatMoeda($totais['receitasOperacionais'] + $totais['despesasOperacionais']), '', ''];
            $rodape[] = ['', '', '', '', '', ''];
            $rodape[] = ['', '', 'RECEITAS NÃO OPERACIONAIS', formatMoeda($totais['receitasNaoOperacionais']), '', ''];
            $rodape[] = ['', '', 'DESPESAS NÃO OPERACIONAIS', formatMoeda($totais['despesasNaoOperacionais']), '', ''];
            $rodape[] = ['', '', 'RESULTADO NÃO OPERACIONAL', formatMoeda($totais['receitasNaoOperacionais'] + $totais['despesasNaoOperacionais']), '', ''];
            $rodape[] = ['', '', '', '', '', ''];
            $rodape[] = ['', '', 'RESULTADO FINAL DO PERÍODO:', formatMoeda(
                $totais['receitasOperacionais'] + $totais['despesasOperacionais'] +
                $totais['receitasNaoOperacionais'] + $totais['despesasNaoOperacionais']), '', ''];
        } catch (Exception $e) {
            $erro = 'Erro na consulta: ' . $e->getMessage();
        }
    }
}

function rdx_money($v) { return formatMoeda($v); }
function rdx_pct($v) { return ($v === '' || $v === null) ? '' : number_format((float)$v, 4, ',', '') . '%'; }
?>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="page-title mb-0"><i class="bi bi-bar-chart"></i> Receitas x Despesas</h4>
</div>

<?php if ($erro): ?>
<div class="alert alert-danger"><?= htmlspecialchars($erro) ?></div>
<?php endif; ?>

<form method="POST" class="row g-3 mb-3">
    <div class="col-md-2">
        <label class="form-label">Período de Vencimentos — de</label>
        <input type="text" name="data_ini" class="form-control datepicker" value="<?= htmlspecialchars($dataIni) ?>" placeholder="dd/mm/yyyy" required>
    </div>
    <div class="col-md-2">
        <label class="form-label">até</label>
        <input type="text" name="data_fim" class="form-control datepicker" value="<?= htmlspecialchars($dataFim) ?>" placeholder="dd/mm/yyyy" required>
    </div>
    <div class="col-md-5">
        <label class="form-label d-block">Regime</label>
        <div class="btn-group" role="group">
            <input type="radio" class="btn-check" name="regime" id="regVcto" value="vcto" <?= $regime==='vcto'?'checked':'' ?>>
            <label class="btn btn-outline-secondary" for="regVcto">Vencimento</label>
            <input type="radio" class="btn-check" name="regime" id="regCaixa" value="caixa" <?= $regime==='caixa'?'checked':'' ?>>
            <label class="btn btn-outline-secondary" for="regCaixa">Regime de Caixa</label>
            <input type="radio" class="btn-check" name="regime" id="regComp" value="competencia" <?= $regime==='competencia'?'checked':'' ?>>
            <label class="btn btn-outline-secondary" for="regComp">Competência</label>
        </div>
    </div>
    <div class="col-md-1 d-flex align-items-end">
        <button type="submit" class="btn btn-primary w-100">Pesquisar</button>
    </div>
</form>

<?php if ($_SERVER['REQUEST_METHOD'] === 'POST' && empty($erro)): ?>
<div class="card">
    <div class="card-header"><i class="bi bi-table"></i> Detalhamento (Principal / Grupo / Conta / Total / Acumulado / % / Média Histórica)</div>
    <div class="card-body">
        <?php if (isset($diasPeriodo)): ?>
        <div class="alert alert-secondary py-2 mb-2 small">
            <strong>Depuração:</strong> Período <?= htmlspecialchars($dataIni) ?> a <?= htmlspecialchars($dataFim) ?>
            | Regime: <?= htmlspecialchars($campoData) ?>
            | Registros (detalhamento): <?= count($linhas) ?>
            | RecOp: <?= formatMoeda($totais['receitasOperacionais']) ?>
            | DespOp: <?= formatMoeda($totais['despesasOperacionais']) ?>
            | Total Rec: <?= formatMoeda($totais['totalReceitas']) ?>
            | Total Desp: <?= formatMoeda($totais['totalDespesas']) ?>
        </div>
        <?php endif; ?>
        <?php if (empty($linhas)): ?>
            <div class="alert alert-info mb-0">Nenhum registro encontrado no período informado.</div>
        <?php else: ?>
        <div class="table-responsive">
            <table class="table table-sm table-hover datatable">
                <thead>
                    <tr>
                        <th>PRINCIPAL</th>
                        <th>GRUPO</th>
                        <th>CONTA</th>
                        <th class="text-end">TOTAL CONTA</th>
                        <th class="text-end">ACUMULADO</th>
                        <th class="text-end">PERCENTUAL</th>
                        <th class="text-end">MÉDIA HISTÓRICA (%)</th>
                    </tr>
                </thead>
                <tbody>
                <?php foreach ($linhas as $l):
                    $vazio = ($l[0]==='' && $l[1]==='' && $l[2]==='' && $l[3]==='');
                    if ($vazio): ?>
                    <tr class="table-secondary"><td colspan="7"></td></tr>
                    <?php continue; endif;
                    $isResumo = ($l[1]==='' && $l[2]!=='' && ($l[3]===''));
                ?>
                    <tr class="<?= $isResumo ? 'table-light fw-semibold' : '' ?>">
                        <td><?= htmlspecialchars($l[0]) ?></td>
                        <td><?= htmlspecialchars($l[1]) ?></td>
                        <td><?= htmlspecialchars($l[2]) ?></td>
                        <td class="text-end <?= ($l[3] ?? 0) < 0 ? 'valor-negativo' : 'valor-positivo' ?>"><?= rdx_money($l[3]) ?></td>
                        <td class="text-end <?= ($l[4] ?? 0) < 0 ? 'valor-negativo' : 'valor-positivo' ?>"><?= rdx_money($l[4]) ?></td>
                        <td class="text-end"><?= rdx_pct($l[5]) ?></td>
                        <td class="text-end"><?= rdx_pct($l[6]) ?></td>
                    </tr>
                <?php endforeach; ?>
                <?php if (!empty($rodape)): foreach ($rodape as $l):
                    $vazio = ($l[0]==='' && $l[1]==='' && $l[2]==='' && $l[3]==='');
                    if ($vazio): ?><tr class="table-secondary"><td colspan="7"></td></tr><?php continue; endif;
                    $isResumo = ($l[1]==='' && $l[2]!=='' && ($l[3]===''));
                ?>
                    <tr class="<?= $isResumo ? 'table-light fw-semibold' : '' ?>">
                        <td><?= htmlspecialchars($l[0]) ?></td>
                        <td><?= htmlspecialchars($l[1]) ?></td>
                        <td><?= htmlspecialchars($l[2]) ?></td>
                        <td class="text-end <?= is_numeric($l[3] ?? '') && $l[3] < 0 ? 'valor-negativo' : 'valor-positivo' ?>"><?= htmlspecialchars($l[3]) ?></td>
                        <td class="text-end"><?= htmlspecialchars($l[4]) ?></td>
                        <td class="text-end"><?= htmlspecialchars($l[5]) ?></td>
                    </tr>
                <?php endforeach; endif; ?>
                </tbody>
            </table>
        </div>
        <?php endif; ?>
    </div>
</div>
<?php endif; ?>

<?php include '../includes/footer.php'; ?>
