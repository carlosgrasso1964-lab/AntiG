<?php
session_start();
ini_set('display_errors',1); error_reporting(E_ALL);
require_once '../config/database.php';
require_once '../includes/functions.php';
requireAuth();
$title='Precificação'; include '../includes/header.php';
$db=Database::getInstance()->getConnection();
?>
<div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="mb-0"><i class="bi bi-calculator"></i> Precificação — Formulário de Cálculo</h4>
    <div class="d-flex gap-2">
        <button class="btn btn-outline-primary btn-sm" id="btnExportar"><i class="bi bi-download"></i> Exportar</button>
        <button class="btn btn-outline-secondary btn-sm" onclick="window.print()"><i class="bi bi-printer"></i> Imprimir</button>
    </div>
</div>

<div class="row g-3">

    <!-- ============ BASE DE CÁLCULO (INFORME) ============ -->
    <div class="col-lg-4">
        <div class="card shadow-sm">
            <div class="card-header bg-primary text-white fw-bold">Base de Cálculo (Informe)</div>
            <div class="card-body">
                <div class="mb-2">
                    <label class="form-label fw-semibold">Faturamento (R$):</label>
                    <input type="text" class="form-control text-end" id="txtFaturamento" inputmode="decimal" placeholder="0,00">
                </div>
                <div class="mb-2">
                    <label class="form-label fw-semibold">Qtd. Vendida:</label>
                    <input type="text" class="form-control text-end" id="txtQtdVendida" inputmode="decimal" placeholder="0">
                </div>
                <div class="mb-2">
                    <label class="form-label fw-semibold">Custos (R$):</label>
                    <input type="text" class="form-control text-end" id="txtCustos" inputmode="decimal" placeholder="0,00">
                </div>
                <div class="mb-2">
                    <label class="form-label fw-semibold">Despesas Fixas (R$):</label>
                    <input type="text" class="form-control text-end" id="txtDespFixas" inputmode="decimal" placeholder="0,00">
                </div>

                <div class="border rounded p-2 mb-2 bg-body-tertiary">
                    <label class="form-label fw-semibold mb-1">Despesas Variáveis (%):</label>
                    <input type="text" class="form-control text-end mb-2" id="txtPercDespVariaveis" readonly tabindex="-1" placeholder="soma abaixo">
                    <div class="row g-1">
                        <div class="col-6">
                            <label class="form-label small mb-0">Comissões:</label>
                            <input type="text" class="form-control form-control-sm text-end" id="txtComissoes" inputmode="decimal" placeholder="0,00">
                        </div>
                        <div class="col-6">
                            <label class="form-label small mb-0">Publicidade:</label>
                            <input type="text" class="form-control form-control-sm text-end" id="txtPublicidade" inputmode="decimal" placeholder="0,00">
                        </div>
                        <div class="col-6">
                            <label class="form-label small mb-0">Desp. Admin.:</label>
                            <input type="text" class="form-control form-control-sm text-end" id="txtDespAdministrativas" inputmode="decimal" placeholder="0,00">
                        </div>
                        <div class="col-6">
                            <label class="form-label small mb-0">Impostos/Taxas:</label>
                            <input type="text" class="form-control form-control-sm text-end" id="txtImpostos" inputmode="decimal" placeholder="0,00">
                        </div>
                    </div>
                </div>

                <div class="mb-2">
                    <label class="form-label fw-semibold">Impostos sobre Vendas (%) — Deduções Vendas:</label>
                    <input type="text" class="form-control text-end" id="txtImpVendas" inputmode="decimal" placeholder="0,00">
                </div>
                <div class="mb-2">
                    <label class="form-label fw-semibold">Não Operacional — Empréstimos (R$):</label>
                    <input type="text" class="form-control text-end" id="txtValorEmprest" inputmode="decimal" placeholder="0,00">
                </div>
                <div class="mb-3">
                    <label class="form-label fw-semibold">Lucro Líquido Desejado (%):</label>
                    <input type="text" class="form-control text-end" id="txtLucroLDesejado" inputmode="decimal" placeholder="0,00">
                </div>

                <div class="d-grid gap-2">
                    <button class="btn btn-primary" id="btnCalc"><i class="bi bi-check2-circle"></i> Calcular</button>
                    <button class="btn btn-success" id="btnSimular" style="display:none"><i class="bi bi-graph-up"></i> Simular</button>
                    <div class="d-flex gap-2">
                        <button class="btn btn-outline-secondary flex-fill" id="btnLimpar"><i class="bi bi-eraser"></i> Limpar</button>
                        <button class="btn btn-outline-danger flex-fill" id="btnFechar"><i class="bi bi-x-lg"></i> Fechar</button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ============ MINUTA DE CÁLCULO ============ -->
    <div class="col-lg-4">
        <div class="card shadow-sm">
            <div class="card-header bg-success text-white fw-bold">Minuta de Cálculo</div>
            <div class="card-body table-responsive">
                <table class="table table-sm table-bordered align-middle mb-2">
                    <thead><tr><th class="w-50">Conta</th><th class="text-end">R$</th><th class="text-end">%</th></tr></thead>
                    <tbody>
                        <tr><td>+ Faturamento</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMFatur" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMPercFat" readonly tabindex="-1"></td></tr>
                        <tr><td>- Impostos sobre Faturamento</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMImpSFatur" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMPercImpSFat" readonly tabindex="-1"></td></tr>
                        <tr><td>- Custo Mercadoria Vendida</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMCustos" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMPercCusto" readonly tabindex="-1"></td></tr>
                        <tr class="table-warning"><td>= Lucro Bruto</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0 fw-bold" id="txtMLucroBruto" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMPercLucroBruto" readonly tabindex="-1"></td></tr>
                        <tr><td>- Gastos Variáveis</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMGastosVar" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMPercGastosVar" readonly tabindex="-1"></td></tr>
                        <tr class="table-warning"><td>= Margem de Contribuição</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0 fw-bold" id="txtMMargContr" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMPercMContr" readonly tabindex="-1"></td></tr>
                        <tr><td>- Despesas Fixas</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMDespFix" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMPercDespFixas" readonly tabindex="-1"></td></tr>
                        <tr class="table-warning"><td>= Resultado Operacional</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0 fw-bold" id="txtMResultOper" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMPercResultOper" readonly tabindex="-1"></td></tr>
                        <tr><td>- Empréstimos Bancários</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMEmprest" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMPercEmprest" readonly tabindex="-1"></td></tr>
                        <tr class="table-info"><td>= Resultado Líquido</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0 fw-bold" id="txtMResultLiq" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMPerResLiq" readonly tabindex="-1"></td></tr>
                    </tbody>
                </table>

                <label class="form-label small fw-bold mb-1">Valores Unitários:</label>
                <table class="table table-sm table-bordered align-middle mb-2">
                    <tbody>
                        <tr><td>Faturamento Und.</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMVrFatUn" readonly tabindex="-1"></td></tr>
                        <tr><td>Custo Und.</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMCustoUn" readonly tabindex="-1"></td></tr>
                        <tr><td>Gastos Variáveis Und.</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMGastosUnit" readonly tabindex="-1"></td></tr>
                        <tr><td>Margem Contribuição Und.</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtMMargContrUnit" readonly tabindex="-1"></td></tr>
                    </tbody>
                </table>

                <div class="mb-1">
                    <label class="form-label small fw-bold mb-0">Índice para Formação do preço de Venda:</label>
                    <input type="text" class="form-control form-control-sm text-end" id="txtMultiplicador" readonly tabindex="-1">
                </div>
                <div class="mb-1">
                    <label class="form-label small fw-bold mb-0">Cálculo do Ponto de Equilíbrio em Quantidade:</label>
                    <input type="text" class="form-control form-control-sm text-end" id="txtMQtdEquilibrio" readonly tabindex="-1">
                </div>
                <div>
                    <label class="form-label small fw-bold mb-0">Valor de Equilíbrio (R$):</label>
                    <input type="text" class="form-control form-control-sm text-end" id="txtMValorEquilibrio" readonly tabindex="-1">
                </div>
            </div>
        </div>
    </div>

    <!-- ============ D.R.E. ============ -->
    <div class="col-lg-4">
        <div class="card shadow-sm">
            <div class="card-header bg-dark text-white fw-bold">D.R.E.</div>
            <div class="card-body table-responsive">
                <table class="table table-sm table-bordered align-middle mb-2">
                    <thead><tr><th class="w-50">Conta</th><th class="text-end">R$</th><th class="text-end">%</th></tr></thead>
                    <tbody>
                        <tr><td>Receita Bruta de Vendas</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtDRecBruta" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtDPercRecBru" readonly tabindex="-1"></td></tr>
                        <tr><td>(-) Deduções Vendas (Impostos s/ Vendas)</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtDImpVds" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtDPercImpVd" readonly tabindex="-1"></td></tr>
                        <tr class="table-warning"><td>= Receita Líquida</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0 fw-bold" id="txtDRecLiq" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtDPercRecLiq" readonly tabindex="-1"></td></tr>
                        <tr><td>(-) Custos</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtDCustos" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtDPercCustos" readonly tabindex="-1"></td></tr>
                        <tr class="table-warning"><td>= Lucro Bruto</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0 fw-bold" id="txtDLucroBruto" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtDPercLucroBr" readonly tabindex="-1"></td></tr>
                        <tr><td>(-) Despesas Variáveis</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtDDespVar" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtDPercDespVar" readonly tabindex="-1"></td></tr>
                        <tr class="text-muted small">
                            <td colspan="3">
                                &nbsp;&nbsp;Comissões: <input type="text" class="form-control form-control-sm text-end d-inline-block w-25 border-0" id="txtDComiss" readonly tabindex="-1">
                                &nbsp;Publicidade: <input type="text" class="form-control form-control-sm text-end d-inline-block w-25 border-0" id="txtDVerbasPubl" readonly tabindex="-1"><br>
                                &nbsp;&nbsp;Desp. Administração: <input type="text" class="form-control form-control-sm text-end d-inline-block w-25 border-0" id="txtDAdm" readonly tabindex="-1">
                            </td>
                        </tr>
                        <tr><td>(-) Despesas Fixas</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtDDespFix" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtDPercDespFix" readonly tabindex="-1"></td></tr>
                        <tr class="table-warning"><td>= Lucro Líquido (Resultado Operacional)</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0 fw-bold" id="txtDLucrLiq" readonly tabindex="-1"></td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtDPercLucroLiq" readonly tabindex="-1"></td></tr>
                        <tr><td>(-) Empréstimos</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0" id="txtDEmprest" readonly tabindex="-1"></td><td class="p-0"></td></tr>
                        <tr class="table-info"><td>= Resultado Líquido</td><td class="p-0"><input type="text" class="form-control form-control-sm text-end border-0 fw-bold" id="txtDResLiq" readonly tabindex="-1"></td><td class="p-0"></td></tr>
                    </tbody>
                </table>
                <p class="small text-muted mb-0"><i class="bi bi-info-circle"></i> Despesas Variáveis = Comissões + Publicidade + Desp. Administrativas + Impostos e Taxas. Preencha os campos da Base de Cálculo e clique em <strong>Calcular</strong>.</p>
            </div>
        </div>
    </div>
</div>

<script>
function initPC() {
    if (window.__pcInited) return;
    window.__pcInited = true;

    var calculoRealizado = false;

    // ---- conversão pt-BR ("1.234,56" -> 1234.56) ----
    function parseBR(v) {
        if (v === null || v === undefined) return 0;
        var s = String(v).trim();
        if (s === '') return 0;
        s = s.replace(/\./g, '').replace(',', '.');
        var n = parseFloat(s);
        return isNaN(n) ? 0 : n;
    }

    function fmt(v) {
        return v.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }
    function fmtIdx(v) {
        // igual Java: 4 casas com vírgula, sem separador de milhar
        return v.toFixed(4).replace('.', ',');
    }
    function arred4(v) { return Math.round(v * 10000) / 10000; }

    // ---- helpers ----
    function V(id) { return parseBR(document.getElementById(id).value); }
    function setV(id, v) { document.getElementById(id).value = fmt(v); }
    function setVIdx(id, v) { document.getElementById(id).value = fmtIdx(v); }
    function setRaw(id, v) { document.getElementById(id).value = String(v); }

    // ---- validação: todos os campos obrigatórios (igual Java verificarCamposVazios) ----
    function camposVazios() {
        var ids = ['txtFaturamento','txtImpVendas','txtDespFixas','txtCustos','txtDespAdministrativas',
                   'txtComissoes','txtPublicidade','txtImpostos','txtValorEmprest','txtLucroLDesejado','txtQtdVendida'];
        for (var i = 0; i < ids.length; i++) {
            if (document.getElementById(ids[i]).value.trim() === '') return true;
        }
        return false;
    }

    // ---- soma das despesas variáveis (percentual) ----
    function percDespVariaveis() {
        return V('txtComissoes') + V('txtPublicidade') + V('txtDespAdministrativas') + V('txtImpostos');
    }

    // ---- CALCULAR (espelha btnCalcActionPerformed) ----
    function calcular() {
        if (camposVazios()) {
            alert('Preencha todos os campos obrigatórios!');
            return;
        }

        var faturamento = V('txtFaturamento');
        var impVendas = V('txtImpVendas');
        var despFixas = V('txtDespFixas');
        var custoMercadoria = V('txtCustos');
        var percComissoes = V('txtComissoes');
        var percPublicidade = V('txtPublicidade');
        var percDespAdministrativas = V('txtDespAdministrativas');
        var percImpostos = V('txtImpostos');
        var emprestimos = V('txtValorEmprest');
        var qtdVendida = V('txtQtdVendida');

        var percDespVar = percComissoes + percPublicidade + percDespAdministrativas + percImpostos;
        setV('txtPercDespVariaveis', percDespVar);

        // valores diretos
        setV('txtMFatur', faturamento);
        setV('txtDRecBruta', faturamento);
        setV('txtMCustos', custoMercadoria);
        setV('txtDCustos', custoMercadoria);
        setV('txtMDespFix', despFixas);
        setV('txtDDespFix', despFixas);
        setV('txtDComiss', percComissoes);
        setV('txtDVerbasPubl', percPublicidade);
        setV('txtDAdm', percDespAdministrativas);
        setV('txtMEmprest', emprestimos);
        setV('txtDEmprest', emprestimos);

        // lucro bruto = faturamento - faturamento*impVendas/100 - custoMercadoria
        var impostoSobreFat = faturamento * impVendas / 100;
        var lucroBruto = arred4(faturamento - impostoSobreFat - custoMercadoria);
        setV('txtMLucroBruto', lucroBruto);
        setV('txtDLucroBruto', lucroBruto);

        // impostos sobre faturamento (valor)
        setV('txtMImpSFatur', impostoSobreFat);
        setV('txtDImpVds', impostoSobreFat);

        // receita líquida (DRE)
        var receitaLiquida = faturamento - impostoSobreFat;
        setV('txtDRecLiq', receitaLiquida);

        // percentual receita líquida = imposto / faturamento * 100 (Java usa txtDPercImpVd)
        if (faturamento !== 0) {
            setV('txtDPercRecLiq', arred4(impostoSobreFat / faturamento * 100));
        } else {
            setV('txtDPercRecLiq', 0);
        }

        // gastos variáveis (valor) = faturamento * percDespVar/100
        var gastosVariaveis = faturamento * percDespVar / 100;
        // margem de contribuição = lucroBruto - gastosVariaveis
        var margemContribuicao = arred4(lucroBruto - gastosVariaveis);
        setV('txtMGastosVar', gastosVariaveis);
        setV('txtDDespVar', gastosVariaveis);
        setV('txtMMargContr', margemContribuicao);

        // resultado operacional = margem - despFixas
        var resultadoOperacional = arred4(margemContribuicao - despFixas);
        setV('txtMResultOper', resultadoOperacional);
        setV('txtDLucrLiq', resultadoOperacional);

        // percentual gastos variáveis = total (soma dos %) e percentual resultado operacional
        setV('txtMPercGastosVar', percDespVar);
        setV('txtDPercDespVar', percDespVar);
        if (faturamento !== 0) {
            setV('txtMPercResultOper', arred4(resultadoOperacional / faturamento * 100));
            setV('txtDPercLucroLiq', arred4(resultadoOperacional / faturamento * 100));
        } else {
            setV('txtMPercResultOper', 0);
            setV('txtDPercLucroLiq', 0);
        }

        // percentual empréstimos = emprestimos / faturamento * 100
        if (faturamento !== 0) {
            setV('txtMPercEmprest', arred4(emprestimos / faturamento * 100));
        } else {
            setV('txtMPercEmprest', 0);
        }

        // resultado líquido = resultadoOperacional - emprestimos
        var resultadoLiquido = arred4(resultadoOperacional - emprestimos);
        setV('txtMResultLiq', resultadoLiquido);
        setV('txtDResLiq', resultadoLiquido);

        // percentual resultado líquido
        if (faturamento !== 0) {
            setV('txtMPerResLiq', arred4(resultadoLiquido / faturamento * 100));
        } else {
            setV('txtMPerResLiq', 0);
        }

        // ---- percentuais (minuta) ----
        setV('txtMPercFat', 100);
        setV('txtDPercRecBru', 100);
        setV('txtMPercImpSFat', impVendas);
        setV('txtDPercImpVd', impVendas);
        if (faturamento !== 0) {
            setV('txtMPercCusto', arred4(custoMercadoria / faturamento * 100));
            setV('txtDPercCustos', arred4(custoMercadoria / faturamento * 100));
            setV('txtMPercLucroBruto', arred4(lucroBruto / faturamento * 100));
            setV('txtDPercLucroBr', arred4(lucroBruto / faturamento * 100));
            setV('txtMPercDespFixas', arred4(despFixas / faturamento * 100));
            setV('txtDPercDespFix', arred4(despFixas / faturamento * 100));
            setV('txtMPercMContr', arred4(margemContribuicao / faturamento * 100));
        } else {
            setV('txtMPercCusto', 0); setV('txtDPercCustos', 0);
            setV('txtMPercLucroBruto', 0); setV('txtDPercLucroBr', 0);
            setV('txtMPercDespFixas', 0); setV('txtDPercDespFix', 0);
            setV('txtMPercMContr', 0);
        }

        // ---- valores unitários ----
        if (qtdVendida !== 0) {
            setV('txtMVrFatUn', faturamento / qtdVendida);
            setV('txtMCustoUn', custoMercadoria / qtdVendida);
            setV('txtMGastosUnit', gastosVariaveis / qtdVendida);
            setV('txtMMargContrUnit', margemContribuicao / qtdVendida);
        } else {
            setV('txtMVrFatUn', 0); setV('txtMCustoUn', 0);
            setV('txtMGastosUnit', 0); setV('txtMMargContrUnit', 0);
        }

        // ---- índices ----
        calcularIndices();

        calculoRealizado = true;
        document.getElementById('btnSimular').style.display = 'block';
    }

    // ---- índices (espelha calcularIndices) ----
    function calcularIndices() {
        var percGastosVar = V('txtMPercGastosVar') / 100;
        var perResLiq = V('txtMPerResLiq') / 100;
        var despFix = V('txtMDespFix');
        var emprest = V('txtMEmprest');
        var margContrUnit = V('txtMMargContrUnit');
        var vrFatUn = V('txtMVrFatUn');

        // Multiplicador = 1 / (1 - (percGastosVar + perResLiq))
        var denom = 1 - (percGastosVar + perResLiq);
        var multiplicador = denom !== 0 ? 1 / denom : 0;
        setVIdx('txtMultiplicador', multiplicador);

        // Qtd de Equilíbrio = (despFix + emprest) / margContrUnit
        var qtdEquilibrio = margContrUnit !== 0 ? (despFix + emprest) / margContrUnit : 0;
        setV('txtMQtdEquilibrio', qtdEquilibrio);

        // Valor de Equilíbrio = qtdEquilibrio * vrFatUn
        setV('txtMValorEquilibrio', qtdEquilibrio * vrFatUn);
    }

    // ---- SIMULAR (espelha jButtonSimularActionPerformed) ----
    function simular() {
        if (!calculoRealizado) {
            alert('Você precisa calcular antes de simular!');
            return;
        }
        var custos = V('txtMCustos');
        var despesasFixas = V('txtMDespFix');
        var percDespesasVariaveis = V('txtMPercGastosVar') / 100;
        var percLucroLiquido = V('txtLucroLDesejado') / 100;
        var percImposto = V('txtMPercImpSFat') / 100;

        var somaPercentuais = percDespesasVariaveis + percLucroLiquido + percImposto;
        if (somaPercentuais >= 1) {
            alert('A soma dos percentuais não pode ser maior ou igual a 100%!');
            return;
        }
        var faturamentoNecessario = (despesasFixas + custos) / (1 - somaPercentuais);
        alert('Faturamento Necessário: R$ ' + fmt(faturamentoNecessario));
    }

    // ---- LIMPAR (espelha btnLimparActionPerformed) ----
    function limpar() {
        var campos = document.querySelectorAll('.card input[type="text"]');
        for (var i = 0; i < campos.length; i++) {
            if (!campos[i].readOnly || campos[i].readOnly === false) campos[i].value = '';
            else campos[i].value = '';
        }
        calculoRealizado = false;
        document.getElementById('btnSimular').style.display = 'none';
        document.getElementById('txtFaturamento').focus();
    }

    // ---- EXPORTAR CSV (Minuta + Unitários + Índices + DRE) ----
    function gerarCSV(linhas, nomeArquivo) {
        if (!linhas || linhas.length < 2) { alert('Sem dados para exportar.'); return; }
        var csv = '\uFEFF' + linhas.join('\n');
        var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        var link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = nomeArquivo + '_' + new Date().toISOString().slice(0, 10) + '.csv';
        link.click();
    }

    // Escapa texto que começa com = + - @ (Excel os interpreta como fórmula e trunca).
    // Prefixa com ' — o Excel mostra o conteúdo sem o apóstrofo, como texto.
    function escTxt(s) {
        s = String(s);
        return /^[=+\-@]/.test(s) ? "'" + s : s;
    }

    // Monta linha CSV: escapa só os NOMES (coluna texto); valores numéricos ficam como estão.
    function linhaMinuta(nome, idValor, idPct) {
        return escTxt(nome) + ';' + document.getElementById(idValor).value + ';' + document.getElementById(idPct).value;
    }

    function exportarPrecificacao() {
        var rows = [];
        rows.push('PRECIFICAÇÃO — MINUTA DE CÁLCULO');
        rows.push('Conta;R$;%');
        rows.push(linhaMinuta('+ Faturamento', 'txtMFatur', 'txtMPercFat'));
        rows.push(linhaMinuta('- Impostos sobre Faturamento', 'txtMImpSFatur', 'txtMPercImpSFat'));
        rows.push(linhaMinuta('- Custo Mercadoria Vendida', 'txtMCustos', 'txtMPercCusto'));
        rows.push(linhaMinuta('= Lucro Bruto', 'txtMLucroBruto', 'txtMPercLucroBruto'));
        rows.push(linhaMinuta('- Gastos Variáveis', 'txtMGastosVar', 'txtMPercGastosVar'));
        rows.push(linhaMinuta('= Margem de Contribuição', 'txtMMargContr', 'txtMPercMContr'));
        rows.push(linhaMinuta('- Despesas Fixas', 'txtMDespFix', 'txtMPercDespFixas'));
        rows.push(linhaMinuta('= Resultado Operacional', 'txtMResultOper', 'txtMPercResultOper'));
        rows.push(linhaMinuta('- Empréstimos Bancários', 'txtMEmprest', 'txtMPercEmprest'));
        rows.push(linhaMinuta('= Resultado Líquido', 'txtMResultLiq', 'txtMPerResLiq'));
        rows.push('');
        rows.push('VALORES UNITÁRIOS');
        rows.push(escTxt('Faturamento Und.') + ';' + document.getElementById('txtMVrFatUn').value);
        rows.push(escTxt('Custo Und.') + ';' + document.getElementById('txtMCustoUn').value);
        rows.push(escTxt('Gastos Variáveis Und.') + ';' + document.getElementById('txtMGastosUnit').value);
        rows.push(escTxt('Margem Contribuição Und.') + ';' + document.getElementById('txtMMargContrUnit').value);
        rows.push('');
        rows.push('ÍNDICES');
        rows.push(escTxt('Multiplicador') + ';' + document.getElementById('txtMultiplicador').value);
        rows.push(escTxt('Ponto de Equilíbrio (Qtd)') + ';' + document.getElementById('txtMQtdEquilibrio').value);
        rows.push(escTxt('Valor de Equilíbrio (R$)') + ';' + document.getElementById('txtMValorEquilibrio').value);
        rows.push('');
        rows.push('D.R.E.');
        rows.push('Conta;R$;%');
        rows.push(linhaMinuta('Receita Bruta de Vendas', 'txtDRecBruta', 'txtDPercRecBru'));
        rows.push(linhaMinuta('(-) Deduções Vendas (Impostos s/ Vendas)', 'txtDImpVds', 'txtDPercImpVd'));
        rows.push(linhaMinuta('= Receita Líquida', 'txtDRecLiq', 'txtDPercRecLiq'));
        rows.push(linhaMinuta('(-) Custos', 'txtDCustos', 'txtDPercCustos'));
        rows.push(linhaMinuta('= Lucro Bruto', 'txtDLucroBruto', 'txtDPercLucroBr'));
        rows.push(linhaMinuta('(-) Despesas Variáveis', 'txtDDespVar', 'txtDPercDespVar'));
        rows.push(linhaMinuta('(-) Despesas Fixas', 'txtDDespFix', 'txtDPercDespFix'));
        rows.push(linhaMinuta('= Lucro Líquido (Resultado Operacional)', 'txtDLucrLiq', 'txtDPercLucroLiq'));
        rows.push(escTxt('(-) Empréstimos') + ';' + document.getElementById('txtDEmprest').value);
        rows.push(escTxt('= Resultado Líquido') + ';' + document.getElementById('txtDResLiq').value);
        gerarCSV(rows, 'precificacao');
    }

    // ---- listeners ----
    document.getElementById('btnCalc').addEventListener('click', calcular);
    document.getElementById('btnSimular').addEventListener('click', simular);
    document.getElementById('btnLimpar').addEventListener('click', limpar);
    document.getElementById('btnExportar').addEventListener('click', exportarPrecificacao);
    document.getElementById('btnFechar').addEventListener('click', function () {
        window.location.href = 'index.php';
    });

    // ao digitar nas despesas variáveis, atualiza a soma em tempo real
    ['txtComissoes','txtPublicidade','txtDespAdministrativas','txtImpostos'].forEach(function (id) {
        document.getElementById(id).addEventListener('input', function () {
            setV('txtPercDespVariaveis', percDespVariaveis());
        });
    });
}

if (document.readyState === 'complete' || document.readyState === 'interactive') initPC();
else { window.addEventListener('DOMContentLoaded', initPC); window.addEventListener('load', initPC); }
</script>
<?php include '../includes/footer.php';?>
