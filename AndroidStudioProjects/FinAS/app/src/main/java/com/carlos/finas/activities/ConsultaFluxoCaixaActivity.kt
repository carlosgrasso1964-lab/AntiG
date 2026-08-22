package com.carlos.finas.activities

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.adapters.FluxoCaixa
import com.carlos.finas.adapters.FluxoCaixaAdapter
import com.carlos.finas.databinding.ActivityConsultaFluxoCaixaBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.Utils
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
import kotlin.text.iterator
import androidx.core.content.ContextCompat
import java.util.*


class ConsultaFluxoCaixaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsultaFluxoCaixaBinding
    private lateinit var dbHelper: DatabaseHelper
    private val decimalFormat =
        DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())
    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val inputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val createDocument =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
            if (uri != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    generateAndSavePdf(uri)
                }
            } else {
                Toast.makeText(
                    this,
                    "Nenhum local selecionado para salvar o PDF!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Removido setRequestedOrientation para suportar orientação horizontal
        Utils.init(this)
        binding = ActivityConsultaFluxoCaixaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DatabaseHelper(this)

        binding.recyclerViewFluxoCaixa.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFluxoCaixa.adapter = FluxoCaixaAdapter.Companion.create(emptyList()) { fluxo ->
            Toast.makeText(this, "Clicou em ${fluxo.descr}", Toast.LENGTH_SHORT).show()
        }

        // Adicionar DateMaskWatcher para formatação automática de datas
        binding.editDataIni.addTextChangedListener(DateMaskWatcher())
        binding.editDataFim.addTextChangedListener(DateMaskWatcher())

        binding.btnPesquisar.setOnClickListener {
            pesquisarFluxoCaixa()
        }

        binding.btnLimpar.setOnClickListener {
            binding.recyclerViewFluxoCaixa.adapter =
                FluxoCaixaAdapter.Companion.create(emptyList()) { fluxo ->
                    Toast.makeText(this, "Clicou em ${fluxo.descr}", Toast.LENGTH_SHORT).show()
                }
            binding.editDataIni.text.clear()
            binding.editDataFim.text.clear()
            binding.txtSaldoAnterior.text = "R$ 0,00"
        }

        binding.btnImprimir.setOnClickListener {
            Log.d("ConsultaFluxoCaixaActivity", "Botão Imprimir clicado")
            val adapter = binding.recyclerViewFluxoCaixa.adapter as? FluxoCaixaAdapter
            if (adapter == null || adapter.itemCount == 0) {
                Toast.makeText(this, "Nenhum dado para gerar o PDF!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createDocument.launch("Fluxo_Caixa_${dateFormat.format(Date()).replace(":", "-")}.pdf")
        }

        binding.btnGerarGrafico.setOnClickListener {
            gerarGrafico()
        }
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
    private fun pesquisarFluxoCaixa() {
        val dataIniStr = binding.editDataIni.text.toString().trim()
        val dataFimStr = binding.editDataFim.text.toString().trim()

        if (dataIniStr.length < 10 || !dataIniStr.matches("\\d{2}/\\d{2}/\\d{4}".toRegex())) {
            Toast.makeText(this@ConsultaFluxoCaixaActivity, "Preencha a data inicial corretamente (dd/MM/yyyy)", Toast.LENGTH_SHORT).show()
            return
        }
        if (dataFimStr.length < 10 || !dataFimStr.matches("\\d{2}/\\d{2}/\\d{4}".toRegex())) {
            Toast.makeText(this@ConsultaFluxoCaixaActivity, "Preencha a data final corretamente (dd/MM/yyyy)", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val saldoAnterior = withContext(Dispatchers.IO) { pegaSaldoAnterior(dataIniStr) }
                binding.txtSaldoAnterior.text = decimalFormat.format(saldoAnterior)

                // ----------------- Lógica de Realizado vs Projeção -----------------
                val formatoEntrada = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                val dataInicio = formatoEntrada.parse(dataIniStr)!!

                val hoje = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val isRealizado = dataInicio.before(hoje)  // < hoje → Realizado

                // Atualiza o título/tipo de consulta (opcional - só executa se o TextView existir)
                binding.txtTipoConsulta?.let { txt ->
                    txt.text = if (isRealizado) "FLUXO DE CAIXA REALIZADO" else "PROJEÇÃO DE CAIXA"
                    txt.setTextColor(
                        if (isRealizado)
                            ContextCompat.getColor(this@ConsultaFluxoCaixaActivity, android.R.color.holo_blue_dark)
                        else
                            ContextCompat.getColor(this@ConsultaFluxoCaixaActivity, android.R.color.darker_gray)
                    )
                }

                val fluxos = withContext(Dispatchers.IO) {
                    val db = dbHelper.readableDatabase

                    val sql = if (isRealizado) {
                        """
                        SELECT * FROM tbmovimento 
                        WHERE dtApr BETWEEN ? AND ? 
                        AND recurso IN ('0079', '0022', '0080') 
                        AND classif NOT IN ('9.001.000', '9.001.001', '9.001.002', '9.001.003') 
                        ORDER BY dtApr
                    """.trimIndent()
                    } else {
                        """
                        SELECT * FROM tbmovimento 
                        WHERE dtVcto BETWEEN ? AND ? 
                        AND vrecurso IN ('1.001.001', '1.001.002', '1.002.001', '1.003.001', '2.001.002', '2.001.003', '2.001.004') 
                        AND statusMov = '' 
                        AND recurso <> '0080' 
                        ORDER BY dtVcto
                    """.trimIndent()
                    }

                    val dataInicioBanco = formatarDataInputParaBanco(dataIniStr)
                    val dataFimBanco = formatarDataInputParaBanco(dataFimStr)

                    val cursor = db.rawQuery(sql, arrayOf(dataInicioBanco, dataFimBanco))

                    val lista = mutableListOf<FluxoCaixa>()
                    var saldoAcumulado = saldoAnterior
                    var dataAnterior = ""
                    var indiceUltima = -1
                    var saldoDia = saldoAnterior

                    cursor.use {
                        while (it.moveToNext()) {
                            val dtPrincipal = if (isRealizado) {
                                it.getString(it.getColumnIndexOrThrow("dtApr"))
                            } else {
                                it.getString(it.getColumnIndexOrThrow("dtVcto"))
                            }

                            val lancData = formatarDataBancoParaSaida(dtPrincipal ?: "")
                            val diaSemana = getDiaSemana(dtPrincipal ?: "")

                            val valor = it.getDouble(it.getColumnIndexOrThrow("Valor"))
                            saldoAcumulado += valor

                            // Atualiza saldo da linha anterior se mudou o dia
                            if (lancData != dataAnterior && indiceUltima != -1) {
                                lista[indiceUltima] = lista[indiceUltima].copy(saldo = saldoDia)
                            }

                            saldoDia = saldoAcumulado

                            lista.add(
                                FluxoCaixa(
                                    idMov = it.getString(it.getColumnIndexOrThrow("idMov")),
                                    recurso = it.getString(it.getColumnIndexOrThrow("recurso")),
                                    clifor = it.getString(it.getColumnIndexOrThrow("clifor")),
                                    dtEmi = formatarDataBancoParaSaida(
                                        it.getString(it.getColumnIndexOrThrow("dtEmi")) ?: ""
                                    ),
                                    dtVcto = lancData,
                                    diaSemana = diaSemana,
                                    documento = it.getString(it.getColumnIndexOrThrow("documento")),
                                    descr = it.getString(it.getColumnIndexOrThrow("Descr")),
                                    valor = valor,
                                    prev = it.getString(it.getColumnIndexOrThrow("Prev")),
                                    saldo = null  // só preenche na última do dia
                                )
                            )

                            indiceUltima = lista.size - 1
                            dataAnterior = lancData
                        }

                        // Último dia
                        if (indiceUltima != -1) {
                            lista[indiceUltima] = lista[indiceUltima].copy(saldo = saldoDia)
                        }
                    }

                    db.close()
                    lista
                }

                // Atualiza o RecyclerView
                val adapter = FluxoCaixaAdapter.create(fluxos) { fluxo ->
                    Toast.makeText(
                        this@ConsultaFluxoCaixaActivity,  // ← sempre use o nome da Activity aqui
                        "Clicou em ${fluxo.descr}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.recyclerViewFluxoCaixa.adapter = adapter

            } catch (e: Exception) {
                // Erro principal → mostrar Toast na Main Thread
                Toast.makeText(
                    this@ConsultaFluxoCaixaActivity,
                    "Erro ao consultar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                Log.e("ConsultaFluxoCaixa", "Erro completo: ", e)
            }
        }
    }

    @SuppressLint("LongLogTag")
    private fun pegaSaldoAnterior(dataIni: String): Double {
        return try {
            val db = dbHelper.readableDatabase
            val query = """
                SELECT SUM(Valor) FROM tbmovimento 
                WHERE dtlancto <= ? 
                AND vrecurso in ('1.001.001','1.001.002')
                AND recurso NOT IN ('0079', '0022')
            """.trimIndent()
            //AND vrecurso in ('1.001.001','1.001.002', '1.003.001')
            val cursor = db.rawQuery(query, arrayOf(formatarDataInputParaBanco(dataIni)))
            var saldo = 0.0
            cursor.use {
                if (it.moveToFirst()) {
                    saldo = it.getDouble(0)
                }
            }
            db.close()
            saldo
        } catch (e: Exception) {
            Log.e("ConsultaFluxoCaixaActivity", "Erro em pegaSaldoAnterior: ${e.message}", e)
            0.0
        }
    }

    private fun formatarDataInputParaBanco(data: String): String {
        return try {
            val dataUtil = inputDateFormat.parse(data) ?: return ""
            dbDateFormat.format(dataUtil)
        } catch (e: Exception) {
            ""
        }
    }

    private fun formatarDataBancoParaSaida(data: String?): String {
        return data?.let {
            try {
                val dataUtil = dbDateFormat.parse(it) ?: return "Data inválida"
                inputDateFormat.format(dataUtil)
            } catch (e: Exception) {
                "Data inválida"
            }
        } ?: ""
    }

    private fun getDiaSemana(data: String?): String {
        return data?.let {
            try {
                val dataUtil = dbDateFormat.parse(it) ?: return "---"
                val cal = Calendar.getInstance()
                cal.time = dataUtil
                val dayFormat = SimpleDateFormat("EEE", Locale("pt", "BR"))
                dayFormat.format(cal.time).replace(".", "")
            } catch (e: Exception) {
                "---"
            }
        } ?: "---"
    }

    private fun generateAndSavePdf(uri: Uri) {
        val adapter = binding.recyclerViewFluxoCaixa.adapter as? FluxoCaixaAdapter
        val fluxos = adapter?.getFluxos() ?: emptyList()

        if (fluxos.isEmpty()) {
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
                val saldoAnterior = binding.txtSaldoAnterior.text.toString()
                canvas.drawText(
                    "Previsão de Caixa - $currentDate - Saldo Anterior: $saldoAnterior",
                    marginLeft,
                    40f,
                    paint
                )

                val columnWidths = floatArrayOf(
                    40f, 50f, 45f, 70f, 70f, 45f, 100f, 200f, 80f, 30f, 80f
                )
                val headers = arrayOf(
                    "ID",
                    "Recurso",
                    "Cli/For",
                    "Emissão",
                    "Vencimento",
                    "Dia",
                    "Documento",
                    "Descrição",
                    "Valor",
                    "Prev",
                    "Saldo"
                )
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

                fluxos.forEachIndexed { index, fluxo ->
                    if (y + rowHeight > pageHeight - 40f) {
                        canvas.drawText("Página $pageNumber", marginLeft, pageHeight - 20f, paint)
                        pdfDocument.finishPage(page)
                        pageNumber++
                        page = pdfDocument.startPage(
                            PdfDocument.PageInfo.Builder(842, 595, pageNumber).create()
                        )
                        canvas = page.canvas
                        y = 60f
                        canvas.drawText(
                            "Previsão de Caixa - $currentDate - Saldo Anterior: $saldoAnterior",
                            marginLeft,
                            40f,
                            paint
                        )
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
                    canvas.drawText(fluxo.idMov ?: "-", x, y, paint)
                    x += columnWidths[0]
                    canvas.drawText(fluxo.recurso ?: "-", x, y, paint)
                    x += columnWidths[1]
                    canvas.drawText(fluxo.clifor ?: "-", x, y, paint)
                    x += columnWidths[2]
                    canvas.drawText(fluxo.dtEmi ?: "-", x, y, paint)
                    x += columnWidths[3]
                    canvas.drawText(fluxo.dtVcto ?: "-", x, y, paint)
                    x += columnWidths[4]
                    canvas.drawText(fluxo.diaSemana ?: "-", x, y, paint)
                    x += columnWidths[5]
                    canvas.drawText(fluxo.documento ?: "-", x, y, paint)
                    x += columnWidths[6]
                    val descricao = fluxo.descr?.take(33) ?: "-"
                    canvas.drawText(descricao, x, y, paint)
                    x += columnWidths[7]
                    canvas.drawText(decimalFormat.format(fluxo.valor), x, y, paint)
                    x += columnWidths[8]
                    canvas.drawText(fluxo.prev ?: "-", x, y, paint)
                    x += columnWidths[9]
                    canvas.drawText(fluxo.saldo?.let { decimalFormat.format(it) } ?: "", x, y, paint)
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
                    Toast.makeText(
                        this@ConsultaFluxoCaixaActivity,
                        "PDF gerado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ConsultaFluxoCaixaActivity,
                        "Erro ao gerar PDF: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    @SuppressLint("LongLogTag")
    fun gerarGrafico() {
        val adapter = binding.recyclerViewFluxoCaixa.adapter as? FluxoCaixaAdapter
        val fluxos = adapter?.getFluxos() ?: emptyList()

        Log.d("ConsultaFluxoCaixaActivity", "Fluxos: ${fluxos.size}, Dados: $fluxos")

        if (fluxos.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para gerar o gráfico!", Toast.LENGTH_SHORT).show()
            return
        }

        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        fluxos.forEachIndexed { index, fluxo ->
            fluxo.saldo?.let {
                entries.add(Entry(index.toFloat(), it.toFloat()))
                labels.add(fluxo.dtVcto?.substring(0, 5) ?: "-") // Usar apenas dd/MM para rótulos
            }
        }

        Log.d("ConsultaFluxoCaixaActivity", "Entries: $entries, Labels: $labels")

        if (entries.isEmpty()) {
            Toast.makeText(this, "Nenhum saldo válido para o gráfico!", Toast.LENGTH_SHORT).show()
            return
        }

        val dataSet = LineDataSet(entries, "Saldo").apply {
            color = Color.BLUE
            setCircleColor(Color.BLUE)
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 10f
            valueTextColor = Color.WHITE // Alterar cor dos valores do gráfico para branco
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return DecimalFormat("#,##0.00").format(value.toDouble())
                }
            }
        }

        val lineData = LineData(dataSet)
        val chart = LineChart(this).apply {
            data = lineData
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                labelRotationAngle = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 0f else 45f
                textSize = 10f
                textColor = Color.WHITE // Alterar cor dos rótulos do eixo X para branco
                setDrawGridLines(false)
                labelCount = labels.size.coerceAtMost(10) // Limitar número de rótulos para evitar sobreposição
                granularity = 1f
            }
            axisLeft.apply {
                setDrawGridLines(false)
                textColor = Color.WHITE // Alterar cor dos rótulos do eixo Y para branco
            }
            axisRight.isEnabled = false
            description.text = "Evolução do Saldo no Período"
            description.textSize = 12f
            description.textColor = Color.WHITE // Alterar cor da descrição para branco
            setTouchEnabled(true)
            setPinchZoom(true)
            animateX(1000)
            // Ajustar margens internas do gráfico
            setExtraOffsets(10f, 10f, 10f, 20f)
            setBackgroundColor(Color.DKGRAY) // Fundo escuro para contraste com texto branco
        }

        // Calcular dimensões do gráfico com base no tamanho da tela
        val displayMetrics = resources.displayMetrics
        val chartWidth = (displayMetrics.widthPixels * 0.9f).toInt() // 90% da largura da tela
        val chartHeight = (displayMetrics.heightPixels * 0.8f).toInt() // 80% da altura da tela

        val frameLayout = FrameLayout(this)
        frameLayout.addView(chart)
        chart.layoutParams = FrameLayout.LayoutParams(
            chartWidth,
            chartHeight
        ).apply {
            gravity = Gravity.CENTER
            setMargins(16, 16, 16, 16)
        }

        // Criar estilo personalizado para o título do AlertDialog
        val titleTextView = android.widget.TextView(this).apply {
            text = "Gráfico de Saldo"
            setTextColor(Color.WHITE)
            textSize = 20f
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER
        }

        val dialog = AlertDialog.Builder(this)
            .setCustomTitle(titleTextView) // Usar TextView personalizado para o título
            .setView(frameLayout)
            .setPositiveButton("Fechar") { _, _ -> }
            .create()

        // Configurar o dialog para ocupar a maior parte da tela
        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        chart.invalidate()
    }
}