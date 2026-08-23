<?php
session_start();
ini_set('display_errors', 1);
error_reporting(E_ALL);
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();

$title = 'Auditoria';
include '../includes/header.php';

$db = Database::getInstance()->getConnection();

// === Parâmetros ===
$dataIni  = $_POST['dataIni'] ?? '';
$dataFim  = $_POST['dataFim'] ?? '';
$regime   = $_POST['regime'] ?? 'V';    // V=Vcto / A=Apresentação
$status   = $_POST['status'] ?? 'T';    // T=Todos / C=Confirmados / P=Previstos
$analisou = false;
$linhas   = [];

function fmtBR($n) { return number_format($n, 2, ',', '.'); }

// === ANALISAR ===
if (isset($_POST['btnAnalisar']) && $dataIni && $dataFim) {
    $analisou = true;
    $partsIni = explode('/', $dataIni);
    $partsFim = explode('/', $dataFim);
    if (count($partsIni) !== 3 || count($partsFim) !== 3) {
        echo '<div class="alert alert-danger">Datas inválidas.</div>';
        $analisou = false;
    } else {
        $dataIniSQL = $partsIni[2] . '-' . $partsIni[1] . '-' . $partsIni[0];
        $dataFimSQL = $partsFim[2] . '-' . $partsFim[1] . '-' . $partsFim[0];
        $anoRef = (int)$partsIni[2];

        $campoData = ($regime === 'A') ? 'dtApr' : 'dtVcto';

        // Filtro de status na viewmovrd
        $filtroPrev = '';
        if ($status === 'P') $filtroPrev = "AND Prev = 'V'";
        elseif ($status === 'C') $filtroPrev = "AND Prev = 'C'";

        $sql = "SELECT
                    p.conta AS 'conta',
                    ROUND(SUM(p.valor_planejado), 2) AS 'previsto',
                    ROUND(COALESCE(m.TotalRealizado, 0), 2) AS 'realizado',
                    ROUND((COALESCE(m.TotalRealizado, 0) - SUM(p.valor_planejado)), 2) AS 'desvio_abs',
                    CASE WHEN SUM(p.valor_planejado) = 0 THEN 0
                    ELSE ROUND(((COALESCE(m.TotalRealizado, 0) / SUM(p.valor_planejado)) - 1) * 100, 2) END AS 'var_pct'
                FROM tb_plano_diretor p
                LEFT JOIN (
                    SELECT nome_C, SUM(Valor) as TotalRealizado
                    FROM viewmovrd
                    WHERE $campoData BETWEEN ? AND ?
                    $filtroPrev
                    GROUP BY nome_C
                ) m ON p.conta = m.nome_C COLLATE utf8mb4_general_ci
                WHERE p.ano_referencia = ?
                  AND p.mes BETWEEN MONTH(?) AND MONTH(?)
                  AND p.conta NOT LIKE 'Receitas%'
                  AND p.conta NOT LIKE 'Despesas%'
                  AND p.conta NOT LIKE 'Saldo%'
                  AND p.conta NOT LIKE 'Resultado%'
                GROUP BY p.conta
                ORDER BY ABS(COALESCE(m.TotalRealizado, 0) - SUM(p.valor_planejado)) DESC";

        try {
            $stmt = $db->prepare($sql);
            $stmt->execute([$dataIniSQL, $dataFimSQL, $anoRef, $dataIniSQL, $dataFimSQL]);
            $linhas = $stmt->fetchAll(PDO::FETCH_ASSOC);
        } catch (Exception $e) {
            echo '<div class="alert alert-danger">Erro: ' . htmlspecialchars($e->getMessage()) . '</div>';
            $analisou = false;
        }
    }
}

// === Inflação premissa (para o botão Lia) ===
$inflacaoAnual = 5.0;
try {
    $stmt = $db->query("SELECT inflacao_premissa FROM tb_plano_diretor LIMIT 1");
    $r = $stmt->fetch(PDO::FETCH_ASSOC);
    if ($r) $inflacaoAnual = (float)$r['inflacao_premissa'];
} catch (Exception $e) {}
?>
<div class="container-fluid mt-3">
    <h4>Consulta - Auditoria</h4>

    <!-- Filtros -->
    <form method="POST" class="row g-2 mb-3 p-2 bg-light border rounded align-items-end">
        <div class="col-auto">
            <label class="form-label small">Data Inicial</label>
            <input type="text" name="dataIni" class="form-control form-control-sm" placeholder="dd/mm/aaaa"
                   value="<?= htmlspecialchars($dataIni) ?>" style="width:120px" required>
        </div>
        <div class="col-auto">
            <label class="form-label small">Data Final</label>
            <input type="text" name="dataFim" class="form-control form-control-sm" placeholder="dd/mm/aaaa"
                   value="<?= htmlspecialchars($dataFim) ?>" style="width:120px" required>
        </div>
        <div class="col-auto">
            <label class="form-label small">Regime</label>
            <div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="regime" value="V"
                           id="regVcto" <?= $regime === 'V' ? 'checked' : '' ?>>
                    <label class="form-check-label small" for="regVcto">Vencimento</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="regime" value="A"
                           id="regApr" <?= $regime === 'A' ? 'checked' : '' ?>>
                    <label class="form-check-label small" for="regApr">Apresentação</label>
                </div>
            </div>
        </div>
        <div class="col-auto">
            <label class="form-label small">Status</label>
            <div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="status" value="T"
                           id="stTodos" <?= $status === 'T' ? 'checked' : '' ?>>
                    <label class="form-check-label small" for="stTodos">Todos</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="status" value="C"
                           id="stConf" <?= $status === 'C' ? 'checked' : '' ?>>
                    <label class="form-check-label small" for="stConf">Confirmados</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="status" value="P"
                           id="stPrev" <?= $status === 'P' ? 'checked' : '' ?>>
                    <label class="form-check-label small" for="stPrev">Previstos</label>
                </div>
            </div>
        </div>
        <div class="col-auto">
            <button type="submit" name="btnAnalisar" class="btn btn-primary btn-sm">Analisar</button>
            <button type="button" class="btn btn-secondary btn-sm" onclick="limparAuditoria()">Limpar</button>
            <button type="button" class="btn btn-info btn-sm" onclick="window.open('planejamento.php?dataIni=<?= urlencode($dataIni) ?>&dataFim=<?= urlencode($dataFim) ?>','_blank')">Salvar/Alterar</button>
            <button type="button" class="btn btn-danger btn-sm" onclick="if(confirm('Excluir planejamento do período?'))window.open('planejamento.php?dataIni=<?= urlencode($dataIni) ?>&dataFim=<?= urlencode($dataFim) ?>','_blank')">Excluir</button>
        </div>
    </form>

    <!-- Botões de ação -->
    <?php if ($analisou): ?>
    <div class="mb-2">
        <button class="btn btn-outline-success btn-sm" onclick="exportarCSV()">Exportar CSV</button>
        <button class="btn btn-outline-secondary btn-sm" onclick="window.print()">Imprimir</button>
        <button class="btn btn-outline-warning btn-sm" id="btnLia" onclick="analisarComLia()">Analisar com Lia</button>
    </div>
    <?php endif; ?>

    <!-- Tabela de resultados -->
    <div class="table-responsive">
        <table class="table table-sm table-bordered table-striped" id="tabelaAuditoria">
            <thead class="table-light">
                <tr>
                    <th>Grupo de Conta</th>
                    <th class="text-end">Previsto</th>
                    <th class="text-end">Realizado</th>
                    <th class="text-end">Desvio Abs.</th>
                    <th class="text-end">Var %</th>
                </tr>
            </thead>
            <tbody>
                <?php if ($linhas): ?>
                <?php foreach ($linhas as $r): ?>
                <tr>
                    <td><?= htmlspecialchars($r['conta']) ?></td>
                    <td class="text-end"><?= fmtBR($r['previsto']) ?></td>
                    <td class="text-end"><?= fmtBR($r['realizado']) ?></td>
                    <td class="text-end <?= $r['desvio_abs'] < 0 ? 'text-danger' : 'text-success' ?>"><?= fmtBR($r['desvio_abs']) ?></td>
                    <td class="text-end <?= $r['var_pct'] < 0 ? 'text-danger' : 'text-success' ?>"><?= fmtBR($r['var_pct']) ?>%</td>
                </tr>
                <?php endforeach; ?>
                <?php elseif ($analisou): ?>
                <tr><td colspan="5" class="text-center text-muted">Nenhum resultado para o período.</td></tr>
                <?php endif; ?>
            </tbody>
        </table>
    </div>
</div>

<script>
function limparAuditoria() {
    document.querySelector('[name="dataIni"]').value = '';
    document.querySelector('[name="dataFim"]').value = '';
    document.querySelector('#tabelaAuditoria tbody').innerHTML = '<tr><td colspan="5" class="text-center text-muted">Clique em Analisar para carregar dados.</td></tr>';
}
function exportarCSV() {
    var tbl = document.getElementById('tabelaAuditoria');
    if (!tbl || tbl.rows.length < 2) { alert('Nenhum dado para exportar.'); return; }
    var csv = '\uFEFF';
    csv += 'Grupo de Conta;Previsto;Realizado;Desvio Abs.;Var %\n';
    for (var i = 1; i < tbl.rows.length; i++) {
        var cells = tbl.rows[i].cells;
        if (cells.length < 5) continue;
        csv += cells[0].textContent.trim() + ';';
        csv += cells[1].textContent.trim() + ';';
        csv += cells[2].textContent.trim() + ';';
        csv += cells[3].textContent.trim() + ';';
        csv += cells[4].textContent.trim() + '\n';
    }
    var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    var link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'auditoria_' + new Date().toISOString().slice(0,10) + '.csv';
    link.click();
}
function analisarComLia() {
    var btn = document.getElementById('btnLia');
    btn.disabled = true; btn.textContent = 'Lia está pensando... (pode demorar)';

    var dataIni = document.querySelector('[name="dataIni"]').value;
    var dataFim = document.querySelector('[name="dataFim"]').value;
    if (!dataIni || !dataFim) { alert('Pesquise um período primeiro.'); btn.disabled = false; btn.textContent = 'Analisar com Lia'; return; }
    var inflacaoAnual = <?= json_encode($inflacaoAnual) ?>;
    var partesIni = dataIni.split('/'), partesFim = dataFim.split('/');
    var mesIni = parseInt(partesIni[1]), mesFim = parseInt(partesFim[1]);
    var qtdMeses = (mesFim - mesIni) + 1;
    if (qtdMeses <= 0) qtdMeses = 1;
    var inflacaoPeriodo = (inflacaoAnual / 12) * qtdMeses;

    var dados = '';
    var tbl = document.getElementById('tabelaAuditoria');
    var temDados = false;
    for (var i = 1; i < tbl.rows.length; i++) {
        var cells = tbl.rows[i].cells;
        if (cells.length < 5) continue;
        temDados = true;
        dados += '- ' + cells[0].textContent.trim()
            + ': Prev ' + cells[1].textContent.trim()
            + ' | Real ' + cells[2].textContent.trim()
            + ' | Desvio: ' + cells[4].textContent.trim() + '\n';
    }
    if (!temDados) { alert('Nenhum dado para analisar. Clique em Analisar primeiro.'); btn.disabled = false; btn.textContent = 'Analisar com Lia'; return; }

    var prompt = 'Lia, atue como minha analista financeira pessoal. '
        + 'Analise meus desvios financeiros no período de ' + dataIni + ' a ' + dataFim + '. '
        + 'Atenção: valores negativos são despesas. Se o desvio for negativo e o valor realizado for maior que o previsto, isso é um aumento de gasto. '
        + 'Minha premissa de inflação ANUAL é de ' + inflacaoAnual.toFixed(2) + '%, '
        + 'o que equivale a uma inflação de ' + inflacaoPeriodo.toFixed(2)
        + '% para este período de ' + qtdMeses + ' mês(es).\n\n'
        + 'Lia, quando o desvio da Aposentadoria for negativo, chame isso de \'Achatamento\' e analise o impacto disso no meu consumo de itens básicos como Energia e Gás.\n\n'
        + 'Dados da Auditoria:\n' + dados
        + '\nCom base na inflação de ' + inflacaoPeriodo.toFixed(2)
        + '%, identifique onde meu poder de compra está sendo mais \'achatado\'.';

    // Chama Ollama DIRETAMENTE do navegador (sem PHP, sem timeout de servidor)
    fetch('http://127.0.0.1:11434/api/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            model: 'gemma2:2b',
            prompt: prompt,
            stream: false
        })
    })
    .then(function(r) {
        if (!r.ok) throw new Error('HTTP ' + r.status);
        return r.json();
    })
    .then(function(data) {
        btn.disabled = false; btn.textContent = 'Analisar com Lia';
        var resposta = data.response || '';
        if (!resposta) { alert('Resposta vazia da Lia.'); return; }
        // Salva parecer via AJAX (opcional, apenas para histórico)
        fetch('ajax/salvar_parecer.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ resposta: resposta })
        }).catch(function(){});
        // Exibe o parecer
        var div = document.createElement('div');
        div.className = 'modal fade';
        div.id = 'modalLia';
        div.innerHTML = '<div class="modal-dialog modal-lg"><div class="modal-content"><div class="modal-header"><h5 class="modal-title">Parecer da Lia</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div><div class="modal-body"><pre style="white-space:pre-wrap;font-family:sans-serif;font-size:14px">' + resposta.replace(/</g,'&lt;') + '</pre></div></div></div>';
        document.body.appendChild(div);
        var modal = new bootstrap.Modal(div);
        modal.show();
        div.addEventListener('hidden.bs.modal', function() { div.remove(); });
    })
    .catch(function(err) {
        btn.disabled = false; btn.textContent = 'Analisar com Lia';
        alert('Lia está exausta (Erro): ' + err.message + '\n\nVerifique se o Ollama está rodando (ollama serve) e tente novamente.');
    });
}
</script>

<?php include '../includes/footer.php'; ?>
