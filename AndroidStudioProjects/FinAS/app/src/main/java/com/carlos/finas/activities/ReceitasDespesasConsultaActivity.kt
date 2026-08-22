package com.carlos.finas.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.R
import com.carlos.finas.adapters.ReceitaDespesa
import com.carlos.finas.adapters.ReceitasDespesasAdapter
import com.carlos.finas.databinding.ActivityReceitasDespesasConsultaBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.time.LocalDate
import java.time.ZoneId

data class Totais(
    val receitasOperacionais: Double,
    val despesasOperacionais: Double,
    val receitasNaoOperacionais: Double,
    val despesasNaoOperacionais: Double
)

class ReceitasDespesasConsultaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceitasDespesasConsultaBinding
    private lateinit var dbHelper: DatabaseHelper
    private val decimalFormat = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
    private val percentFormat = DecimalFormat("0.0000%", DecimalFormatSymbols(Locale("pt", "BR")))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())
    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val inputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val createDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            CoroutineScope(Dispatchers.Main).launch {
                generateAndSavePdf(uri)
            }
        } else {
            Toast.makeText(this, "Nenhum local selecionado para salvar o PDF!", Toast.LENGTH_SHORT).show()
        }
    }

    private val exportDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            CoroutineScope(Dispatchers.Main).launch {
                exportToCsv(uri)
            }
        } else {
            Toast.makeText(this, "Nenhum local selecionado para salvar o CSV!", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        binding = ActivityReceitasDespesasConsultaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DatabaseHelper(this)

        binding.recyclerViewRd.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRd.adapter = ReceitasDespesasAdapter.create(emptyList()) { item ->
            Toast.makeText(this, "Clicou em ${item.conta}", Toast.LENGTH_SHORT).show()
        }

        // Adicionar DateMaskWatcher para formatação automática de datas
        binding.editDataIni.addTextChangedListener(DateMaskWatcher())
        binding.editDataFim.addTextChangedListener(DateMaskWatcher())

        // Adicionar DatePicker para editDataIni
        binding.editDataIni.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                binding.editDataIni.setText(String.format("%02d/%02d/%d", d, m + 1, y))
            }, year, month, day)
            datePicker.show()
        }

        // Adicionar DatePicker para editDataFim
        binding.editDataFim.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                binding.editDataFim.setText(String.format("%02d/%02d/%d", d, m + 1, y))
            }, year, month, day)
            datePicker.show()
        }

        binding.btnPesquisar.setOnClickListener { pesquisar() }
        binding.btnLimpar.setOnClickListener { limpar() }
        binding.btnImprimir.setOnClickListener {
            Log.d("ReceitasDespesasConsultaActivity", "Botão Imprimir clicado")
            val adapter = binding.recyclerViewRd.adapter as? ReceitasDespesasAdapter
            if (adapter == null || adapter.itemCount == 0) {
                Toast.makeText(this, "Nenhum dado para gerar o PDF!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createDocument.launch("Receitas_Despesas_${dateFormat.format(Date()).replace(":", "-")}.pdf")
        }
        binding.btnExportar.setOnClickListener {
            val adapter = binding.recyclerViewRd.adapter as? ReceitasDespesasAdapter
            if (adapter == null || adapter.itemCount == 0) {
                Toast.makeText(this, "Nenhum dado para exportar!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            exportDocument.launch("Receitas_Despesas_${dateFormat.format(Date()).replace(":", "-")}.csv")
        }
        binding.btnGraficoRec.setOnClickListener { gerarGraficoReceitas() }
        binding.btnGraficoDesp.setOnClickListener { gerarGraficoDespesas() }
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

    @SuppressLint("LongLogTag")
    private fun pesquisar() {
        val dataIni = binding.editDataIni.text.toString().trim()
        val dataFim = binding.editDataFim.text.toString().trim()

        if (dataIni.length < 10) {
            Toast.makeText(this, "Atenção! Preencha a data inicial.", Toast.LENGTH_SHORT).show()
            return
        }
        if (dataFim.length < 10) {
            Toast.makeText(this, "Atenção! Preencha a data final.", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val (items, totaisReceitasDespesas, totais) = withContext(Dispatchers.IO) {
                    val db = dbHelper.readableDatabase
                    val regime = when (binding.radioGroupRegime.checkedRadioButtonId) {
                        R.id.rb_vcto -> "dtVcto"
                        R.id.rb_competencia -> "dtEmi"
                        else -> "dtApr"
                    }
                    val dataIniDb = formatarDataInputParaBanco(dataIni)
                    val dataFimDb = formatarDataInputParaBanco(dataFim)

                    // Calcular data inicial histórica (24 meses antes da data final)
                    val dateFimp = inputDateFormat.parse(dataFim)
                    val dataFimLocal = dateFimp?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    val dataInicioHist = dataFimLocal?.minusMonths(24)
                    val dataInicioHistDb = dataInicioHist?.let {
                        dbDateFormat.format(Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    } ?: ""

                    // Mapa para armazenar percentuais históricos
                    val percentuaisHistoricos = mutableMapOf<String, Double>()
                    var totalReceitasHist = 0.0
                    var totalDespesasHist = 0.0

                    // Consulta para totais históricos por nome_S (últimos 24 meses)
                    val queryHistTotais = "SELECT nome_S, SUM(Valor) AS Total FROM viewmovrd WHERE nome_P <> 'NULO' AND $regime >= ? GROUP BY nome_S"
                    db.rawQuery(queryHistTotais, arrayOf(dataInicioHistDb)).use { cursor ->
                        while (cursor.moveToNext()) {
                            val nomeS = cursor.getString(cursor.getColumnIndexOrThrow("nome_S"))
                            val total = cursor.getDouble(cursor.getColumnIndexOrThrow("Total"))
                            if (nomeS.contains("RECEITAS", ignoreCase = true)) {
                                totalReceitasHist += total
                            } else if (nomeS.contains("DESPESAS", ignoreCase = true)) {
                                totalDespesasHist += total
                            }
                        }
                    }

                    // Consulta para totais históricos por nome_C (últimos 24 meses)
                    val queryHist = "SELECT nome_C, nome_S, SUM(Valor) AS Total FROM viewmovrd WHERE nome_P <> 'NULO' AND $regime >= ? GROUP BY nome_C, nome_S"
                    db.rawQuery(queryHist, arrayOf(dataInicioHistDb)).use { cursor ->
                        while (cursor.moveToNext()) {
                            val nomeC = cursor.getString(cursor.getColumnIndexOrThrow("nome_C"))
                            val total = cursor.getDouble(cursor.getColumnIndexOrThrow("Total"))
                            val percentHist = when {
                                total > 0 && totalReceitasHist != 0.0 -> (total / totalReceitasHist) * 100
                                total < 0 && totalDespesasHist != 0.0 -> (total / totalDespesasHist) * 100
                                else -> 0.0
                            }
                            percentuaisHistoricos[nomeC] = percentHist
                        }
                    }

                    var receitasOperacionais = 0.0
                    var receitasNaoOperacionais = 0.0
                    var despesasOperacionais = 0.0
                    var despesasNaoOperacionais = 0.0
                    var totalReceitas = 0.0
                    var totalDespesas = 0.0

                    // Consulta para totais do período selecionado
                    val queryGrupos = "SELECT nome_S, SUM(Valor) AS Total FROM viewmovrd WHERE $regime BETWEEN ? AND ? AND nome_P <> 'NULO' GROUP BY nome_S"
                    db.rawQuery(queryGrupos, arrayOf(dataIniDb, dataFimDb)).use { cursor ->
                        while (cursor.moveToNext()) {
                            val nomeS = cursor.getString(cursor.getColumnIndexOrThrow("nome_S"))
                            val total = cursor.getDouble(cursor.getColumnIndexOrThrow("Total"))
                            when (nomeS.uppercase()) {
                                "RECEITAS OPERACIONAIS" -> {
                                    receitasOperacionais += total
                                    totalReceitas += total
                                }
                                "RECEITAS NÃO OPERACIONAIS", "RECEITAS NAO OPERACIONAIS" -> {
                                    receitasNaoOperacionais += total
                                    totalReceitas += total
                                }
                                "DESPESAS OPERACIONAIS" -> {
                                    despesasOperacionais += total
                                    totalDespesas += total
                                }
                                "DESPESAS NÃO OPERACIONAIS", "DESPESAS NAO OPERACIONAIS" -> {
                                    despesasNaoOperacionais += total
                                    totalDespesas += total
                                }
                            }
                        }
                    }

                    // Consulta principal para preencher os itens
                    val queryContas = "SELECT nome_P, nome_S, nome_C, SUM(Valor) AS Total FROM viewmovrd WHERE $regime BETWEEN ? AND ? AND nome_P <> 'NULO' GROUP BY nome_C ORDER BY classif"
                    val items = mutableListOf<ReceitaDespesa>()
                    var acumulado = 0.0
                    var lastPrincipal = ""
                    var principal = ""
                    var grupo = ""
                    db.rawQuery(queryContas, arrayOf(dataIniDb, dataFimDb)).use { cursor ->
                        while (cursor.moveToNext()) {
                            val nomeP = cursor.getString(cursor.getColumnIndexOrThrow("nome_P"))
                            val nomeS = cursor.getString(cursor.getColumnIndexOrThrow("nome_S"))
                            val nomeC = cursor.getString(cursor.getColumnIndexOrThrow("nome_C"))
                            val total = cursor.getDouble(cursor.getColumnIndexOrThrow("Total"))
                            val percentual = when {
                                total > 0 && totalReceitas != 0.0 -> (total / totalReceitas) * 100
                                total < 0 && totalDespesas != 0.0 -> (total / totalDespesas) * 100
                                else -> 0.0
                            }
                            val percentualHistorico = percentuaisHistoricos[nomeC] ?: 0.0
                            acumulado += total

                            if (lastPrincipal.isNotEmpty() && lastPrincipal != nomeP) {
                                items.add(ReceitaDespesa(null, null, null, null, null, null, null))
                            }

                            val principalDisplay = if (principal == nomeP) " " else nomeP
                            val grupoDisplay = if (grupo == nomeS) " " else nomeS

                            items.add(
                                ReceitaDespesa(
                                    principalDisplay,
                                    grupoDisplay,
                                    nomeC,
                                    total,
                                    acumulado,
                                    percentual,
                                    percentualHistorico
                                )
                            )

                            lastPrincipal = nomeP
                            principal = nomeP
                            grupo = nomeS
                        }
                    }

                    items.add(ReceitaDespesa(null, null, null, null, null, null, null))
                    items.add(
                        ReceitaDespesa(
                            null,
                            null,
                            "TAXA DE CONSUMO",
                            null,
                            null,
                            if (totalReceitas != 0.0) (-totalDespesas / totalReceitas) * 100 else 0.0,
                            null
                        )
                    )

                    val dateIni = inputDateFormat.parse(dataIni)
                    val dateFim = inputDateFormat.parse(dataFim)
                    val diasPeriodo = if (dateIni != null && dateFim != null) {
                        ((dateFim.time - dateIni.time) / (1000 * 60 * 60 * 24)) + 1
                    } else {
                        0L
                    }
                    val receitaPorDia = if (diasPeriodo != 0L) totalReceitas / diasPeriodo else 0.0
                    val diasConsumidos = if (receitaPorDia != 0.0) totalDespesas / receitaPorDia else 0.0

                    items.add(ReceitaDespesa(null, null, null, null, null, null, null))
                    items.add(
                        ReceitaDespesa(
                            null,
                            null,
                            "TOTAL DIAS NO PERÍODO: $diasPeriodo dias",
                            null,
                            null,
                            null,
                            null
                        )
                    )
                    items.add(
                        ReceitaDespesa(
                            null,
                            null,
                            "DIAS EQUIVALENTES CONSUMIDOS: ${-diasConsumidos.toInt()} dias",
                            null,
                            null,
                            null,
                            null
                        )
                    )
                    items.add(ReceitaDespesa(null, null, null, null, null, null, null))
                    items.add(
                        ReceitaDespesa(
                            null,
                            null,
                            "RECEITAS OPERACIONAIS",
                            null,
                            receitasOperacionais,
                            null,
                            null
                        )
                    )
                    items.add(
                        ReceitaDespesa(
                            null,
                            null,
                            "DESPESAS OPERACIONAIS",
                            null,
                            despesasOperacionais,
                            null,
                            null
                        )
                    )
                    items.add(
                        ReceitaDespesa(
                            null,
                            null,
                            "RESULTADO OPERACIONAL (RECEITAS-DESPESAS)",
                            null,
                            receitasOperacionais + despesasOperacionais,
                            null,
                            null
                        )
                    )
                    items.add(ReceitaDespesa(null, null, null, null, null, null, null))
                    items.add(
                        ReceitaDespesa(
                            null,
                            null,
                            "RECEITAS NÃO OPERACIONAIS",
                            null,
                            receitasNaoOperacionais,
                            null,
                            null
                        )
                    )
                    items.add(
                        ReceitaDespesa(
                            null,
                            null,
                            "DESPESAS NÃO OPERACIONAIS",
                            null,
                            despesasNaoOperacionais,
                            null,
                            null
                        )
                    )
                    items.add(
                        ReceitaDespesa(
                            null,
                            null,
                            "RESULTADO NÃO OPERACIONAL",
                            null,
                            receitasNaoOperacionais + despesasNaoOperacionais,
                            null,
                            null
                        )
                    )
                    items.add(ReceitaDespesa(null, null, null, null, null, null, null))
                    items.add(
                        ReceitaDespesa(
                            null,
                            null,
                            "RESULTADO FINAL DO PERÍODO:",
                            null,
                            receitasOperacionais + despesasOperacionais + receitasNaoOperacionais + despesasNaoOperacionais,
                            null,
                            null
                        )
                    )

                    db.close()
                    Triple(items, totalReceitas to totalDespesas, Totais(receitasOperacionais, despesasOperacionais, receitasNaoOperacionais, despesasNaoOperacionais))
                }

                val (totalReceitas, totalDespesas) = totaisReceitasDespesas
                val (receitasOperacionais, despesasOperacionais, receitasNaoOperacionais, despesasNaoOperacionais) = totais
                val adapter = binding.recyclerViewRd.adapter as? ReceitasDespesasAdapter
                adapter?.updateItems(items) ?: run {
                    binding.recyclerViewRd.adapter = ReceitasDespesasAdapter.create(items) { item ->
                        Toast.makeText(this@ReceitasDespesasConsultaActivity, "Clicou em ${item.conta}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ReceitasDespesasConsultaActivity, "Erro ao consultar: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("ReceitasDespesasConsultaActivity", "Erro em pesquisar: ${e.message}", e)
            }
        }
    }

    private fun limpar() {
        binding.recyclerViewRd.adapter = ReceitasDespesasAdapter.create(emptyList()) { item ->
            Toast.makeText(this, "Clicou em ${item.conta}", Toast.LENGTH_SHORT).show()
        }
        binding.editDataIni.text.clear()
        binding.editDataFim.text.clear()
        binding.radioGroupRegime.check(R.id.rb_vcto)
    }

    private fun formatarDataInputParaBanco(data: String): String {
        return try {
            val dataUtil = inputDateFormat.parse(data) ?: return ""
            dbDateFormat.format(dataUtil)
        } catch (e: Exception) {
            ""
        }
    }

    private fun generateAndSavePdf(uri: Uri) {
        val adapter = binding.recyclerViewRd.adapter as? ReceitasDespesasAdapter
        val items = adapter?.getItems() ?: emptyList()

        if (items.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para gerar o PDF!", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pdfDocument = PdfDocument()
                var pageNumber = 1
                var y = 60f
                val pageHeight = 595f
                val rowHeight = 20f
                val marginLeft = 40f
                val marginRight = 40f
                var page = pdfDocument.startPage(
                    PdfDocument.PageInfo.Builder(842, 595, pageNumber).create()
                )
                var canvas = page.canvas
                val paint = Paint().apply {
                    color = Color.BLACK
                    textSize = 10f
                    isAntiAlias = true
                }

                val currentDate = dateFormat.format(Date())
                canvas.drawText("Consulta - Receitas e Despesas - $currentDate", marginLeft, 40f, paint)

                val columnWidths = floatArrayOf(150f, 150f, 150f, 100f, 100f, 100f, 100f)
                val headers = arrayOf("Principal", "Grupo", "Conta", "Valor", "Acumulado", "Percentual", "Média Histórica (%)")
                var x = marginLeft

                headers.forEachIndexed { index, header ->
                    canvas.drawText(header, x, y, paint)
                    x += columnWidths[index]
                }
                y += rowHeight

                canvas.drawLine(marginLeft, y - 10f, 842f - marginRight, y - 10f, paint)
                x = marginLeft
                columnWidths.forEach { width ->
                    canvas.drawLine(x, y - 10f, x, pageHeight - 20f, paint)
                    x += width
                }
                canvas.drawLine(x, y - 10f, x, pageHeight - 20f, paint)

                items.forEachIndexed { index, item ->
                    if (y + rowHeight > pageHeight - 40f) {
                        canvas.drawText("Página $pageNumber", marginLeft, pageHeight - 20f, paint)
                        pdfDocument.finishPage(page)
                        pageNumber++
                        page = pdfDocument.startPage(
                            PdfDocument.PageInfo.Builder(842, 595, pageNumber).create()
                        )
                        canvas = page.canvas
                        y = 60f
                        canvas.drawText("Consulta - Receitas e Despesas - $currentDate", marginLeft, 40f, paint)
                        x = marginLeft
                        headers.forEachIndexed { headerIndex, header ->
                            canvas.drawText(header, x, y, paint)
                            x += columnWidths[headerIndex]
                        }
                        y += rowHeight
                        canvas.drawLine(marginLeft, y - 10f, 842f - marginRight, y - 10f, paint)
                        x = marginLeft
                        columnWidths.forEach { width ->
                            canvas.drawLine(x, y - 10f, x, pageHeight - 20f, paint)
                            x += width
                        }
                        canvas.drawLine(x, y - 10f, x, pageHeight - 20f, paint)
                    }

                    x = marginLeft
                    canvas.drawText(item.principal ?: "", x, y, paint)
                    x += columnWidths[0]
                    canvas.drawText(item.grupo ?: "", x, y, paint)
                    x += columnWidths[1]
                    canvas.drawText(item.conta ?: "", x, y, paint)
                    x += columnWidths[2]
                    canvas.drawText(item.total?.let { decimalFormat.format(it) } ?: "", x, y, paint)
                    x += columnWidths[3]
                    canvas.drawText(item.acumulado?.let { decimalFormat.format(it) } ?: "", x, y, paint)
                    x += columnWidths[4]
                    canvas.drawText(item.percentual?.let { percentFormat.format(it / 100) } ?: "", x, y, paint)
                    x += columnWidths[5]
                    canvas.drawText(item.percentualHistorico?.let { percentFormat.format(it / 100) } ?: "", x, y, paint)
                    y += rowHeight
                }

                canvas.drawLine(marginLeft, y, 842f - marginRight, y, paint)
                canvas.drawText("Página $pageNumber", marginLeft, pageHeight - 20f, paint)
                pdfDocument.finishPage(page)

                contentResolver.openFileDescriptor(uri, "w")?.use { descriptor ->
                    FileOutputStream(descriptor.fileDescriptor).use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                }
                pdfDocument.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReceitasDespesasConsultaActivity, "PDF gerado com sucesso!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReceitasDespesasConsultaActivity, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun exportToCsv(uri: Uri) {
        val adapter = binding.recyclerViewRd.adapter as? ReceitasDespesasAdapter
        val items = adapter?.getItems() ?: emptyList()

        if (items.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para exportar!", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val csvBuilder = StringBuilder()
                csvBuilder.append("Principal;Grupo;Conta;Valor;Acumulado;Percentual;Média Histórica (%)\n")

                items.forEach { item ->
                    val principal = item.principal?.replace(";", ",") ?: ""
                    val grupo = item.grupo?.replace(";", ",") ?: ""
                    val conta = item.conta?.replace(";", ",") ?: ""
                    val valor = item.total?.let { decimalFormat.format(it).replace("R$", "").trim() } ?: ""
                    val acumulado = item.acumulado?.let { decimalFormat.format(it).replace("R$", "").trim() } ?: ""
                    val percentual = item.percentual?.let { percentFormat.format(it / 100).replace("%", "") } ?: ""
                    val percentualHistorico = item.percentualHistorico?.let { percentFormat.format(it / 100).replace("%", "") } ?: ""
                    csvBuilder.append("$principal;$grupo;$conta;$valor;$acumulado;$percentual;$percentualHistorico\n")
                }

                contentResolver.openFileDescriptor(uri, "w")?.use { descriptor ->
                    FileOutputStream(descriptor.fileDescriptor).use { outputStream ->
                        outputStream.write(csvBuilder.toString().toByteArray())
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReceitasDespesasConsultaActivity, "CSV exportado com sucesso!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReceitasDespesasConsultaActivity, "Erro ao exportar CSV: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun gerarGraficoReceitas() {
        val adapter = binding.recyclerViewRd.adapter as? ReceitasDespesasAdapter
        val items = adapter?.getItems() ?: emptyList()

        if (items.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para gerar o gráfico!", Toast.LENGTH_SHORT).show()
            return
        }

        val receitas = items.filter { it.total != null && it.total > 0 }
        if (receitas.isEmpty()) {
            Toast.makeText(this, "Nenhuma receita para gerar o gráfico!", Toast.LENGTH_SHORT).show()
            return
        }

        val entries = receitas.mapNotNull { item ->
            item.conta?.let { conta ->
                item.total?.let { valor ->
                    PieEntry(valor.toFloat(), conta)
                }
            }
        }

        val dataSet = PieDataSet(entries, "Receitas").apply {
            setColors(intArrayOf(
                R.color.green_dark,
                R.color.green_light,
                R.color.green_darker,
                R.color.lime_green,
                R.color.light_green
            ), this@ReceitasDespesasConsultaActivity)
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
            description.text = "Receitas por Conta"
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
            .setTitle("Gráfico de Receitas")
            .setView(frameLayout)
            .setPositiveButton("Fechar") { _, _ -> }
            .create()

        dialog.show()
        chart.invalidate()
    }

    private fun gerarGraficoDespesas() {
        val adapter = binding.recyclerViewRd.adapter as? ReceitasDespesasAdapter
        val items = adapter?.getItems() ?: emptyList()

        if (items.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para gerar o gráfico!", Toast.LENGTH_SHORT).show()
            return
        }

        val despesas = items.filter { it.total != null && it.total < 0 }
        if (despesas.isEmpty()) {
            Toast.makeText(this, "Nenhuma despesa para gerar o gráfico!", Toast.LENGTH_SHORT).show()
            return
        }

        val entries = despesas.mapNotNull { item ->
            item.conta?.let { conta ->
                item.total?.let { valor ->
                    PieEntry(-valor.toFloat(), conta)
                }
            }
        }

        val dataSet = PieDataSet(entries, "Despesas").apply {
            setColors(intArrayOf(
                R.color.red_dark,
                R.color.red_darker,
                R.color.red_orange,
                R.color.tomato,
                R.color.crimson
            ), this@ReceitasDespesasConsultaActivity)
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
            description.text = "Despesas por Conta"
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
            .setTitle("Gráfico de Despesas")
            .setView(frameLayout)
            .setPositiveButton("Fechar") { _, _ -> }
            .create()

        dialog.show()
        chart.invalidate()
    }
}