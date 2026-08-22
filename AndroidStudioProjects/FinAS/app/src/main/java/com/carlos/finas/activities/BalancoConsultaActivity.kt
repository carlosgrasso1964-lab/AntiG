package com.carlos.finas.activities

import android.graphics.Color
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.R
import com.carlos.finas.databinding.ActivityBalancoConsultaBinding
import com.carlos.finas.databinding.ItemBalancoBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.kernel.geom.PageSize
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.iterator

class BalancoConsultaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBalancoConsultaBinding
    private val decimalFormat = DecimalFormat("#,##0.00")
    private val percentFormat = DecimalFormat("0.0000%")
    private val indexFormat = DecimalFormat("0.0000")
    private val daysFormat = DecimalFormat("00")
    private val dateFormatInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateFormatOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var contaTotais: Map<String, Double> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBalancoConsultaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewBalanco.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewBalanco.adapter = BalancoAdapter(emptyList())

        // Adicionar DateMaskWatcher para formatação automática de datas
        binding.editDataIni.addTextChangedListener(DateMaskWatcher())
        binding.editDataFim.addTextChangedListener(DateMaskWatcher())

        binding.btnPesquisar.setOnClickListener { pesquisar() }
        binding.btnLimpar.setOnClickListener { limpar() }
        binding.btnImprimir.setOnClickListener { imprimir() }
        binding.btnExportar.setOnClickListener { exportar() }
        binding.btnGrafico.setOnClickListener { gerarGrafico() }
    }

    // Classe DateMaskWatcher para formatar a data
    private inner class DateMaskWatcher : TextWatcher {
        private var isUpdating = false
        private val mask = "##/##/####"

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (isUpdating || s.isNullOrEmpty()) return
            isUpdating = true
            val str = s.toString().replace("[^0-9]".toRegex(), "")
            val formatted = StringBuilder()
            var i = 0
            for (m in mask) {
                if (m != '#' && i < str.length) {
                    formatted.append(m)
                    continue
                }
                if (i < str.length) {
                    formatted.append(str[i])
                    i++
                }
            }
            s.replace(0, s.length, formatted.toString())
            isUpdating = false
        }
    }

    private fun limpar() {
        binding.editDataIni.text.clear()
        binding.editDataFim.text.clear()
        binding.recyclerViewBalanco.adapter = BalancoAdapter(emptyList())
        contaTotais = emptyMap()
        Toast.makeText(this, "Campos limpos!", Toast.LENGTH_SHORT).show()
    }

    private fun pesquisar() {
        val dataIni = binding.editDataIni.text.toString().trim()
        val dataFim = binding.editDataFim.text.toString().trim()

        if (dataIni.length < 10) {
            Toast.makeText(this, "Atenção! Favor preencher o campo de data inicial corretamente.", Toast.LENGTH_SHORT).show()
            return
        }
        if (dataFim.length < 10) {
            Toast.makeText(this, "Atenção! Favor preencher o campo de data final corretamente.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dataIniParsed = dateFormatInput.parse(dataIni)
            val dataFimParsed = dateFormatInput.parse(dataFim)

            if (dataIniParsed == null || dataFimParsed == null) {
                Toast.makeText(this, "Erro! Formato de data inválido.", Toast.LENGTH_SHORT).show()
                return
            }

            val dataIniFormatada = dateFormatOutput.format(dataIniParsed)
            val dataFimFormatada = dateFormatOutput.format(dataFimParsed)

            val dbHelper = DatabaseHelper(this)
            val db = dbHelper.readableDatabase

            // Consulta principal com nomebcoR adaptada do modelo Desktop
            val sql = """
                SELECT 
                    nome_P AS Princ, 
                    nome_S AS Sub, 
                    CASE 
                        WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS À RECEBER%' THEN 'Contas à Receber' 
                        WHEN UPPER(TRIM(nomebcoR)) = 'CONTAS À RECEBER' AND UPPER(TRIM(nome_C)) = 'BANCOS' THEN 'Contas à Receber' 
                        ELSE nome_C 
                    END AS Conta, 
                    CASE 
                        WHEN UPPER(TRIM(nomebcoR)) = 'CONTAS À RECEBER' THEN 'CONTAS À RECEBER' 
                        WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS À RECEBER%' THEN 'CONTAS À RECEBER' 
                        ELSE nomebcoR 
                    END AS Nome, 
                    CASE 
                        WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS À RECEBER%' THEN '1.002.001' 
                        WHEN UPPER(TRIM(nomebcoR)) = 'CONTAS À RECEBER' AND UPPER(TRIM(nome_C)) = 'BANCOS' THEN '1.002.001' 
                        ELSE vrecurso 
                    END AS vrecurso, 
                    SUM(Valor) AS Total 
                FROM view_mov 
                WHERE Prev <> 'F' 
                  AND dtlancto <= ? 
                  AND nome_C <> 'Contas à Pagar' 
                  AND NOT (nomebcoR = 'CONTAS À PAGAR' AND nome_C NOT IN ('Contas à Pagar')) 
                GROUP BY 
                    nome_P, nome_S, 
                    CASE WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS À RECEBER%' THEN 'Contas à Receber' 
                         WHEN UPPER(TRIM(nomebcoR)) = 'CONTAS À RECEBER' AND UPPER(TRIM(nome_C)) = 'BANCOS' THEN 'Contas à Receber' 
                         ELSE nome_C END, 
                    CASE WHEN UPPER(TRIM(nomebcoR)) = 'CONTAS À RECEBER' THEN 'CONTAS À RECEBER' 
                         WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS À RECEBER%' THEN 'CONTAS À RECEBER' 
                         ELSE nomebcoR END, 
                    CASE WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS À RECEBER%' THEN '1.002.001' 
                         WHEN UPPER(TRIM(nomebcoR)) = 'CONTAS À RECEBER' AND UPPER(TRIM(nome_C)) = 'BANCOS' THEN '1.002.001' 
                         ELSE vrecurso END, 
                    recurso 
                ORDER BY 
                    CAST(SUBSTR(CASE WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS À RECEBER%' THEN '1.002.001' 
                                     WHEN UPPER(TRIM(nomebcoR)) = 'CONTAS À RECEBER' AND UPPER(TRIM(nome_C)) = 'BANCOS' THEN '1.002.001' 
                                     ELSE vrecurso END, 1, 1) AS INTEGER), 
                    CASE WHEN UPPER(TRIM(nome_C)) LIKE '%CONTAS À RECEBER%' THEN '1.002.001' 
                         WHEN UPPER(TRIM(nomebcoR)) = 'CONTAS À RECEBER' AND UPPER(TRIM(nome_C)) = 'BANCOS' THEN '1.002.001' 
                         ELSE vrecurso END
            """
            val cursor = db.rawQuery(sql, arrayOf(dataFimFormatada))

            val items = mutableListOf<BalancoItem>()
            var ativo = 0.0
            var passivo = 0.0
            var subPassivo = 0.0
            var patrLiq = 0.0
            var resultado = 0.0
            var acumulado = 0.0
            var prin = "a"
            var sub = " "
            var cta = " "
            var titulo = "s"
            var totalPrincipal = 0.0
            var totalGrupo = 0.0
            var totalConta = 0.0
            val tempContaTotais = HashMap<String, Double>()

            if (titulo == "s") {
                items.add(BalancoItem("", "", "", "", "", ""))
                items.add(BalancoItem("", " -- BALANÇO PATRIMONIAL --", "Realizado em - $dataFim", "", "0", "0"))
                titulo = "n"
            }

            while (cursor.moveToNext()) {
                val currentPrincipal = cursor.getString(cursor.getColumnIndexOrThrow("Princ"))
                val currentSub = cursor.getString(cursor.getColumnIndexOrThrow("Sub"))
                val currentConta = cursor.getString(cursor.getColumnIndexOrThrow("Conta"))
                val nome = cursor.getString(cursor.getColumnIndexOrThrow("Nome")) ?: ""
                val valor = cursor.getDouble(cursor.getColumnIndexOrThrow("Total"))

                // Ajustar sinal para contas de passivo
                val valorAjustado = if (currentConta in listOf(
                        "Contas_a_Pagar", "Cartões_de_Crédito", "Fornecedores",
                        "EmprObtidos_Curto_Prazo", "Emprestde_Longo_Prazo_(+_de_1_ano)")) {
                    -valor // Passivos são negativos
                } else {
                    valor // Ativos são positivos
                }

                if (!cta.equals(currentConta)) {
                    if (cta != " ") {
                        items.add(BalancoItem("", "", "", "TOTAL DA CONTA $cta", decimalFormat.format(totalConta), ""))
                        val nomeVariavel = cta.replace(" ", "_")
                            .replace("à", "a").replace("á", "a").replace("ã", "a")
                            .replace("ç", "c").replace(".", "")
                        tempContaTotais[nomeVariavel] = Math.round(totalConta * 100.0) / 100.0
                        totalConta = 0.0
                        items.add(BalancoItem("", "", "", "", "", ""))
                    }
                }

                if (!sub.equals(currentSub)) {
                    if (sub != " ") {
                        items.add(BalancoItem("", "", "", "TOTAL DO GRUPO $sub", decimalFormat.format(totalGrupo), ""))
                        items.add(BalancoItem("", "", "", "", "", ""))
                        totalGrupo = 0.0
                    }
                }

                if (!prin.equals(currentPrincipal)) {
                    if (prin != "a") {
                        items.add(BalancoItem("", "", "", "TOTAL DO $prin", "", decimalFormat.format(totalPrincipal)))
                        items.add(BalancoItem("", "", "", "", "", ""))
                        subPassivo = totalPrincipal
                    }
                    totalPrincipal = 0.0
                }

                totalPrincipal += valorAjustado
                totalGrupo += valorAjustado
                totalConta += valorAjustado
                acumulado += valorAjustado

                items.add(BalancoItem(
                    if (prin == currentPrincipal) "" else currentPrincipal,
                    if (sub == currentSub) "" else currentSub,
                    if (cta == currentConta) "" else currentConta,
                    nome,
                    decimalFormat.format(valorAjustado),
                    decimalFormat.format(acumulado)
                ))

                prin = currentPrincipal
                sub = currentSub
                cta = currentConta
            }

            if (cta != " " && cta != "Contas à Pagar") {
                items.add(BalancoItem("", "", "", "TOTAL DA CONTA $cta", decimalFormat.format(totalConta), ""))
                val nomeVariavel = cta.replace(" ", "_")
                    .replace("à", "a").replace("á", "a").replace("ã", "a")
                    .replace("ç", "c").replace(".", "")
                tempContaTotais[nomeVariavel] = Math.round(totalConta * 100.0) / 100.0
                totalConta = 0.0
            }

            val sqlCP = """
                SELECT SUM(Valor) AS TotalCP 
                FROM view_mov 
                WHERE recurso = '0079' 
                  AND vrecurso = '2.001.002' 
                  AND IFNULL(Prev, '') <> 'F' 
                  AND dtlancto <= ? 
                  AND (dtApr = '' OR dtApr IS NULL OR dtApr >= ?)
            """
            val cursorCP = db.rawQuery(sqlCP, arrayOf(dataFimFormatada, dataIniFormatada))
            var saldoContasPagarCorreto = 0.0
            if (cursorCP.moveToFirst()) {
                val totalCP = cursorCP.getDouble(cursorCP.getColumnIndexOrThrow("TotalCP"))
                saldoContasPagarCorreto = Math.round(totalCP * 100.0) / 100.0
            }
            cursorCP.close()

            val adjustedSaldoCP = saldoContasPagarCorreto
            acumulado += adjustedSaldoCP
            totalGrupo += adjustedSaldoCP
            totalPrincipal += adjustedSaldoCP

            items.add(BalancoItem(
                "", "", "Contas à Pagar", "CONTAS À PAGAR",
                decimalFormat.format(adjustedSaldoCP),
                decimalFormat.format(acumulado)
            ))
            items.add(BalancoItem("", "", "", "TOTAL DA CONTA Contas à Pagar", decimalFormat.format(adjustedSaldoCP), decimalFormat.format(acumulado)))
            tempContaTotais["Contas_a_Pagar"] = adjustedSaldoCP

            if (sub != " ") {
                items.add(BalancoItem("", "", "", "TOTAL DO GRUPO $sub", decimalFormat.format(totalGrupo), decimalFormat.format(acumulado)))
            }

            if (prin != "a") {
                items.add(BalancoItem("", "", "", "SUB DO PRINCIPAL $prin", "", decimalFormat.format(acumulado)))
                ativo = totalPrincipal
            }

            contaTotais = tempContaTotais
            Log.d("BalancoConsulta", "contaTotais: $contaTotais") // Log para depuração

            // Consulta para Receitas e Despesas
            val sql2 = when {
                binding.rbVcto.isChecked -> """
                    SELECT SUM(CASE WHEN nome_P = 'RECEITAS' THEN Valor ELSE 0 END) AS TotalReceitas,
                           SUM(CASE WHEN nome_P = 'DESPESAS' THEN Valor ELSE 0 END) AS TotalDespesas
                    FROM viewmovrd 
                    WHERE dtVcto BETWEEN ? AND ? AND nome_P <> 'NULO'
                    AND vRecurso NOT IN ('2.001.004')
                """
                binding.rbEmissao.isChecked -> """
                    SELECT SUM(CASE WHEN nome_P = 'RECEITAS' THEN Valor ELSE 0 END) AS TotalReceitas,
                           SUM(CASE WHEN nome_P = 'DESPESAS' THEN Valor ELSE 0 END) AS TotalDespesas
                    FROM viewmovrd 
                    WHERE dtEmi BETWEEN ? AND ? AND nome_P <> 'NULO'
                    AND vRecurso NOT IN ('2.001.004')
                """
                else -> """
                    SELECT SUM(CASE WHEN nome_P = 'RECEITAS' THEN Valor ELSE 0 END) AS TotalReceitas,
                           SUM(CASE WHEN nome_P = 'DESPESAS' THEN Valor ELSE 0 END) AS TotalDespesas
                    FROM viewmovrd 
                    WHERE dtApr BETWEEN ? AND ? AND nome_P <> 'NULO'
                    AND vRecurso NOT IN ('2.001.004')
                """
            }

            val cursor2 = db.rawQuery(sql2, arrayOf(dataIniFormatada, dataFimFormatada))
            var receitas = 0.0
            var despesas = 0.0
            if (cursor2.moveToFirst()) {
                receitas = cursor2.getDouble(cursor2.getColumnIndexOrThrow("TotalReceitas"))
                despesas = cursor2.getDouble(cursor2.getColumnIndexOrThrow("TotalDespesas"))
            }

            passivo = acumulado
            ativo = passivo
            patrLiq = -(acumulado - resultado)
            resultado = -(receitas + despesas)

            // Análises financeiras
            val atCiclico = contaTotais.getOrDefault("Contas_a_Receber", 0.0) +
                    contaTotais.getOrDefault("Emprest_Conced._C._Prazo", 0.0)
            val atCirculante = contaTotais.getOrDefault("Bancos", 0.0) +
                    contaTotais.getOrDefault("Caixa", 0.0) +
                    contaTotais.getOrDefault("Contas_a_Receber", 0.0) +
                    contaTotais.getOrDefault("Conta_Poupanca", 0.0)
            val pasCiclico = -(contaTotais.getOrDefault("Contas_a_Pagar", 0.0) +
                    contaTotais.getOrDefault("Cartões_de_Crédito", 0.0) +
                    contaTotais.getOrDefault("Fornecedores", 0.0))
            val pasNaoCiclico = -contaTotais.getOrDefault("Emprestde_Longo_Prazo_(+_de_1_ano)", 0.0)
            val passivoTotalCirculanteENaoCirculante = pasCiclico + pasNaoCiclico
            val anCiclico = contaTotais.getOrDefault("Imobilizado", 0.0)
            val pnCiclico = patrLiq
            val atErratico = contaTotais.getOrDefault("Bancos", 0.0) +
                    contaTotais.getOrDefault("Caixa", 0.0) +
                    contaTotais.getOrDefault("Conta_Poupanca", 0.0)
            val paErratico = -contaTotais.getOrDefault("EmprObtidos_Curto_Prazo", 0.0)

            Log.d("BalancoConsulta", "atCiclico: $atCiclico, pasCiclico: $pasCiclico, anCiclico: $anCiclico, " +
                    "pnCiclico: $pnCiclico, atErratico: $atErratico, paErratico: $paErratico, " +
                    "passivoTotalCirculanteENaoCirculante: $passivoTotalCirculanteENaoCirculante")

            // Cálculo das métricas do Modelo Fleuriet
            val CDG = -pnCiclico - anCiclico
            val NCG = atCiclico - pasCiclico
            val ST = atErratico - paErratico

            Log.d("BalancoConsulta", "CDG: $CDG, NCG: $NCG, ST: $ST")

            // Determinar os sinais de CDG, NCG e ST
            val sinalCDG = if (CDG >= 0) "+" else "-"
            val sinalNCG = if (NCG >= 0) "+" else "-"
            val sinalST = if (ST >= 0) "+" else "-"

            // Determinar a situação financeira
            val situacaoFinanceira = when {
                sinalCDG == "+" && sinalNCG == "-" && sinalST == "+" -> "Excelente"
                sinalCDG == "+" && sinalNCG == "+" && sinalST == "+" -> "Sólida"
                sinalCDG == "+" && sinalNCG == "+" && sinalST == "-" -> "Insatisfatória"
                sinalCDG == "-" && sinalNCG == "+" && sinalST == "-" -> "Péssima"
                sinalCDG == "-" && sinalNCG == "-" && sinalST == "-" -> "Muito Ruim"
                sinalCDG == "-" && sinalNCG == "-" && sinalST == "+" -> "Alto Risco"
                else -> "Não Classificada"
            }

            // Proteger divisões por zero
            val roe = if (patrLiq != 0.0) percentFormat.format(-resultado / -patrLiq) else "N/A"
            val roa = if (acumulado - subPassivo + patrLiq != 0.0) percentFormat.format(-resultado / (-(acumulado - subPassivo + patrLiq))) else "N/A"
            val consumo = if (receitas != 0.0) percentFormat.format(-despesas / receitas) else "N/A"
            val pct = if (subPassivo != 0.0) percentFormat.format(passivoTotalCirculanteENaoCirculante / -subPassivo) else "N/A"
            val liquidez = if ((-pasCiclico - paErratico) != 0.0) indexFormat.format((atCiclico + atErratico) / (-pasCiclico - paErratico)) else "N/A"
            val cobertura = if (despesas != 0.0) "${indexFormat.format((atErratico + atCiclico) / -despesas)} meses" else "N/A"
            val coberturaDias = if (despesas != 0.0) "${daysFormat.format(((atErratico + atCiclico) / -despesas) * 30)} dias" else "N/A"
            val endividamento = if (subPassivo != 0.0) indexFormat.format((-pasCiclico - paErratico) / subPassivo) else "N/A"
            val poupanca = if (receitas != 0.0) percentFormat.format(-resultado / receitas) else "N/A"

            Log.d("BalancoConsulta", "roe: $roe, roa: $roa, consumo: $consumo, pct: $pct, " +
                    "liquidez: $liquidez, cobertura: $cobertura, coberturaDias: $coberturaDias, " +
                    "endividamento: $endividamento, poupanca: $poupanca, situacaoFinanceira: $situacaoFinanceira")

            // Adicionar linhas ao modelo
            items.add(BalancoItem("", "", "", "", "", ""))
            items.add(BalancoItem("", "PATRIMÔNIO_LÍQUIDO", "Patrimônio_Acumulado", "", decimalFormat.format(patrLiq), ""))
            items.add(BalancoItem("", "", "", "", "", ""))
            items.add(BalancoItem("", "", "", "RECEITAS NO PERÍODO", decimalFormat.format(receitas), ""))
            items.add(BalancoItem("", "", "", "DESPESAS NO PERÍODO", decimalFormat.format(despesas), ""))
            items.add(BalancoItem("", "", "", "RESULTADO DO PERÍODO", decimalFormat.format(-resultado), ""))
            items.add(BalancoItem("", "", "", "", "", ""))
            items.add(BalancoItem("", "", "", "TOTAL DO PASSIVO", "", decimalFormat.format(-subPassivo)))
            items.add(BalancoItem("", "", "", "", "", ""))
            items.add(BalancoItem("", "   A N Á L I S E S  ", "", "", "", ""))
            items.add(BalancoItem("", "   ==========  ", "", "", "", ""))
            items.add(BalancoItem("", "ROE(Return on Common Equity)", "RETORNO SOBRE CAPITAL PRÓPRIO", "Resultado(Lucro ou Prejuízo)/Patrimônio Liq.", "", roe))
            items.add(BalancoItem("", "ROA(Rentab.Op.do Ativo ou TIR)", "RETORNO SOBRE O ATIVO", "Resultado(Lucro ou Prejuízo)/Ativo Total", "", roa))
            items.add(BalancoItem("", "CONSUMO SOBRE A RECEITA", "TAXA DE CONSUMO", "Despesas/Receitas", "", consumo))
            items.add(BalancoItem("", "USO DO CAPITAL DE TERCEIROS", "PCT-PARTICIPAÇÃO DO CAPITAL DE TERCEIROS", "Passivo Circulante e não Circulante/Passivo Total", "", if (pct != "N/A" && !pct.startsWith("-")) "-$pct" else if (pct.startsWith("-")) pct.removePrefix("-") else pct))
            items.add(BalancoItem("", "", "", "", "", ""))
            items.add(BalancoItem("", "ATIVO E PASSIVO CÍCLICO (Operacionais)", "AC - ATIVO CÍCLICO", "Ctas.à Receber + Emprest.Conced.C.Prazo", "", decimalFormat.format(atCiclico)))
            items.add(BalancoItem("", "", "PC - PASSIVO CÍCLICO", "Ctas.à Pagar + Cartões + Fornecedores", "", decimalFormat.format(pasCiclico)))
            items.add(BalancoItem("", "", "", "", "", ""))
            items.add(BalancoItem("", "ATIVO E PASSIVO NÃO CÍCLICOS (Não Circulante)", "ANC - ATIVO NÃO CÍCLICO", "Realiz. Longo Prazo + Imobilizado", "", decimalFormat.format(anCiclico)))
            items.add(BalancoItem("", "", "PNC - PASSIVO NÃO CÍCLICO", "Financ.Longo Prazo + Capital Social", "", decimalFormat.format(-pnCiclico)))
            items.add(BalancoItem("", "", "", "", "", ""))
            items.add(BalancoItem("", "ATIVO E PASSIVO ERRÁTICOS (Financeiro)", "AE - ATIVO ERRÁTICO", "Caixa + Equiv. de caixa", "", decimalFormat.format(atErratico)))
            items.add(BalancoItem("", "", "PE - PASSIVO ERRÁTICO", "Outros Financiamentos de Curto Prazo", "", decimalFormat.format(paErratico)))
            items.add(BalancoItem("", "", "", "", "", ""))
            items.add(BalancoItem("", "ANÁLISE FINANCEIRA (Fleuriet)", "CDG-Capital de Giro", "PNC - ANC", "", decimalFormat.format(CDG)))
            items.add(BalancoItem("", "", "NCG-Necessidade Capital de Giro", "AC - PC", "", decimalFormat.format(NCG)))
            items.add(BalancoItem("", "", "ST -Situação de Tesouraria", "AE - PE", "", decimalFormat.format(ST)))
            items.add(BalancoItem("", "", "Situação Financeira", "$sinalCDG $sinalNCG $sinalST", "", situacaoFinanceira))
            items.add(BalancoItem("", "", "", "", "", ""))
            items.add(BalancoItem("", "Í N D I C E S", "", "", "", ""))
            items.add(BalancoItem("", "------------------", "", "", "", ""))
            items.add(BalancoItem("", "ÍNDICE DE LIQUIDEZ", "Ideal acima de 1", "Ativo C.Prazo/Passivo C.Prazo", if (liquidez != "N/A" && !liquidez.startsWith("-")) "-$liquidez" else if (liquidez.startsWith("-")) liquidez.removePrefix("-") else liquidez, ""))
            items.add(BalancoItem("", "ÍNDICE DE COBERTURA DESPESAS", "Ideal acima de 6", "Ativo C.Prazo/Despesas Mensais", cobertura, coberturaDias))
            items.add(BalancoItem("", "ÍNDICE DE ENDIVIDAMENTO", "Ideal próximo a 0", "Passivo Exigível/Ativo Total", if (endividamento != "N/A" && !endividamento.startsWith("-")) "-$endividamento" else if (endividamento.startsWith("-")) endividamento.removePrefix("-") else endividamento, ""))
            items.add(BalancoItem("", "ÍNDICE DE POUPANÇA", "Ideal acima de 10%", "Resultado Disponível p/Investir/Receitas", poupanca, ""))
            items.add(BalancoItem("", "", "", "", "", ""))
            items.add(BalancoItem("", "PLANEJAMENTO FINANCEIRO", "", "", "", " %Atingido "))
            items.add(BalancoItem("", "-----------------------------------------", "", "", "", "-------------"))
            items.add(BalancoItem("", "PMS", "Patrimônio Mínimo de Sobrevivência", "6 x Valor Desp.Mensais", decimalFormat.format(-despesas * 6), if (despesas != 0.0) percentFormat.format(atCirculante / (-despesas * 6)) else "N/A"))
            items.add(BalancoItem("", "PMR", "Patrimônio Mínimo Recomendado", "20 x Valor Desp.Mensais", decimalFormat.format(-despesas * 20), if (despesas != 0.0) percentFormat.format(atCirculante / (-despesas * 20)) else "N/A"))
            items.add(BalancoItem("", "PI", "Patrimônio Ideal", "12 x Vr.Desp.Mensais x 10% x Idade", decimalFormat.format((-despesas * 12) * 0.1 * 60), if (despesas != 0.0) percentFormat.format(atCirculante / ((-despesas * 12) * 0.1 * 60)) else "N/A"))
            items.add(BalancoItem("", "PNIF", "Patrim.Nec.p/Indep.Financeira", "12 x Desp.Mensais / Menor % Rentabilidade a.a.", decimalFormat.format(-despesas * 12 / 0.06), if (despesas != 0.0) percentFormat.format(atCirculante / (-despesas * 12 / 0.06)) else "N/A"))
            items.add(BalancoItem("", "", "", "", "", ""))

            items.add(BalancoItem("", " D I A G N Ó S T I C O   D O   S I S T E M A ", "", "", "", ""))
            items.add(BalancoItem("", "-----------------------------------------", "", "", "", ""))
            items.add(BalancoItem("", "", "--- Análise Consultiva ---", "", "", ""))

            // 1. Diagnóstico de Liquidez
            var liqValor = 0.0
            try {
                if (liquidez != "N/A") {
                    liqValor = liquidez.replace(",", ".").toDouble()
                }
            } catch (e: Exception) {
                // continua com 0.0
            }
            val labelLiq: String
            val descLiq: String
            if (liqValor > 1.5) {
                labelLiq = "EXCELENTE:"
                descLiq = "Você tem forte folga financeira para honrar compromissos."
            } else if (liqValor >= 1.0) {
                labelLiq = "BOM:"
                descLiq = "Seus recursos cobrem suas dívidas, mas sem grande margem."
            } else {
                labelLiq = "CRÍTICO:"
                descLiq = "Você pode precisar de crédito para pagar contas imediatas."
            }
            items.add(BalancoItem("", "SAÚDE FINANCEIRA:", labelLiq, descLiq, "", ""))

            // 2. Diagnóstico de Capital de Giro (Fleuriet)
            val labelCG: String
            val descCG: String
            if (CDG >= 0 && ST >= 0) {
                labelCG = "ESTRATÉGIA:"
                descCG = "Posição sólida. Momento ideal para investimentos ou novos aportes."
            } else if (ST < 0) {
                labelCG = "ATENÇÃO:"
                descCG = "Sua tesouraria está negativa. Cuidado com juros de cheque especial/cartão."
            } else {
                labelCG = "REVISÃO:"
                descCG = "Verifique se seus prazos de pagamento estão muito curtos em relação aos recebimentos."
            }
            items.add(BalancoItem("", "CAPITAL DE GIRO:", labelCG, descCG, "", ""))

            // 3. Diagnóstico de Poupança (Rentabilidade)
            var poupValor = 0.0
            try {
                if (poupanca != "N/A") {
                    poupValor = poupanca.replace("%", "").replace(",", ".").toDouble()
                }
            } catch (e: Exception) {
                // continua com 0
            }
            val labelPoup: String
            val descPoup: String
            if (poupValor > 20) {
                labelPoup = "ALTA PERFORMANCE:"
                descPoup = "Você retém uma excelente fatia da sua receita."
            } else if (poupValor >= 10) {
                labelPoup = "DENTRO DA META:"
                descPoup = "Sua taxa de poupança está saudável (acima de 10%)."
            } else {
                labelPoup = "RISCO:"
                descPoup = "Margem de sobra muito baixa. Qualquer imprevisto pode gerar endividamento."
            }
            items.add(BalancoItem("", "CAPACIDADE DE ACÚMULO:", labelPoup, descPoup, "", ""))

            // 4. Dica de Gestão (Baseado no Endividamento)
            var endivValor = 0.0
            try {
                if (endividamento != "N/A") {
                    endivValor = endividamento.replace(",", ".").toDouble()
                }
            } catch (e: Exception) {
                // continua com 0
            }
            val labelDica = if (endivValor < 0.3) "OPORTUNIDADE:" else "PRIORIDADE:"
            val descDica = if (endivValor < 0.3) {
                "Baixo endividamento. Tem espaço para alavancar projetos com capital de terceiros."
            } else {
                "Foque na redução de custos fixos e quitação de dívidas de curto prazo."
            }
            items.add(BalancoItem("", "DICA DO CONSULTOR:", labelDica, descDica, "", ""))
            items.add(BalancoItem("", "-----------------------------------------", "", "", "", ""))

            binding.recyclerViewBalanco.adapter = BalancoAdapter(items)

            cursor.close()
            cursor2.close()
            db.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao realizar pesquisa: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("BalancoConsultaActivity", "Erro na pesquisa: ${e.message}", e)
        }
    }

    private fun imprimir() {
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        val adapter = binding.recyclerViewBalanco.adapter as BalancoAdapter
        val jobName = "Balanço_${SimpleDateFormat("dd_MM_yyyy_HH_mm", Locale.getDefault()).format(Date())}"
        printManager.print(
            jobName,
            BalancoPrintAdapter(adapter.getItems()),
            PrintAttributes.Builder().build()
        )
    }

    private fun exportar() {
        val fileName = "Balanço_${SimpleDateFormat("dd_MM_yyyy_HH_mm", Locale.getDefault()).format(Date())}.xlsx"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Balanço")
            val items = (binding.recyclerViewBalanco.adapter as BalancoAdapter).getItems()

            items.forEachIndexed { index, item ->
                val row = sheet.createRow(index)
                row.createCell(0).setCellValue(item.princ)
                row.createCell(1).setCellValue(item.sub)
                row.createCell(2).setCellValue(item.conta)
                row.createCell(3).setCellValue(item.nome)
                row.createCell(4).setCellValue(item.total)
                row.createCell(5).setCellValue(item.acumulado)
            }

            for (i in 0 until 6) {
                sheet.setColumnWidth(i, 15 * 256)
            }

            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()

            Toast.makeText(this, "Arquivo Excel gerado em ${file.absolutePath}!", Toast.LENGTH_LONG).show()
            Log.d("BalancoConsultaActivity", "Arquivo Excel gerado com sucesso")
        } catch (t: Throwable) {
            Toast.makeText(this, "Erro ao exportar: ${t.message}", Toast.LENGTH_LONG).show()
            Log.e("BalancoConsultaActivity", "Erro ao exportar Excel: ${t.message}", t)
        }
    }

    private fun gerarGrafico() {
        if (contaTotais.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para gerar o gráfico! Realize uma pesquisa primeiro.", Toast.LENGTH_SHORT).show()
            Log.e("BalancoConsulta", "contaTotais vazio")
            return
        }

        val atCiclico = contaTotais.getOrDefault("Contas_a_Receber", 0.0) +
                contaTotais.getOrDefault("Emprest_Conced_C_Prazo", 0.0)
        val pasCiclico = -(contaTotais.getOrDefault("Contas_a_Pagar", 0.0) +
                contaTotais.getOrDefault("Cartoes_de_Credito", 0.0) +
                contaTotais.getOrDefault("Fornecedores", 0.0))
        val anCiclico = contaTotais.getOrDefault("Imobilizado", 0.0)
        val atErratico = contaTotais.getOrDefault("Bancos", 0.0) +
                contaTotais.getOrDefault("Caixa", 0.0) +
                contaTotais.getOrDefault("Conta_Poupanca", 0.0)
        val paErratico = -contaTotais.getOrDefault("EmprObtidos_Curto_Prazo", 0.0)

        Log.d("BalancoConsulta", "atCiclico: $atCiclico, pasCiclico: $pasCiclico, anCiclico: $anCiclico, atErratico: $atErratico, paErratico: $paErratico")

        val entries = listOf(
            PieEntry(atCiclico.toFloat(), "Ativo Cíclico"),
            PieEntry(pasCiclico.toFloat(), "Passivo Cíclico"),
            PieEntry(anCiclico.toFloat(), "Ativo Não Cíclico"),
            PieEntry(atErratico.toFloat(), "Ativo Errático"),
            PieEntry(paErratico.toFloat(), "Passivo Errático")
        ).filter { it.value != 0f }

        if (entries.isEmpty()) {
            Toast.makeText(this, "Nenhum dado válido para o gráfico!", Toast.LENGTH_SHORT).show()
            Log.e("BalancoConsulta", "Nenhuma entrada válida para o gráfico")
            return
        }

        val dataSet = PieDataSet(entries, "Balanço Patrimonial").apply {
            setColors(
                intArrayOf(
                    R.color.green_dark,
                    R.color.red,
                    R.color.blue,
                    R.color.green_light,
                    R.color.red_dark
                ),
                this@BalancoConsultaActivity
            )
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return decimalFormat.format(value.toDouble())
                }
            }
        }

        val pieData = PieData(dataSet)
        val chart = PieChart(this).apply {
            data = pieData
            description.text = "Balanço Patrimonial"
            description.textSize = 12f
            setEntryLabelTextSize(10f)
            setEntryLabelColor(Color.BLACK)
            animateY(1000)
        }

        val frameLayout = FrameLayout(this)
        frameLayout.addView(chart)
        chart.layoutParams = FrameLayout.LayoutParams(
            (900 * resources.displayMetrics.density).toInt(),
            (700 * resources.displayMetrics.density).toInt()
        ).apply {
            setMargins(16, 16, 16, 16)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Gráfico do Balanço")
            .setView(frameLayout)
            .setPositiveButton("Fechar") { _, _ -> }
            .create()

        dialog.show()
        chart.invalidate()
    }
}

data class BalancoItem(
    val princ: String,
    val sub: String,
    val conta: String,
    val nome: String,
    val total: String,
    val acumulado: String
)

class BalancoAdapter(private val items: List<BalancoItem>) :
    RecyclerView.Adapter<BalancoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemBalancoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBalancoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            textPrinc.text = item.princ
            textSub.text = item.sub
            textConta.text = item.conta
            textNome.text = item.nome
            textTotal.text = item.total
            textAcumulado.text = item.acumulado
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItems(): List<BalancoItem> = items
}

class BalancoPrintAdapter(private val items: List<BalancoItem>) : PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }

        val pages = (items.size / 12) + 1
        callback?.onLayoutFinished(
            PrintDocumentInfo.Builder("Balanço.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(pages)
                .build(),
            true
        )
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        try {
            val pdfWriter = PdfWriter(FileOutputStream(destination?.fileDescriptor))
            val pdfDocument = PdfDocument(pdfWriter)
            pdfDocument.setDefaultPageSize(PageSize.A4)
            val document = Document(pdfDocument)

            val columnWidths = floatArrayOf(15f, 30f, 30f, 35f, 15f, 15f)
            var itemCount = 0
            var currentTable: Table? = null
            val itemsPerPage = 12

            items.forEachIndexed { index, item ->
                if (itemCount % itemsPerPage == 0) {
                    currentTable?.let { document.add(it) }
                    currentTable = Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth()
                    if (itemCount == 0) {
                        currentTable?.addHeaderCell(Cell().add(Paragraph("Principal").setBold()))
                        currentTable?.addHeaderCell(Cell().add(Paragraph("Sub").setBold()))
                        currentTable?.addHeaderCell(Cell().add(Paragraph("Conta").setBold()))
                        currentTable?.addHeaderCell(Cell().add(Paragraph("Nome").setBold()))
                        currentTable?.addHeaderCell(Cell().add(Paragraph("Total").setBold()))
                        currentTable?.addHeaderCell(Cell().add(Paragraph("Acumulado").setBold()))
                    }
                }

                currentTable?.addCell(Cell().add(Paragraph(item.princ)))
                currentTable?.addCell(Cell().add(Paragraph(item.sub)))
                currentTable?.addCell(Cell().add(Paragraph(item.conta)))
                currentTable?.addCell(Cell().add(Paragraph(item.nome)))
                currentTable?.addCell(Cell().add(Paragraph(item.total)))
                currentTable?.addCell(Cell().add(Paragraph(item.acumulado)))

                itemCount++
                if (index == items.size - 1) {
                    currentTable?.let { document.add(it) }
                }
            }

            document.close()
            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback?.onWriteFailed(e.message)
            Log.e("BalancoPrintAdapter", "Erro ao gerar PDF: ${e.message}", e)
        }
    }
}