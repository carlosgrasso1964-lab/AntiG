<?php
session_start();
ini_set('display_errors', 1);
error_reporting(E_ALL);
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$title = 'Referência Cruzada';
include '../includes/header.php';

$db = Database::getInstance()->getConnection();
$erro = '';

$dataIni = $_POST['data_ini'] ?? '';
$dataFim = $_POST['data_fim'] ?? '';
$status  = $_POST['status'] ?? 'todos';   // todos | conf | prev

// Meses (índices 0..11) e nomes de coluna
$meses = ['janeiro','fevereiro','marco','abril','maio','junho','julho','agosto','setembro','outubro','novembro','dezembro'];
$colMeses = ['Janeiro','Fevereiro','Março','Abril','Maio','Junho','Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'];

$linhas = [];           // cada linha = array de 14 células (string formatada ou '')
$contas = [];           // nome_C => [0..11 valores, 12=total]

// Inicializa arrays usados no gráfico (evita "Undefined variable" dentro do <script> em GET)
$totaisReceitas = $totaisDespesas = $totaisEmpObtidos = $totaisEmpPagos = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (strlen($dataIni) != 10 || strlen($dataFim) != 10) {
        $erro = 'Atenção! Favor preencher corretamente as datas (dd/mm/yyyy).';
    } else {
        try {
            // Converter dd/mm/yyyy -> yyyy-mm-dd
            $di = DateTime::createFromFormat('d/m/Y', $dataIni);
            $df = DateTime::createFromFormat('d/m/Y', $dataFim);
            if (!$di || !$df) throw new Exception('Data inválida.');
            $diF = $dfi = $di->format('Y-m-d');
            $dfF = $df->format('Y-m-d');

            // Dia anterior ao início (para saldo anterior)
            $diaAnt = clone $di; $diaAnt->modify('-1 day');
            $diaAntF = $diaAnt->format('Y-m-d');

            // Data de hoje
            $hoje = new DateTime();
            $hojeF = $hoje->format('Y-m-d');

            // Filtro de status (espelha Java: Conf=Prev 'V', Prev=Prev 'F')
            $filtroStatus = '';
            if ($status === 'conf') $filtroStatus = " AND Prev = 'V' ";
            elseif ($status === 'prev') $filtroStatus = " AND Prev = 'F' ";

            // === SALDO ANTERIOR (transporte) ===
            // Período no passado? (dia anterior < hoje)
            $periodoNoPassado = ($diaAnt < $hoje);

            $saldoCaixaBancoAnterior = 0.0;
            $saldoEmprestimoAnterior = 0.0;

            if ($periodoNoPassado) {
                // Saldo LIMPO até dia anterior (efetivados)
                $sqlSaldoCaixa =
                    "SELECT COALESCE(SUM(Valor),0) AS saldo FROM tbmovimento
                     WHERE dtlancto <= ? AND vrecurso IN ('1.001.001','1.001.002','1.003.001','2.001.004')
                       AND recurso NOT IN ('0079','0022','0080')
                       AND dtApr IS NOT NULL AND TRIM(COALESCE(dtApr,'')) <> ''";
                $st = $db->prepare($sqlSaldoCaixa);
                $st->execute([$diaAntF]);
                $saldoCaixaBancoAnterior = (float)$st->fetchColumn();

                // Saldo de empréstimo (recurso 0080)
                $sqlEmp = "SELECT COALESCE(SUM(Valor),0) AS saldo FROM tbmovimento WHERE recurso = '0080' AND dtlancto <= ?";
                $st = $db->prepare($sqlEmp);
                $st->execute([$diaAntF]);
                $saldoEmprestimoAnterior = (float)$st->fetchColumn();
            } else {
                // Período atual/futuro: C1 (caixa até hoje) + C2 (projeções confirmadas vencidas até dia ant) + C4 (empréstimos)
                $sqlC1 =
                    "SELECT COALESCE(SUM(Valor),0) AS saldo FROM tbmovimento
                     WHERE vrecurso IN ('1.001.001','1.001.002','1.003.001','2.001.004')
                       AND dtlancto <= ? AND dtApr IS NOT NULL AND TRIM(COALESCE(dtApr,'')) <> ''
                       AND recurso NOT IN ('0079','0022')";
                $st = $db->prepare($sqlC1);
                $st->execute([$hojeF]);
                $saldoCaixaBancoAnterior += (float)$st->fetchColumn();

                $sqlC2 =
                    "SELECT COALESCE(SUM(Valor),0) AS saldo FROM tbmovimento
                     WHERE vrecurso IN ('1.002.001','2.001.002','2.001.003') AND dtVcto <= ? AND statusMov = ''";
                $st = $db->prepare($sqlC2);
                $st->execute([$diaAntF]);
                $saldoCaixaBancoAnterior += (float)$st->fetchColumn();

                $sqlC4 = "SELECT COALESCE(SUM(Valor),0) AS saldo FROM tbmovimento WHERE recurso = '0080' AND dtlancto <= ?";
                $st = $db->prepare($sqlC4);
                $st->execute([$diaAntF]);
                $saldoEmprestimoAnterior = (float)$st->fetchColumn();
            }

            $saldoInicialPeriodo = $saldoCaixaBancoAnterior;     // limpo
            $saldoInicialEmprestimo = $saldoEmprestimoAnterior;  // passivo (negativo no acúmulo)

            // === CONSULTA PRINCIPAL: CONTAS (viewmovrd) ===
            $sql =
                "SELECT nome_C AS Contas,
                    SUM(CASE WHEN MONTH(dtVcto) = '01' THEN Valor ELSE 0 END) AS janeiro,
                    SUM(CASE WHEN MONTH(dtVcto) = '02' THEN Valor ELSE 0 END) AS fevereiro,
                    SUM(CASE WHEN MONTH(dtVcto) = '03' THEN Valor ELSE 0 END) AS marco,
                    SUM(CASE WHEN MONTH(dtVcto) = '04' THEN Valor ELSE 0 END) AS abril,
                    SUM(CASE WHEN MONTH(dtVcto) = '05' THEN Valor ELSE 0 END) AS maio,
                    SUM(CASE WHEN MONTH(dtVcto) = '06' THEN Valor ELSE 0 END) AS junho,
                    SUM(CASE WHEN MONTH(dtVcto) = '07' THEN Valor ELSE 0 END) AS julho,
                    SUM(CASE WHEN MONTH(dtVcto) = '08' THEN Valor ELSE 0 END) AS agosto,
                    SUM(CASE WHEN MONTH(dtVcto) = '09' THEN Valor ELSE 0 END) AS setembro,
                    SUM(CASE WHEN MONTH(dtVcto) = '10' THEN Valor ELSE 0 END) AS outubro,
                    SUM(CASE WHEN MONTH(dtVcto) = '11' THEN Valor ELSE 0 END) AS novembro,
                    SUM(CASE WHEN MONTH(dtVcto) = '12' THEN Valor ELSE 0 END) AS dezembro,
                    SUM(CASE WHEN Valor != 0 THEN Valor ELSE 0 END) AS total
                 FROM viewmovrd
                 WHERE dtVcto BETWEEN ? AND ? AND nome_P <> 'NULO' $filtroStatus
                   AND ((classif NOT IN ('3.003.005','4.008.011'))
                        OR (classif = '4.008.011' AND (dtApr IS NULL OR TRIM(dtApr) = '')))
                 GROUP BY nome_C ORDER BY nome_C";
            $st = $db->prepare($sql);
            $st->execute([$diF, $dfF]);
            while ($r = $st->fetch(PDO::FETCH_ASSOC)) {
                $contas[$r['Contas']] = [
                    (float)$r['janeiro'], (float)$r['fevereiro'], (float)$r['marco'],
                    (float)$r['abril'], (float)$r['maio'], (float)$r['junho'],
                    (float)$r['julho'], (float)$r['agosto'], (float)$r['setembro'],
                    (float)$r['outubro'], (float)$r['novembro'], (float)$r['dezembro'],
                    (float)$r['total']
                ];
            }

            // === RECEITAS / DESPESAS / EMPRÉSTIMOS ===
            $mesCols = '';
            for ($i = 1; $i <= 12; $i++) {
                $mesCols .= "ROUND(SUM(CASE WHEN MONTH(dtVcto) = $i AND Valor > 0 AND classif LIKE '3.%' AND classif != '3.003.005' THEN Valor ELSE 0 END),2) AS m$i,";
            }
            $stR = $db->prepare("SELECT $mesCols ROUND(SUM(CASE WHEN Valor > 0 AND classif LIKE '3.%' AND classif != '3.003.005' THEN Valor ELSE 0 END),2) AS tot FROM viewmovrd WHERE dtVcto BETWEEN ? AND ? AND nome_P <> 'NULO' $filtroStatus");
            $stR->execute([$diF, $dfF]);
            $rowR = $stR->fetch(PDO::FETCH_ASSOC);
            $totaisReceitas = []; $totalReceitas = 0;
            for ($i = 0; $i < 12; $i++) { $totaisReceitas[$i] = (float)($rowR['m'.($i+1)] ?? 0); $totalReceitas += $totaisReceitas[$i]; }

            $mesCols = '';
            for ($i = 1; $i <= 12; $i++) {
                $mesCols .= "ROUND(SUM(CASE WHEN MONTH(dtVcto) = $i AND Valor < 0 AND classif LIKE '4.%' AND classif != '4.008.011' THEN Valor ELSE 0 END),2) AS m$i,";
            }
            $stD = $db->prepare("SELECT $mesCols ROUND(SUM(CASE WHEN Valor < 0 AND classif LIKE '4.%' AND classif != '4.008.011' THEN Valor ELSE 0 END),2) AS tot FROM viewmovrd WHERE dtVcto BETWEEN ? AND ? AND nome_P <> 'NULO' $filtroStatus");
            $stD->execute([$diF, $dfF]);
            $rowD = $stD->fetch(PDO::FETCH_ASSOC);
            $totaisDespesas = []; $totalDespesas = 0;
            for ($i = 0; $i < 12; $i++) { $totaisDespesas[$i] = (float)($rowD['m'.($i+1)] ?? 0); $totalDespesas += $totaisDespesas[$i]; }

            // Empréstimos Obtidos (3.003.005, ABS, dtApr NOT NULL) - tbmovimento
            $mesCols = '';
            for ($i = 1; $i <= 12; $i++) {
                $mesCols .= "ROUND(SUM(CASE WHEN MONTH(dtVcto) = $i AND classif = '3.003.005' THEN ABS(Valor) ELSE 0 END),2) AS m$i,";
            }
            $stEO = $db->prepare("SELECT $mesCols ROUND(SUM(CASE WHEN classif = '3.003.005' THEN ABS(Valor) ELSE 0 END),2) AS tot FROM tbmovimento WHERE dtVcto BETWEEN ? AND ? AND dtApr IS NOT NULL $filtroStatus");
            $stEO->execute([$diF, $dfF]);
            $rowEO = $stEO->fetch(PDO::FETCH_ASSOC);
            $totaisEmpObtidos = []; $totalEmpObtidos = 0;
            for ($i = 0; $i < 12; $i++) { $totaisEmpObtidos[$i] = (float)($rowEO['m'.($i+1)] ?? 0); $totalEmpObtidos += $totaisEmpObtidos[$i]; }

            // Empréstimos Pagos (4.008.011, -ABS, dtApr NOT NULL) - tbmovimento
            $mesCols = '';
            for ($i = 1; $i <= 12; $i++) {
                $mesCols .= "ROUND(SUM(CASE WHEN MONTH(dtVcto) = $i AND classif = '4.008.011' THEN -ABS(Valor) ELSE 0 END),2) AS m$i,";
            }
            $stEP = $db->prepare("SELECT $mesCols ROUND(SUM(CASE WHEN classif = '4.008.011' THEN -ABS(Valor) ELSE 0 END),2) AS tot FROM tbmovimento WHERE dtVcto BETWEEN ? AND ? AND dtApr IS NOT NULL $filtroStatus");
            $stEP->execute([$diF, $dfF]);
            $rowEP = $stEP->fetch(PDO::FETCH_ASSOC);
            $totaisEmpPagos = []; $totalEmpPagos = 0;
            for ($i = 0; $i < 12; $i++) { $totaisEmpPagos[$i] = (float)($rowEP['m'.($i+1)] ?? 0); $totalEmpPagos += $totaisEmpPagos[$i]; }

            // === CÁLCULO ACUMULATIVO (espelha Java) ===
            $mesInicio = (int)$di->format('m');
            $mesFim = (int)$df->format('m');

            $saldosIniciais = array_fill(0,12,0.0);
            $saldosFinais = array_fill(0,12,0.0);
            $saldoAcumEmprestimo = array_fill(0,12,0.0);
            $saldoInicialCaixaBanco = $saldoInicialPeriodo;
            $saldoBaseEmprestimo = $saldoInicialEmprestimo;

            for ($i = 0; $i < 12; $i++) {
                $mesAtual = $i + 1;
                if ($mesAtual >= $mesInicio && $mesAtual <= $mesFim) {
                    $saldosIniciais[$i] = ($mesAtual == $mesInicio) ? $saldoInicialCaixaBanco : $saldosFinais[$i-1];
                    $resultadoMes = $totaisReceitas[$i] + $totaisDespesas[$i];
                    $saldosFinais[$i] = $saldosIniciais[$i] + $resultadoMes;

                    $emprestimoMes = $totaisEmpObtidos[$i] + $totaisEmpPagos[$i];
                    if ($i == 0) {
                        $saldoAcumEmprestimo[$i] = $saldoBaseEmprestimo + $emprestimoMes;
                    } else {
                        $saldoAcumEmprestimo[$i] = $saldoAcumEmprestimo[$i-1] + $emprestimoMes;
                    }
                } else {
                    $saldosIniciais[$i] = $saldosFinais[$i] = $saldoAcumEmprestimo[$i] = 0.0;
                }
            }

            // === MONTAR LINHAS DA TABELA ===
            $fmt = function($v) { return $v === '' || $v === null ? '' : formatMoeda($v); };

            // Linha: Saldo Anterior (Cx/Bcos/Poup) S/Empr.
            $saldoAnteriorTotal = $saldoInicialPeriodo; // + saldoInicialEmprestimo (Java comenta essa soma)
            $linhaSA = array_fill(0,14,'');
            $linhaSA[0] = 'Saldo Anterior(Cx/Bcos/Poup)S/Empr.';
            for ($i = 0; $i < 12; $i++) {
                $mesAtual = $i + 1;
                if ($mesAtual >= $mesInicio && $mesAtual <= $mesFim) {
                    if ($mesAtual == $mesInicio) {
                        $linhaSA[$i+1] = $fmt($saldoAnteriorTotal);
                    } else {
                        $linhaSA[$i+1] = $fmt($saldosFinais[$i-1] + $saldoAcumEmprestimo[$i-1]);
                    }
                }
            }
            $linhaSA[13] = $fmt($saldoAnteriorTotal);
            $linhas[] = $linhaSA;
            $linhas[] = array_fill(0,14,''); // linha em branco

            // Linhas de Contas
            foreach ($contas as $nome => $vals) {
                $row = [$nome];
                for ($i = 0; $i < 12; $i++) $row[] = $fmt($vals[$i]);
                $row[] = $fmt($vals[12]);
                $linhas[] = $row;
            }
            $linhas[] = array_fill(0,14,''); // linha em branco

            // Receitas / Despesas / Resultado
            $mkRow = function($label, $arr, $tot) use ($fmt, $mesInicio, $mesFim) {
                $row = [$label];
                for ($i = 0; $i < 12; $i++) {
                    $mesAtual = $i + 1;
                    $row[] = ($mesAtual >= $mesInicio && $mesAtual <= $mesFim) ? $fmt($arr[$i]) : '';
                }
                $row[] = $fmt($tot);
                return $row;
            };
            // Receitas e Despesas aparecem em todos os meses do período (Java não restringe por mês nelas)
            $linhas[] = $mkRow('Receitas', $totaisReceitas, $totalReceitas);
            $linhas[] = $mkRow('Despesas', $totaisDespesas, $totalDespesas);
            $linhas[] = $mkRow('Resultado', array_map(function($a,$b){return $a+$b;}, $totaisReceitas, $totaisDespesas), $totalReceitas + $totalDespesas);
            $linhas[] = array_fill(0,14,''); // linha em branco

            // Saldo Inicial Empréstimo
            $linhaSE = array_fill(0,14,'');
            $linhaSE[0] = 'Saldo Inicial Empréstimo';
            $valExibir = $saldoInicialEmprestimo <= 0 ? $saldoInicialEmprestimo : -$saldoInicialEmprestimo;
            for ($i = 0; $i < 12; $i++) {
                if (($i+1) == $mesInicio) $linhaSE[$i+1] = $fmt($valExibir);
            }
            $linhaSE[13] = $fmt($valExibir);
            $linhas[] = $linhaSE;

            // Empréstimos Obtidos (Recebidos) - apresentar negativo (inverte sinal)
            $mkEmp = function($label, $arr, $tot) use ($fmt) {
                $row = [$label];
                for ($i = 0; $i < 12; $i++) $row[] = $fmt(-$arr[$i]); // inverte sinal
                $row[] = $fmt(-$tot);
                return $row;
            };
            $linhas[] = $mkEmp('Emprést.Obt.C.Prazo-Recebidos', $totaisEmpObtidos, $totalEmpObtidos);
            $linhas[] = $mkEmp('Emprést.Obt.C.Prazo-Pagos', $totaisEmpPagos, $totalEmpPagos);

            // Saldo Empréstimos C.Prazo (força negativo)
            $fmtNeg = function($v) use ($fmt) { $v = ($v <= 0) ? $v : -$v; return $fmt($v); };
            $rowSEmp = ['Saldo Empréstimos C.Prazo'];
            for ($i = 0; $i < 12; $i++) {
                $mesAtual = $i + 1;
                $rowSEmp[] = ($mesAtual >= $mesInicio && $mesAtual <= $mesFim) ? $fmtNeg($saldoAcumEmprestimo[$i]) : '';
            }
            $rowSEmp[] = $fmtNeg($saldoAcumEmprestimo[$mesFim-1]);
            $linhas[] = $rowSEmp;
            $linhas[] = array_fill(0,14,''); // linha em branco

            // Saldo Final (Cx/Bcos/Poup) S/Empréstimos
            $linhaSF = ['Saldo Final (Cx/Bcos/Poup)S/Empréstimos'];
            for ($i = 0; $i < 12; $i++) {
                $mesAtual = $i + 1;
                $linhaSF[] = ($mesAtual >= $mesInicio && $mesAtual <= $mesFim) ? $fmt($saldosFinais[$i]) : '';
            }
            $linhaSF[] = $fmt($saldosFinais[$mesFim-1]);
            $linhas[] = $linhaSF;

            // Saldo Total Disponível (C/Empréstimos)
            $linhaST = ['Saldo Total Disponível(C/Empréstimos)'];
            for ($i = 0; $i < 12; $i++) {
                $mesAtual = $i + 1;
                $linhaST[] = ($mesAtual >= $mesInicio && $mesAtual <= $mesFim) ? $fmt($saldosFinais[$i] + $saldoAcumEmprestimo[$i]) : '';
            }
            $linhaST[] = $fmt($saldosFinais[$mesFim-1] + $saldoAcumEmprestimo[$mesFim-1]);
            $linhas[] = $linhaST;

            // === AÇÕES: SALVAR PREVISTOS (tb_plano_diretor) ===
            $acao = $_POST['acao'] ?? '';
            if ($acao === 'salvar_prev') {
                $inflacao = isset($_POST['inflacao']) ? (float)str_replace(',', '.', $_POST['inflacao']) : 0;
                $anoAtual = (int)$di->format('Y');
                $stIns = $db->prepare("INSERT INTO tb_plano_diretor (ano_referencia, mes, conta, valor_planejado, inflacao_premissa) VALUES (?, ?, ?, ?, ?)");
                $gravados = 0;
                foreach ($contas as $nome => $vals) {
                    for ($m = 1; $m <= 12; $m++) {
                        $valor = $vals[$m - 1];
                        if ($valor != 0) {
                            $stIns->execute([$anoAtual, $m, $nome, $valor, $inflacao]);
                            $gravados++;
                        }
                    }
                }
                setFlash('success', "Plano diretor salvo: $gravados lançamentos (ano $anoAtual, inflação " . str_replace('.', ',', (string)$inflacao) . "%).");
            }

        } catch (Exception $e) {
            $erro = 'Erro ao consultar dados: ' . $e->getMessage();
        }
    }
}
?>
<div class="container-fluid mt-3">
    <h4 class="page-title"><i class="bi bi-intersect"></i> Consulta - Referência Cruzada</h4>

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
        <div class="col-md-4 d-flex align-items-end gap-2">
            <button type="submit" class="btn btn-primary"><i class="bi bi-search"></i> Pesquisar</button>
            <button type="button" class="btn btn-outline-secondary" id="btnAnual">Anual</button>
            <a href="referencia_cruzada.php" class="btn btn-outline-secondary">Limpar</a>
        </div>
        <div class="col-md-4 d-flex align-items-end gap-2 flex-wrap">
            <button type="button" class="btn btn-outline-info" id="btnGrafico"><i class="bi bi-bar-chart"></i> Gráfico</button>
            <button type="button" class="btn btn-outline-success" id="btnExportar"><i class="bi bi-file-earmark-excel"></i> Exportar</button>
            <button type="button" class="btn btn-outline-dark" id="btnImprimir"><i class="bi bi-printer"></i> Imprimir</button>
            <button type="button" class="btn btn-outline-warning" id="btnSalvarPrev"><i class="bi bi-save"></i> Salvar Previstos</button>
            <input type="hidden" name="acao" id="acao" value="">
            <input type="hidden" name="inflacao" id="inflacao" value="">
        </div>
        <div class="col-md-4 d-flex align-items-end">
            <div class="btn-group" role="group" aria-label="Status">
                <input type="radio" class="btn-check" name="status" id="st_todos" value="todos" <?= $status==='todos'?'checked':'' ?>>
                <label class="btn btn-outline-secondary" for="st_todos">Todos</label>
                <input type="radio" class="btn-check" name="status" id="st_conf" value="conf" <?= $status==='conf'?'checked':'' ?>>
                <label class="btn btn-outline-secondary" for="st_conf">Confirmados</label>
                <input type="radio" class="btn-check" name="status" id="st_prev" value="prev" <?= $status==='prev'?'checked':'' ?>>
                <label class="btn btn-outline-secondary" for="st_prev">Previstos</label>
            </div>
        </div>
    </form>

    <?php if ($_SERVER['REQUEST_METHOD'] === 'POST' && !$erro && !empty($linhas)): ?>
    <div class="card">
        <div class="card-header"><i class="bi bi-table"></i> Referência Cruzada — Período: <?= htmlspecialchars($dataIni) ?> a <?= htmlspecialchars($dataFim) ?></div>
        <div class="card-body table-responsive">
            <table id="tabelaRC" class="table table-sm table-bordered">
                <thead>
                    <tr>
                        <th>Contas</th>
                        <?php foreach ($colMeses as $m): ?><th class="text-end"><?= $m ?></th><?php endforeach; ?>
                        <th class="text-end">Total</th>
                    </tr>
                </thead>
                <tbody>
                    <?php foreach ($linhas as $row): ?>
                    <tr>
                        <?php foreach ($row as $idx => $cell): ?>
                            <?php if ($idx == 0): ?>
                                <td><strong><?= htmlspecialchars($cell) ?></strong></td>
                            <?php else: ?>
                                <td class="text-end"><?= htmlspecialchars($cell) ?></td>
                            <?php endif; ?>
                        <?php endforeach; ?>
                    </tr>
                    <?php endforeach; ?>
                </tbody>
            </table>
        </div>
    </div>
    <div class="card mt-3" id="cardGrafico" style="display:none;">
        <div class="card-header"><i class="bi bi-bar-chart"></i> Gráfico — Receitas / Despesas / Resultado</div>
        <div class="card-body"><canvas id="graficoRC" height="120"></canvas></div>
    </div>
    <?php elseif ($_SERVER['REQUEST_METHOD'] === 'POST' && !$erro): ?>
    <div class="alert alert-info">Nenhum dado encontrado para o período informado.</div>
    <?php endif; ?>
</div>

<script>
function initRC(){
    if (window.__rcInited) return;
    window.__rcInited = true;
    if (typeof $ === 'undefined') return; // jQuery do footer ainda não carregou
    $('#btnAnual').on('click', function(){
        var y = new Date().getFullYear();
        $('input[name="data_ini"]').val('01/01/'+y);
        $('input[name="data_fim"]').val('31/12/'+y);
        $('form').submit();
    });
    $('#btnExportar').on('click', function(){
        var table = document.getElementById('tabelaRC');
        if (!table) { alert('Faça uma pesquisa primeiro.'); return; }
        var rows = table.querySelectorAll('tr');
        var csv = '﻿'; // BOM UTF-8 (Excel pt-BR)
        rows.forEach(function(tr){
            var cells = tr.querySelectorAll('th, td');
            var line = [];
            cells.forEach(function(td){ line.push('"' + td.textContent.trim().replace(/"/g, '""') + '"'); });
            csv += line.join(';') + '\n';
        });
        var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        var url = URL.createObjectURL(blob);
        var a = document.createElement('a');
        a.href = url;
        a.download = 'referencia_cruzada_' + (document.querySelector('input[name="data_ini"]').value || '') + '_' + (document.querySelector('input[name="data_fim"]').value || '') + '.csv';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    });
    $('#btnImprimir').on('click', function(){ window.print(); });
    $('#btnSalvarPrev').on('click', function(){
        var inf = prompt('Expectativa de Inflação/Reajuste (%) considerada:');
        if (inf === null) return;
        $('#inflacao').val(inf);
        $('#acao').val('salvar_prev');
        $('form').submit();
    });
    $('#btnGrafico').on('click', function(){
        var $card = $('#cardGrafico');
        if ($card.is(':visible')) { $card.hide(); return; }
        $card.show();
        if (window._graficoRC) { window._graficoRC.destroy(); }
        var ctx = document.getElementById('graficoRC').getContext('2d');
        window._graficoRC = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: <?= json_encode($colMeses) ?>,
                datasets: [
                    { label: 'Receitas', data: <?= json_encode($totaisReceitas ?? []) ?>, backgroundColor: 'rgba(40,167,69,0.7)' },
                    { label: 'Despesas', data: <?= json_encode($totaisDespesas ?? []) ?>.map(function(v){ return Math.abs(v); }), backgroundColor: 'rgba(220,53,69,0.7)' },
                    { label: 'Resultado', data: <?= json_encode(array_map(function($a,$b){return $a+$b;}, $totaisReceitas, $totaisDespesas) ?? []) ?>, backgroundColor: 'rgba(0,123,255,0.7)' }
                ]
            },
            options: { responsive: true, scales: { y: { ticks: { callback: function(v){ return 'R$ ' + v.toLocaleString('pt-BR'); } } } } }
        });
    });
}
// Executa assim que o DOM estiver pronto (funciona mesmo se o script rodar após o load)
if (document.readyState === 'complete' || document.readyState === 'interactive') {
    initRC();
} else {
    window.addEventListener('DOMContentLoaded', initRC);
    window.addEventListener('load', initRC);
}
</script>

<?php include '../includes/footer.php'; ?>
