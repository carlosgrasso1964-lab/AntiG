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
import com.carlos.finas.databinding.ActivityInflacaoConsultaBinding
import com.carlos.finas.databinding.ItemInflacaoBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
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
import kotlin.math.abs

class InflacaoConsultaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInflacaoConsultaBinding
    private val decimalFormat = DecimalFormat("#,##0.00")
    private val percentFormat = DecimalFormat("0.0000")
    private val dateFormatInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateFormatOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var inflacaoItems: List<InflacaoItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInflacaoConsultaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewInflacao.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewInflacao.adapter = InflacaoAdapter(emptyList())

        // Adicionar DateMaskWatcher para formatação automática de datas
        binding.editDataIni.addTextChangedListener(DateMaskWatcher())
        binding.editDataFim.addTextChangedListener(DateMaskWatcher())

        binding.btnPesquisar.setOnClickListener { pesquisar() }
        binding.btnLimpar.setOnClickListener { limpar() }
        binding.btnImprimir.setOnClickListener { imprimir() }
        binding.btnExportar.setOnClickListener { exportar() }
        binding.btnGrafico.setOnClickListener { gerarGrafico() }
        binding.btnFechar.setOnClickListener { finish() }
    }

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
        binding.recyclerViewInflacao.adapter = InflacaoAdapter(emptyList())
        inflacaoItems = emptyList()
        Toast.makeText(this, "Campos limpos!", Toast.LENGTH_SHORT).show()
    }

    private fun pesquisar() {
        val dataIniStr = binding.editDataIni.text.toString().trim()
        val dataFimStr = binding.editDataFim.text.toString().trim()

        if (dataIniStr.length < 10) {
            Toast.makeText(this, "Atenção! Favor preencher o campo de data inicial corretamente.", Toast.LENGTH_SHORT).show()
            return
        }
        if (dataFimStr.length < 10) {
            Toast.makeText(this, "Atenção! Favor preencher o campo de data final corretamente.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dataIniParsed = dateFormatInput.parse(dataIniStr) ?: return
            val dataFimParsed = dateFormatInput.parse(dataFimStr) ?: return

            val calendar = Calendar.getInstance()
            
            // Período Atual
            val dataIniAtualStr = dateFormatOutput.format(dataIniParsed)
            val dataFimAtualStr = dateFormatOutput.format(dataFimParsed)

            // Período Anterior (subtrai 1 ano)
            calendar.time = dataIniParsed
            calendar.add(Calendar.YEAR, -1)
            val dataIniAnteriorStr = dateFormatOutput.format(calendar.time)
            val dataIniAnteriorDisplay = dateFormatInput.format(calendar.time)

            calendar.time = dataFimParsed
            calendar.add(Calendar.YEAR, -1)
            val dataFimAnteriorStr = dateFormatOutput.format(calendar.time)
            val dataFimAnteriorDisplay = dateFormatInput.format(calendar.time)

            // Determinar campo de data
            val campoData = when {
                binding.rbVcto.isChecked -> "dtVcto"
                binding.rbCompetencia.isChecked -> "dtEmi"
                else -> "dtApr"
            }

            val dbHelper = DatabaseHelper(this)
            val db = dbHelper.readableDatabase

            // SQL traduzido do Java (SQLite compatível)
            val sql = """
                SELECT * FROM (
                    WITH PeriodoAtual AS (
                        SELECT nome_C, nome_S, SUM(ABS(Valor)) AS TotalAtual
                        FROM viewmovrd
                        WHERE $campoData BETWEEN ? AND ?
                          AND nome_P <> 'NULO'
                          AND classif NOT IN ('3.003.005', '4.008.011')
                          AND Valor < 0
                        GROUP BY nome_C, nome_S
                    ),
                    PeriodoAnterior AS (
                        SELECT nome_C, nome_S, SUM(ABS(Valor)) AS TotalAnterior
                        FROM viewmovrd
                        WHERE $campoData BETWEEN ? AND ?
                          AND nome_P <> 'NULO'
                          AND classif NOT IN ('3.003.005', '4.008.011')
                          AND Valor < 0
                        GROUP BY nome_C, nome_S
                    ),
                    TotaisPeriodos AS (
                        SELECT
                            (SELECT SUM(ABS(Valor)) FROM viewmovrd
                             WHERE $campoData BETWEEN ? AND ?
                               AND nome_P <> 'NULO'
                               AND classif NOT IN ('3.003.005', '4.008.011')
                               AND Valor < 0) AS TotalDespesasAtual,
                            (SELECT SUM(ABS(Valor)) FROM viewmovrd
                             WHERE $campoData BETWEEN ? AND ?
                               AND nome_P <> 'NULO'
                               AND classif NOT IN ('3.003.005', '4.008.011')
                               AND Valor < 0) AS TotalDespesasAnterior
                    )
                    SELECT
                        COALESCE(pa.nome_C, pp.nome_C) AS Categoria,
                        COALESCE(pa.nome_S, pp.nome_S) AS Grupo,
                        COALESCE(pp.TotalAnterior, 0) AS GastoAnterior,
                        COALESCE(pa.TotalAtual, 0) AS GastoAtual,
                        CASE WHEN t.TotalDespesasAnterior > 0
                             THEN (COALESCE(pp.TotalAnterior, 0) / t.TotalDespesasAnterior * 100)
                             ELSE 0 END AS PesoCategoria,
                        CASE WHEN COALESCE(pp.TotalAnterior, 0) > 0
                             THEN ((COALESCE(pa.TotalAtual, 0) - COALESCE(pp.TotalAnterior, 0)) / COALESCE(pp.TotalAnterior, 0) * 100)
                             WHEN COALESCE(pa.TotalAtual, 0) > 0 THEN 100
                             ELSE 0 END AS VariacaoPercent,
                        CASE WHEN t.TotalDespesasAnterior > 0 AND COALESCE(pp.TotalAnterior, 0) > 0
                             THEN (((COALESCE(pa.TotalAtual, 0) - COALESCE(pp.TotalAnterior, 0)) / COALESCE(pp.TotalAnterior, 0) * 100)
                                   * (COALESCE(pp.TotalAnterior, 0) / t.TotalDespesasAnterior * 100) / 100)
                             ELSE 0 END AS ContribuicaoInflacao
                    FROM PeriodoAtual pa
                    LEFT JOIN PeriodoAnterior pp ON pa.nome_C = pp.nome_C AND pa.nome_S = pp.nome_S
                    CROSS JOIN TotaisPeriodos t
                    UNION
                    SELECT
                        COALESCE(pa.nome_C, pp.nome_C) AS Categoria,
                        COALESCE(pa.nome_S, pp.nome_S) AS Grupo,
                        COALESCE(pp.TotalAnterior, 0) AS GastoAnterior,
                        COALESCE(pa.TotalAtual, 0) AS GastoAtual,
                        CASE WHEN t.TotalDespesasAnterior > 0
                             THEN (COALESCE(pp.TotalAnterior, 0) / t.TotalDespesasAnterior * 100)
                             ELSE 0 END AS PesoCategoria,
                        CASE WHEN COALESCE(pp.TotalAnterior, 0) > 0
                             THEN ((COALESCE(pa.TotalAtual, 0) - COALESCE(pp.TotalAnterior, 0)) / COALESCE(pp.TotalAnterior, 0) * 100)
                             WHEN COALESCE(pa.TotalAtual, 0) > 0 THEN 100
                             ELSE 0 END AS VariacaoPercent,
                        CASE WHEN t.TotalDespesasAnterior > 0 AND COALESCE(pp.TotalAnterior, 0) > 0
                             THEN (((COALESCE(pa.TotalAtual, 0) - COALESCE(pp.TotalAnterior, 0)) / COALESCE(pp.TotalAnterior, 0) * 100)
                                   * (COALESCE(pp.TotalAnterior, 0) / t.TotalDespesasAnterior * 100) / 100)
                             ELSE 0 END AS ContribuicaoInflacao
                    FROM PeriodoAnterior pp
                    LEFT JOIN PeriodoAtual pa ON pa.nome_C = pp.nome_C AND pa.nome_S = pp.nome_S
                    CROSS JOIN TotaisPeriodos t
                    WHERE pa.nome_C IS NULL
                )
                ORDER BY ABS(ContribuicaoInflacao) DESC
            """

            val cursor = db.rawQuery(sql, arrayOf(
                dataIniAtualStr, dataFimAtualStr,
                dataIniAnteriorStr, dataFimAnteriorStr,
                dataIniAtualStr, dataFimAtualStr,
                dataIniAnteriorStr, dataFimAnteriorStr
            ))

            val items = mutableListOf<InflacaoItem>()
            var inflacaoTotal = 0.0

            while (cursor.moveToNext()) {
                val categoria = cursor.getString(cursor.getColumnIndexOrThrow("Categoria"))
                val grupo = cursor.getString(cursor.getColumnIndexOrThrow("Grupo"))
                val gastoAnt = cursor.getDouble(cursor.getColumnIndexOrThrow("GastoAnterior"))
                val gastoAtual = cursor.getDouble(cursor.getColumnIndexOrThrow("GastoAtual"))
                val peso = cursor.getDouble(cursor.getColumnIndexOrThrow("PesoCategoria"))
                val variacao = cursor.getDouble(cursor.getColumnIndexOrThrow("VariacaoPercent"))
                val contribuicao = cursor.getDouble(cursor.getColumnIndexOrThrow("ContribuicaoInflacao"))

                inflacaoTotal += contribuicao

                items.add(InflacaoItem(
                    categoria, grupo,
                    decimalFormat.format(gastoAnt),
                    decimalFormat.format(gastoAtual),
                    percentFormat.format(peso),
                    percentFormat.format(variacao),
                    percentFormat.format(contribuicao)
                ))
            }

            // Adicionar linha de resumo
            items.add(InflacaoItem("", "", "", "", "", "INFLAÇÃO:", percentFormat.format(inflacaoTotal) + "%"))

            inflacaoItems = items
            binding.recyclerViewInflacao.adapter = InflacaoAdapter(items)

            cursor.close()
            db.close()

            val message = "Sua inflação pessoal no período foi de: ${percentFormat.format(inflacaoTotal)}%\n\n" +
                    "Período atual: $dataIniStr a $dataFimStr\n" +
                    "Período anterior (ano passado): $dataIniAnteriorDisplay a $dataFimAnteriorDisplay"
            
            AlertDialog.Builder(this)
                .setTitle("Inflação Pessoal")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()

        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao realizar pesquisa: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("InflacaoConsulta", "Erro na pesquisa: ${e.message}", e)
        }
    }

    private fun imprimir() {
        if (inflacaoItems.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para imprimir!", Toast.LENGTH_SHORT).show()
            return
        }
        val printManager = getSystemService(PRINT_SERVICE) as PrintManager
        val jobName = "Inflacao_${SimpleDateFormat("dd_MM_yyyy_HH_mm", Locale.getDefault()).format(Date())}"
        printManager.print(
            jobName,
            InflacaoPrintAdapter(inflacaoItems, binding.editDataIni.text.toString(), binding.editDataFim.text.toString()),
            PrintAttributes.Builder().build()
        )
    }

    private fun exportar() {
        if (inflacaoItems.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para exportar!", Toast.LENGTH_SHORT).show()
            return
        }
        val fileName = "Inflação_${SimpleDateFormat("dd_MM_yyyy_HH_mm", Locale.getDefault()).format(Date())}.xlsx"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Inflação")
            
            // Header
            val header = sheet.createRow(0)
            val cols = listOf("CATEGORIA", "GRUPO", "GASTO ANTERIOR", "GASTO ATUAL", "PESO (%)", "VARIAÇÃO (%)", "CONTRIBUIÇÃO (%)")
            cols.forEachIndexed { i, s -> header.createCell(i).setCellValue(s) }

            inflacaoItems.forEachIndexed { index, item ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(item.categoria)
                row.createCell(1).setCellValue(item.grupo)
                row.createCell(2).setCellValue(item.gastoAnterior)
                row.createCell(3).setCellValue(item.gastoAtual)
                row.createCell(4).setCellValue(item.peso)
                row.createCell(5).setCellValue(item.variacao)
                row.createCell(6).setCellValue(item.contribuicao)
            }

            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()

            Toast.makeText(this, "Arquivo Excel gerado em ${file.absolutePath}!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao exportar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun gerarGrafico() {
        if (inflacaoItems.isEmpty() || inflacaoItems.size <= 1) { // Só a linha de resumo
            Toast.makeText(this, "Nenhum dado para gerar o gráfico!", Toast.LENGTH_SHORT).show()
            return
        }

        // Pegar top 15 (excluindo a última linha que é o resumo)
        val dataForChart = inflacaoItems.subList(0, inflacaoItems.size - 1)
            .sortedByDescending { abs(it.contribuicao.replace(",", ".").replace("%", "").toDoubleOrNull() ?: 0.0) }
            .take(15)

        val entriesAnterior = mutableListOf<BarEntry>()
        val entriesAtual = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        dataForChart.forEachIndexed { index, item ->
            val gastoAnt = item.gastoAnterior.replace(".", "").replace(",", ".").toFloatOrNull() ?: 0f
            val gastoAtual = item.gastoAtual.replace(".", "").replace(",", ".").toFloatOrNull() ?: 0f
            entriesAnterior.add(BarEntry(index.toFloat(), gastoAnt))
            entriesAtual.add(BarEntry(index.toFloat(), gastoAtual))
            labels.add(if (item.categoria.length > 20) item.categoria.substring(0, 17) + "..." else item.categoria)
        }

        val context = this
        val barChart = BarChart(this).apply {
            description.isEnabled = false
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                labelRotationAngle = -45f
                textColor = Color.WHITE // Adicionado para contraste
            }

            axisLeft.apply {
                textColor = Color.WHITE // Adicionado para contraste
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "R$ ${decimalFormat.format(value.toDouble())}"
                    }
                }
            }
            axisRight.isEnabled = false
            
            legend.apply {
                textColor = Color.WHITE // Adicionado para contraste
            }
        }

        val set1 = BarDataSet(entriesAnterior, "Anterior").apply { 
            color = Color.parseColor("#4682B4") 
            valueTextColor = Color.WHITE // Adicionado para contraste
        }
        val set2 = BarDataSet(entriesAtual, "Atual").apply { 
            color = Color.parseColor("#DC143C") 
            valueTextColor = Color.WHITE // Adicionado para contraste
        }

        val data = BarData(set1, set2)
        val groupSpace = 0.06f
        val barSpace = 0.02f
        val barWidth = 0.45f

        data.barWidth = barWidth
        barChart.data = data
        barChart.groupBars(0f, groupSpace, barSpace)
        barChart.invalidate()

        val frameLayout = FrameLayout(this)
        frameLayout.addView(barChart)
        barChart.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            (400 * resources.displayMetrics.density).toInt()
        ).apply {
            setMargins(16, 16, 16, 16)
        }

        AlertDialog.Builder(this)
            .setTitle("Comparativo de Gastos - Top 15")
            .setView(frameLayout)
            .setPositiveButton("Fechar", null)
            .show()
    }
}

data class InflacaoItem(
    val categoria: String,
    val grupo: String,
    val gastoAnterior: String,
    val gastoAtual: String,
    val peso: String,
    val variacao: String,
    val contribuicao: String
)

class InflacaoAdapter(private val items: List<InflacaoItem>) :
    RecyclerView.Adapter<InflacaoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemInflacaoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInflacaoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            textCategoria.text = item.categoria
            textGrupo.text = item.grupo
            textGastoAnterior.text = item.gastoAnterior
            textGastoAtual.text = item.gastoAtual
            textPeso.text = "Peso: ${item.peso}%"
            textVariacao.text = "Var: ${item.variacao}%"
            textContribuicao.text = "Contrib: ${item.contribuicao}%"

            // Destacar linha de resumo
            if (item.categoria.isEmpty() && item.grupo.isEmpty()) {
                root.setBackgroundColor(Color.parseColor("#F5F5F5"))
                textVariacao.setTypeface(null, android.graphics.Typeface.BOLD)
                textContribuicao.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                root.setBackgroundColor(Color.TRANSPARENT)
                textVariacao.setTypeface(null, android.graphics.Typeface.NORMAL)
                textContribuicao.setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}

class InflacaoPrintAdapter(private val items: List<InflacaoItem>, private val dataIni: String, private val dataFim: String) : PrintDocumentAdapter() {
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

        val pages = (items.size / 15) + 1
        callback?.onLayoutFinished(
            PrintDocumentInfo.Builder("Inflação.pdf")
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
            pdfDocument.defaultPageSize = PageSize.A4.rotate()
            val document = Document(pdfDocument)

            document.add(Paragraph("Inflação Pessoal - Período: $dataIni a $dataFim").setBold().setFontSize(14f))

            val columnWidths = floatArrayOf(30f, 20f, 10f, 10f, 10f, 10f, 10f)
            val table = Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth()
            
            table.addHeaderCell(Cell().add(Paragraph("CATEGORIA").setFontSize(10f).setBold()))
            table.addHeaderCell(Cell().add(Paragraph("GRUPO").setFontSize(10f).setBold()))
            table.addHeaderCell(Cell().add(Paragraph("G.ANT").setFontSize(10f).setBold()))
            table.addHeaderCell(Cell().add(Paragraph("G.ATUAL").setFontSize(10f).setBold()))
            table.addHeaderCell(Cell().add(Paragraph("PESO %").setFontSize(10f).setBold()))
            table.addHeaderCell(Cell().add(Paragraph("VAR %").setFontSize(10f).setBold()))
            table.addHeaderCell(Cell().add(Paragraph("CONTRIB %").setFontSize(10f).setBold()))

            items.forEach { item ->
                table.addCell(Cell().add(Paragraph(item.categoria).setFontSize(9f)))
                table.addCell(Cell().add(Paragraph(item.grupo).setFontSize(9f)))
                table.addCell(Cell().add(Paragraph(item.gastoAnterior).setFontSize(9f)))
                table.addCell(Cell().add(Paragraph(item.gastoAtual).setFontSize(9f)))
                table.addCell(Paragraph(item.peso).setFontSize(9f))
                table.addCell(Paragraph(item.variacao).setFontSize(9f))
                table.addCell(Paragraph(item.contribuicao).setFontSize(9f))
            }

            document.add(table)
            document.close()
            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback?.onWriteFailed(e.message)
        }
    }
}
