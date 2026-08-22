package com.carlos.finas.activities

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.Parcel
import android.os.ParcelFileDescriptor
import android.os.Parcelable
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.R
import com.carlos.finas.PlanoDiretorRecord
import com.carlos.finas.databinding.ActivityConsultaRcBinding
import com.carlos.finas.databinding.ItemConsultaRcBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.os.Build
import android.widget.ScrollView
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import java.io.FileInputStream
import java.text.NumberFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

private val resultadosMensais = DoubleArray(12) { 0.0 }
private val monthlyFinalBalances = DoubleArray(12) { 0.0 }  // Saldo Final por mês


data class ConsultaRCItem(
    val contas: String,
    val jan: String, val fev: String, val mar: String, val abr: String,
    val mai: String, val jun: String, val jul: String, val ago: String,
    val set: String, val out: String, val nov: String, val dez: String,
    val total: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(contas)
        parcel.writeString(jan); parcel.writeString(fev); parcel.writeString(mar)
        parcel.writeString(abr); parcel.writeString(mai); parcel.writeString(jun)
        parcel.writeString(jul); parcel.writeString(ago); parcel.writeString(set)
        parcel.writeString(out); parcel.writeString(nov); parcel.writeString(dez)
        parcel.writeString(total)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ConsultaRCItem> {
        override fun createFromParcel(parcel: Parcel): ConsultaRCItem = ConsultaRCItem(parcel)
        override fun newArray(size: Int): Array<ConsultaRCItem?> = arrayOfNulls(size)
        fun emptyLine(): ConsultaRCItem {
            return ConsultaRCItem("", "", "", "", "", "", "", "", "", "", "", "", "", "")
        }
    }
}

class ConsultaRCAdapter(
    var items: List<ConsultaRCItem>, // Removi 'private' para tornar pública
    private var months: List<String>,
    private var startMonth: Int,
    private var endMonth: Int
) : RecyclerView.Adapter<ConsultaRCAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemConsultaRcBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemConsultaRcBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.textViewContas.text = item.contas
        holder.binding.textViewTotal.text = item.total

        holder.binding.linearLayoutValues.removeAllViews()
        val monthValues = listOf(
            item.jan, item.fev, item.mar, item.abr, item.mai, item.jun,
            item.jul, item.ago, item.set, item.out, item.nov, item.dez
        )

        for (i in startMonth..endMonth) {
            val tv = TextView(holder.itemView.context).apply {
                text = monthValues[i]
                gravity = Gravity.CENTER
                textSize = 8f
                setPadding(8, 8, 8, 8)

                // --- ALTERAÇÃO AQUI: Largura fixa de 100dp ---
                val larguraEmPixels = (100 * resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(larguraEmPixels, LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            holder.binding.linearLayoutValues.addView(tv)
        }

        // Negrito para linhas de resumo
        if (item.contas in listOf(
                "Saldo Anterior", "Receitas", "Despesas", "Resultado",
                "Emprést.Obt.C.Prazo-Recebidos", "Emprést.Obt.C.Prazo-Pagos",
                "Saldo Empréstimos C.Prazo", "Resultado Período"
            )
        ) {
            holder.binding.textViewContas.setTypeface(null, Typeface.BOLD)
            holder.binding.textViewTotal.setTypeface(null, Typeface.BOLD)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(
        newItems: List<ConsultaRCItem>,
        newMonths: List<String>,
        newStartMonth: Int,
        newEndMonth: Int
    ) {
        items = newItems
        months = newMonths
        startMonth = newStartMonth
        endMonth = newEndMonth
        notifyDataSetChanged()
    }
}

class ConsultaRCPrintAdapter(
    private val context: Context,
    private val items: List<ConsultaRCItem>
) : PrintDocumentAdapter() {

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

        val info = PrintDocumentInfo.Builder("ConsultaRC.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()

        callback?.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        try {
            val writer = PdfWriter(FileOutputStream(destination?.fileDescriptor))
            val pdfDocument = PdfDocument(writer)
            pdfDocument.defaultPageSize = PageSize.A4.rotate()
            val document = Document(pdfDocument)
            document.setMargins(20f, 20f, 20f, 20f)

            document.add(
                Paragraph("Relatório de Referência Cruzada")
                    .setFontSize(12f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )

            val columnWidths =
                floatArrayOf(100f, 50f, 50f, 50f, 50f, 50f, 50f, 50f, 50f, 50f, 50f, 50f, 50f, 50f)
            val table = Table(UnitValue.createPointArray(columnWidths))
            table.setFontSize(8f)

            listOf(
                "Contas",
                "Jan",
                "Fev",
                "Mar",
                "Abr",
                "Mai",
                "Jun",
                "Jul",
                "Ago",
                "Set",
                "Out",
                "Nov",
                "Dez",
                "Total"
            ).forEach { header ->
                table.addHeaderCell(header)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
            }

            items.forEach { item ->
                table.addCell(item.contas)
                table.addCell(item.jan)
                table.addCell(item.fev)
                table.addCell(item.mar)
                table.addCell(item.abr)
                table.addCell(item.mai)
                table.addCell(item.jun)
                table.addCell(item.jul)
                table.addCell(item.ago)
                table.addCell(item.set)
                table.addCell(item.out)
                table.addCell(item.nov)
                table.addCell(item.dez)
                table.addCell(item.total)
            }

            document.add(table)
            document.close()

            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback?.onWriteFailed(e.message)
            Log.e("ConsultaRCPrintAdapter", "Erro ao gerar PDF: ${e.message}", e)
        }
    }
}

class ConsultaRCActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsultaRcBinding
    private val dateFormatInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateFormatOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val receitasGrafico = DoubleArray(12) { 0.0 }
    private val despesasGrafico = DoubleArray(12) { 0.0 }
    private val saldosFinaisGrafico = DoubleArray(12) { 0.0 }
    private val saldoAcumuladoEmprestimoGrafico = DoubleArray(12) { 0.0 }

    private var mesInicioAtual = 0
    private var mesFimAtual = 11


    // CORREÇÃO: Use java.text.DecimalFormat SEM cast para android.icu
    private val decimalFormat = java.text.DecimalFormat("#,##0.00").apply {
        val symbols = java.text.DecimalFormatSymbols(Locale("pt", "BR"))
        symbols.groupingSeparator = '.'
        symbols.decimalSeparator = ','
        this.decimalFormatSymbols = symbols
    }

    private val items = mutableListOf<ConsultaRCItem>()
    private var months = listOf<String>()
    private var startMonth = 0
    private var endMonth = 11

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsultaRcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewConsultaRC.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewConsultaRC.adapter =
            ConsultaRCAdapter(items, months, startMonth, endMonth)

        binding.editTextDataInicial.addTextChangedListener(DateMaskWatcher())
        binding.editTextDataFinal.addTextChangedListener(DateMaskWatcher())

        binding.btnPesquisar.setOnClickListener { pesquisar() }
        binding.btnLimpar.setOnClickListener { limpar() }
        binding.btnImprimir.setOnClickListener { imprimir() }
        binding.btnExportar.setOnClickListener { exportar() }
        binding.btnGrafico.setOnClickListener { mostrarGrafico() }
        binding.btnSalvarPrev.setOnClickListener { salvarPrevistos() }
        binding.btnAuditoria.setOnClickListener { abrirAuditoria() }

        binding.radioButtonPrev.isChecked = true

//        binding.recyclerViewConsultaRC.addOnScrollListener(object :
//            RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                binding.headerScrollView.scrollBy(dx, 0)
//            }
//        })
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

    private fun pesquisar() {
        val dataIni = binding.editTextDataInicial.text.toString().trim()
        val dataFim = binding.editTextDataFinal.text.toString().trim()

        if (dataIni.length < 10 || dataFim.length < 10) {
            Toast.makeText(this, "Preencha as datas corretamente.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val db = DatabaseHelper(this@ConsultaRCActivity).readableDatabase

            try {
                val dataIniParsed = dateFormatInput.parse(dataIni)!!
                val dataFimParsed = dateFormatInput.parse(dataFim)!!

                val dataIniSQL = dateFormatOutput.format(dataIniParsed)
                val dataFimSQL = dateFormatOutput.format(dataFimParsed)

                val mesInicio =
                    Calendar.getInstance().apply { time = dataIniParsed }.get(Calendar.MONTH)
                val mesFim =
                    Calendar.getInstance().apply { time = dataFimParsed }.get(Calendar.MONTH)

                // === LEITURA DO TRANSPORTE ===
                val prefs = getSharedPreferences("rc_prefs", Context.MODE_PRIVATE)
                val saldoTransportado = prefs.getFloat("saldo_transportado", 0f).toDouble()
                val saldoEmprestimoTransportado =
                    prefs.getFloat("saldo_emprestimo_transportado", 0f).toDouble()


                // === CÁLCULO DINÂMICO DO SALDO ANTERIOR ===
                val hoje = Date()
                val calInicio = Calendar.getInstance().apply { time = dataIniParsed }
                val anoInicio = calInicio.get(Calendar.YEAR)
                val anoHoje = Calendar.getInstance().apply { time = hoje }.get(Calendar.YEAR)

                // Prepara a data do dia anterior para as consultas
                val calAnt = Calendar.getInstance().apply {
                    time = dataIniParsed
                    add(Calendar.DAY_OF_MONTH, -1)
                }
                val dataAntSQL = dateFormatOutput.format(calAnt.time)
                val dataHojeSQL = dateFormatOutput.format(hoje)

                // Variáveis para armazenar o que foi calculado
                var saldoCaixaBancoAnterior = 0.0
                var saldoEmprestimoAnterior = 0.0

                if (anoInicio > anoHoje) {
                    // === CASO 1: CONSULTANDO ANO FUTURO (Ex: 2026) ===

                    // 1. Saldo REAL (efetivado) até HOJE - SEM EMPRÉSTIMO
                    val sqlRealHoje = """
                    SELECT COALESCE(SUM(Valor), 0) FROM tbmovimento
                    WHERE dtlancto <= ? 
                      AND vrecurso IN ('1.001.001', '1.001.002', '1.003.001', '2.001.004')
                      AND recurso NOT IN ('0079', '0022', '0080')  -- RECURSO 0080 EXCLUÍDO AQUI
                      AND dtApr IS NOT NULL AND TRIM(COALESCE(dtApr,'')) <> ''
                """.trimIndent()

                    // 2. PROJEÇÕES (abertas) até 31/12/2025 - SEM EMPRÉSTIMO
                    val dataFimAnoCorrente = "$anoHoje-12-31"
                    val sqlProj = """
                    SELECT COALESCE(SUM(Valor), 0) FROM tbmovimento
                    WHERE vrecurso IN ('1.002.001', '2.001.002', '2.001.003')
                      AND recurso NOT IN ('0080') -- GARANTINDO QUE NÃO PEGA EMPRÉSTIMO NAS PROJEÇÕES
                      AND dtVcto BETWEEN ? AND ? 
                      AND statusMov = ''
                """.trimIndent()

                    // 3. Saldo de EMPRÉSTIMO (0080) acumulado até o fim do ano corrente
                    val sqlEmp =
                        "SELECT COALESCE(SUM(Valor), 0) FROM tbmovimento WHERE recurso = '0080' AND dtlancto <= ?"

                    // Execução para Saldo Limpo (Soma o Real + Projeções)
                    db.rawQuery(sqlRealHoje, arrayOf(dataHojeSQL)).use { c ->
                        if (c.moveToFirst()) saldoCaixaBancoAnterior = c.getDouble(0)
                    }

                    db.rawQuery(sqlProj, arrayOf(dataHojeSQL, dataFimAnoCorrente)).use { c ->
                        if (c.moveToFirst()) saldoCaixaBancoAnterior += c.getDouble(0)
                    }

                    // Execução para Saldo de Empréstimo (Variável separada)
                    db.rawQuery(sqlEmp, arrayOf(dataFimAnoCorrente)).use { c ->
                        if (c.moveToFirst()) {
                            //saldoEmprestimoAnterior = c.getDouble(0)
                            saldoEmprestimoAnterior = c.getDouble(0) * -1
                        }
                    }

                    // SE por acaso o saldoCaixaBancoAnterior ainda contiver o empréstimo
                    // devido a algum lançamento cruzado, você pode forçar a subtração aqui:
                    saldoCaixaBancoAnterior -= saldoEmprestimoAnterior


                } else {
                    // === CASO 2: ANO ATUAL OU PASSADO ===
                    val periodoNoPassado = calAnt.time.before(hoje)

                    if (periodoNoPassado) {
                        // PERÍODO JÁ PASSOU: Segue exatamente o seu Java (apenas dtApr preenchido)
                        val sqlCaixa = """
                    SELECT COALESCE(SUM(Valor), 0) FROM tbmovimento
                    WHERE dtlancto <= ? 
                      AND vrecurso IN ('1.001.001', '1.001.002', '1.003.001', '2.001.004')
                      AND recurso NOT IN ('0079', '0022', '0080')
                      AND dtApr IS NOT NULL AND TRIM(COALESCE(dtApr,'')) <> ''
                """.trimIndent()

                        val sqlEmp =
                            "SELECT COALESCE(SUM(Valor), 0) FROM tbmovimento WHERE recurso = '0080' AND dtlancto <= ?"

                        db.rawQuery(sqlCaixa, arrayOf(dataAntSQL)).use { c ->
                            if (c.moveToFirst()) saldoCaixaBancoAnterior = c.getDouble(0)
                        }
                        db.rawQuery(sqlEmp, arrayOf(dataAntSQL)).use { c ->
                            if (c.moveToFirst()) saldoEmprestimoAnterior = c.getDouble(0)
                        }

                    } else {
                        // PERÍODO FUTURO DENTRO DO ANO ATUAL (Projeções)
                        // C1: Saldo efetivo consolidado até HOJE
                        val sqlC1 = """
                        SELECT COALESCE(SUM(Valor), 0) FROM tbmovimento 
                        WHERE vrecurso IN ('1.001.001','1.001.002','1.003.001','2.001.004') 
                          AND dtlancto <= ? 
                          AND dtApr IS NOT NULL AND TRIM(COALESCE(dtApr,'')) <> '' 
                          AND recurso NOT IN ('0079','0022')
                    """.trimIndent()

                        // C2: Projeções confirmadas (status vazio) até o dia anterior
                        val sqlC2 = """
                        SELECT COALESCE(SUM(Valor), 0) FROM tbmovimento 
                        WHERE vrecurso IN ('1.002.001','2.001.002','2.001.003') 
                          AND dtVcto <= ? AND statusMov = ''
                    """.trimIndent()

                        // C4: Empréstimos (Recurso 0080) até o dia anterior
                        val sqlC4 =
                            "SELECT COALESCE(SUM(Valor), 0) FROM tbmovimento WHERE recurso = '0080' AND dtlancto <= ?"

                        db.rawQuery(sqlC1, arrayOf(dataHojeSQL)).use { c ->
                            if (c.moveToFirst()) saldoCaixaBancoAnterior += c.getDouble(0)
                        }
                        db.rawQuery(sqlC2, arrayOf(dataAntSQL)).use { c ->
                            if (c.moveToFirst()) saldoCaixaBancoAnterior += c.getDouble(0)
                        }
                        db.rawQuery(sqlC4, arrayOf(dataAntSQL)).use { c ->
                            if (c.moveToFirst()) {
                                saldoEmprestimoAnterior = c.getDouble(0)
                                // No seu Java você soma o empréstimo ao caixa para o saldo líquido
                                saldoCaixaBancoAnterior += saldoEmprestimoAnterior
                            }
                        }
                    }
                }

                // === ATRIBUIÇÃO PARA AS VARIÁVEIS QUE A TABELA USA ===
                // Importante: no seu Java, saldoInicialPeriodo recebe o saldo LIMPO
                // e saldoInicialEmprestimo recebe a dívida.
                val saldoInicialPeriodoCalculado = saldoCaixaBancoAnterior
                val saldoInicialEmprestimoCalculado = saldoEmprestimoAnterior

                // === CONSULTAS PRINCIPAIS ===
                val statusFiltro = when {
                    binding.radioButtonConf.isChecked -> " AND dtApr IS NOT NULL AND TRIM(COALESCE(dtApr,'')) <> ''"
                    binding.radioButtonPrev.isChecked -> " AND (dtApr IS NULL OR TRIM(COALESCE(dtApr,'')) = '')"
                    else -> ""
                }

                val sqlContas = """
                    SELECT nome_C AS Contas,
                           SUM(CASE WHEN strftime('%m', dtVcto) = '01' THEN Valor ELSE 0 END) AS janeiro,
                           SUM(CASE WHEN strftime('%m', dtVcto) = '02' THEN Valor ELSE 0 END) AS fevereiro,
                           SUM(CASE WHEN strftime('%m', dtVcto) = '03' THEN Valor ELSE 0 END) AS marco,
                           SUM(CASE WHEN strftime('%m', dtVcto) = '04' THEN Valor ELSE 0 END) AS abril,
                           SUM(CASE WHEN strftime('%m', dtVcto) = '05' THEN Valor ELSE 0 END) AS maio,
                           SUM(CASE WHEN strftime('%m', dtVcto) = '06' THEN Valor ELSE 0 END) AS junho,
                           SUM(CASE WHEN strftime('%m', dtVcto) = '07' THEN Valor ELSE 0 END) AS julho,
                           SUM(CASE WHEN strftime('%m', dtVcto) = '08' THEN Valor ELSE 0 END) AS agosto,
                           SUM(CASE WHEN strftime('%m', dtVcto) = '09' THEN Valor ELSE 0 END) AS setembro,
                           SUM(CASE WHEN strftime('%m', dtVcto) = '10' THEN Valor ELSE 0 END) AS outubro,
                           SUM(CASE WHEN strftime('%m', dtVcto) = '11' THEN Valor ELSE 0 END) AS novembro,
                           SUM(CASE WHEN strftime('%m', dtVcto) = '12' THEN Valor ELSE 0 END) AS dezembro,
                           SUM(CASE WHEN Valor != 0 THEN Valor ELSE 0 END) AS total
                    FROM viewmovrd
                    WHERE dtVcto BETWEEN ? AND ? AND nome_P <> 'NULO'
                      $statusFiltro
                      AND ((classif NOT IN ('3.003.005', '4.008.011'))
                           OR (classif = '4.008.011' AND (dtApr IS NULL OR TRIM(dtApr) = '')))
                    GROUP BY nome_C ORDER BY nome_C
                """.trimIndent()

                val queryReceitas = """
                    SELECT
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '01' AND Valor > 0 AND classif LIKE '3.%' AND classif != '3.003.005' THEN Valor ELSE 0 END), 2) AS janeiro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '02' AND Valor > 0 AND classif LIKE '3.%' AND classif != '3.003.005' THEN Valor ELSE 0 END), 2) AS fevereiro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '03' AND Valor > 0 AND classif LIKE '3.%' AND classif != '3.003.005' THEN Valor ELSE 0 END), 2) AS marco,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '04' AND Valor > 0 AND classif LIKE '3.%' AND classif != '3.003.005' THEN Valor ELSE 0 END), 2) AS abril,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '05' AND Valor > 0 AND classif LIKE '3.%' AND classif != '3.003.005' THEN Valor ELSE 0 END), 2) AS maio,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '06' AND Valor > 0 AND classif LIKE '3.%' AND classif != '3.003.005' THEN Valor ELSE 0 END), 2) AS junho,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '07' AND Valor > 0 AND classif LIKE '3.%' AND classif != '3.003.005' THEN Valor ELSE 0 END), 2) AS julho,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '08' AND Valor > 0 AND classif LIKE '3.%' AND classif != '3.003.005' THEN Valor ELSE 0 END), 2) AS agosto,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '09' AND Valor > 0 AND classif LIKE '3.%' AND classif != '3.003.005' THEN Valor ELSE 0 END), 2) AS setembro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '10' AND Valor > 0 AND classif LIKE '3.%' AND classif != '3.003.005' THEN Valor ELSE 0 END), 2) AS outubro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '11' AND Valor > 0 AND classif LIKE '3.%' AND classif != '3.003.005' THEN Valor ELSE 0 END), 2) AS novembro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '12' AND Valor > 0 AND classif LIKE '3.%' AND classif != '3.003.005' THEN Valor ELSE 0 END), 2) AS dezembro
                    FROM viewmovrd 
                    WHERE dtVcto BETWEEN ? AND ? AND nome_P <> 'NULO'
                      $statusFiltro
                """.trimIndent()

                val queryDespesas = """
                    SELECT
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '01' AND Valor < 0 AND classif LIKE '4.%' AND classif != '4.008.011' THEN Valor ELSE 0 END), 2) AS janeiro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '02' AND Valor < 0 AND classif LIKE '4.%' AND classif != '4.008.011' THEN Valor ELSE 0 END), 2) AS fevereiro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '03' AND Valor < 0 AND classif LIKE '4.%' AND classif != '4.008.011' THEN Valor ELSE 0 END), 2) AS marco,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '04' AND Valor < 0 AND classif LIKE '4.%' AND classif != '4.008.011' THEN Valor ELSE 0 END), 2) AS abril,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '05' AND Valor < 0 AND classif LIKE '4.%' AND classif != '4.008.011' THEN Valor ELSE 0 END), 2) AS maio,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '06' AND Valor < 0 AND classif LIKE '4.%' AND classif != '4.008.011' THEN Valor ELSE 0 END), 2) AS junho,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '07' AND Valor < 0 AND classif LIKE '4.%' AND classif != '4.008.011' THEN Valor ELSE 0 END), 2) AS julho,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '08' AND Valor < 0 AND classif LIKE '4.%' AND classif != '4.008.011' THEN Valor ELSE 0 END), 2) AS agosto,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '09' AND Valor < 0 AND classif LIKE '4.%' AND classif != '4.008.011' THEN Valor ELSE 0 END), 2) AS setembro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '10' AND Valor < 0 AND classif LIKE '4.%' AND classif != '4.008.011' THEN Valor ELSE 0 END), 2) AS outubro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '11' AND Valor < 0 AND classif LIKE '4.%' AND classif != '4.008.011' THEN Valor ELSE 0 END), 2) AS novembro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '12' AND Valor < 0 AND classif LIKE '4.%' AND classif != '4.008.011' THEN Valor ELSE 0 END), 2) AS dezembro
                    FROM viewmovrd 
                    WHERE dtVcto BETWEEN ? AND ? AND nome_P <> 'NULO'
                      $statusFiltro
                """.trimIndent()

                val queryEmprestimosObtidos = """
                    SELECT
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '01' AND classif = '3.003.005' THEN ABS(Valor) ELSE 0 END), 2) AS janeiro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '02' AND classif = '3.003.005' THEN ABS(Valor) ELSE 0 END), 2) AS fevereiro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '03' AND classif = '3.003.005' THEN ABS(Valor) ELSE 0 END), 2) AS marco,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '04' AND classif = '3.003.005' THEN ABS(Valor) ELSE 0 END), 2) AS abril,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '05' AND classif = '3.003.005' THEN ABS(Valor) ELSE 0 END), 2) AS maio,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '06' AND classif = '3.003.005' THEN ABS(Valor) ELSE 0 END), 2) AS junho,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '07' AND classif = '3.003.005' THEN ABS(Valor) ELSE 0 END), 2) AS julho,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '08' AND classif = '3.003.005' THEN ABS(Valor) ELSE 0 END), 2) AS agosto,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '09' AND classif = '3.003.005' THEN ABS(Valor) ELSE 0 END), 2) AS setembro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '10' AND classif = '3.003.005' THEN ABS(Valor) ELSE 0 END), 2) AS outubro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '11' AND classif = '3.003.005' THEN ABS(Valor) ELSE 0 END), 2) AS novembro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '12' AND classif = '3.003.005' THEN ABS(Valor) ELSE 0 END), 2) AS dezembro
                    FROM tbmovimento 
                    WHERE dtVcto BETWEEN ? AND ? AND dtApr IS NOT NULL
                """.trimIndent()

                val queryEmprestimosPagos = """
                    SELECT
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '01' AND classif = '4.008.011' THEN -ABS(Valor) ELSE 0 END), 2) AS janeiro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '02' AND classif = '4.008.011' THEN -ABS(Valor) ELSE 0 END), 2) AS fevereiro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '03' AND classif = '4.008.011' THEN -ABS(Valor) ELSE 0 END), 2) AS marco,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '04' AND classif = '4.008.011' THEN -ABS(Valor) ELSE 0 END), 2) AS abril,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '05' AND classif = '4.008.011' THEN -ABS(Valor) ELSE 0 END), 2) AS maio,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '06' AND classif = '4.008.011' THEN -ABS(Valor) ELSE 0 END), 2) AS junho,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '07' AND classif = '4.008.011' THEN -ABS(Valor) ELSE 0 END), 2) AS julho,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '08' AND classif = '4.008.011' THEN -ABS(Valor) ELSE 0 END), 2) AS agosto,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '09' AND classif = '4.008.011' THEN -ABS(Valor) ELSE 0 END), 2) AS setembro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '10' AND classif = '4.008.011' THEN -ABS(Valor) ELSE 0 END), 2) AS outubro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '11' AND classif = '4.008.011' THEN -ABS(Valor) ELSE 0 END), 2) AS novembro,
                        ROUND(SUM(CASE WHEN strftime('%m', dtVcto) = '12' AND classif = '4.008.011' THEN -ABS(Valor) ELSE 0 END), 2) AS dezembro
                    FROM tbmovimento 
                    WHERE dtVcto BETWEEN ? AND ? AND dtApr IS NOT NULL
                """.trimIndent()

                // === LEITURA ===
                val receitas = DoubleArray(12)
                val despesas = DoubleArray(12)
                val empObtidos = DoubleArray(12)
                val empPagos = DoubleArray(12)

                db.rawQuery(queryReceitas, arrayOf(dataIniSQL, dataFimSQL)).use { c ->
                    if (c.moveToFirst()) for (i in 0..11) receitas[i] = c.getDouble(i)
                }
                db.rawQuery(queryDespesas, arrayOf(dataIniSQL, dataFimSQL)).use { c ->
                    if (c.moveToFirst()) for (i in 0..11) despesas[i] = c.getDouble(i)
                }
                db.rawQuery(queryEmprestimosObtidos, arrayOf(dataIniSQL, dataFimSQL)).use { c ->
                    if (c.moveToFirst()) for (i in 0..11) empObtidos[i] = c.getDouble(i)
                }
                db.rawQuery(queryEmprestimosPagos, arrayOf(dataIniSQL, dataFimSQL)).use { c ->
                    if (c.moveToFirst()) for (i in 0..11) empPagos[i] = c.getDouble(i)
                }

                // === CÁLCULO ACUMULADO ===
                val saldosFinais = DoubleArray(12)
                val saldoAcumuladoEmprestimo = DoubleArray(12)
                var saldoAcumulado = saldoCaixaBancoAnterior

                for (i in 0..11) {
                    if (i >= mesInicio && i <= mesFim) {
                        val fluxo = receitas[i] + despesas[i]
                        saldoAcumulado += fluxo
                        saldosFinais[i] = saldoAcumulado

                        val empMes = empObtidos[i] + empPagos[i]
                        saldoAcumuladoEmprestimo[i] = if (i == 0) {
                            saldoEmprestimoAnterior + empMes
                        } else {
                            saldoAcumuladoEmprestimo[i - 1] + empMes
                        }
                    }
                }

                // === TRANSPORTE CORRETO (só o limpo) ===
                prefs.edit()
                    .putFloat(
                        "saldo_transportado",
                        saldosFinais[mesFim].toFloat()
                    ) // 208,53 — só o limpo
                    .putFloat(
                        "saldo_emprestimo_transportado",
                        saldoAcumuladoEmprestimo[mesFim].toFloat()
                    ) // -600 — separado
                    .apply()

                // === LISTA ===
                items.clear()

                // === LINHA SALDO ANTERIOR ===
                val linhaAnt = Array(13) { "" }

                // Para o primeiro mês do período, mostra o saldo anterior calculado
                if (mesInicio > 0) {
                    // Se não é janeiro, pega o saldo final do mês anterior
                    linhaAnt[mesInicio] =
                        decimalFormat.format(saldosFinais[mesInicio - 1]).toString()
                    linhaAnt[12] = decimalFormat.format(saldosFinais[mesInicio - 1]).toString()
                } else {
                    // Se é janeiro (mesInicio == 0), usa o saldo anterior transportado
                    linhaAnt[mesInicio] = decimalFormat.format(saldoCaixaBancoAnterior).toString()
                    linhaAnt[12] = decimalFormat.format(saldoCaixaBancoAnterior).toString()
                }

                items.add(
                    ConsultaRCItem(
                        "Saldo Anterior(Cx/Bcos/Poup)S/Empr.",
                        linhaAnt[0],
                        linhaAnt[1],
                        linhaAnt[2],
                        linhaAnt[3],
                        linhaAnt[4],
                        linhaAnt[5],
                        linhaAnt[6],
                        linhaAnt[7],
                        linhaAnt[8],
                        linhaAnt[9],
                        linhaAnt[10],
                        linhaAnt[11],
                        linhaAnt[12]
                    )
                )

                // Contas...
                db.rawQuery(sqlContas, arrayOf(dataIniSQL, dataFimSQL)).use { c ->
                    while (c.moveToNext()) {
                        items.add(
                            ConsultaRCItem(
                                c.getString(0),
                                decimalFormat.format(c.getDouble(1)),
                                decimalFormat.format(c.getDouble(2)),
                                decimalFormat.format(c.getDouble(3)),
                                decimalFormat.format(c.getDouble(4)),
                                decimalFormat.format(c.getDouble(5)),
                                decimalFormat.format(c.getDouble(6)),
                                decimalFormat.format(c.getDouble(7)),
                                decimalFormat.format(c.getDouble(8)),
                                decimalFormat.format(c.getDouble(9)),
                                decimalFormat.format(c.getDouble(10)),
                                decimalFormat.format(c.getDouble(11)),
                                decimalFormat.format(c.getDouble(12)),
                                decimalFormat.format(c.getDouble(13))
                            )
                        )
                    }
                }

                // Receitas, Despesas, Resultado
                fun addLinha(nome: String, valores: DoubleArray) {
                    val total = valores.sum()
                    val valuesToFormat = if (nome == "Despesas") {
                        DoubleArray(valores.size) { i -> Math.abs(valores[i]) }
                    } else {
                        valores
                    }
                    val totalToFormat = if (nome == "Despesas") Math.abs(total) else total

                    items.add(
                        ConsultaRCItem(
                            nome,
                            decimalFormat.format(valuesToFormat[0]).toString(),
                            decimalFormat.format(valuesToFormat[1]).toString(),
                            decimalFormat.format(valuesToFormat[2]).toString(),
                            decimalFormat.format(valuesToFormat[3]).toString(),
                            decimalFormat.format(valuesToFormat[4]).toString(),
                            decimalFormat.format(valuesToFormat[5]).toString(),
                            decimalFormat.format(valuesToFormat[6]).toString(),
                            decimalFormat.format(valuesToFormat[7]).toString(),
                            decimalFormat.format(valuesToFormat[8]).toString(),
                            decimalFormat.format(valuesToFormat[9]).toString(),
                            decimalFormat.format(valuesToFormat[10]).toString(),
                            decimalFormat.format(valuesToFormat[11]).toString(),
                            decimalFormat.format(totalToFormat).toString()
                        )
                    )
                }

                addLinha("Receitas", receitas)
                addLinha("Despesas", despesas)
                addLinha("Resultado", receitas.zip(despesas) { r, d -> r + d }.toDoubleArray())
                items.add(ConsultaRCItem("", "", "", "", "", "", "", "", "", "", "", "", "", ""))

                // Saldo Inicial Empréstimo (sempre negativo)
                val linhaEmpIni = Array(13) { "" }
                val emprestimoInicialExibir =
                    if (saldoEmprestimoAnterior > 0) -saldoEmprestimoAnterior else saldoEmprestimoAnterior
                linhaEmpIni[mesInicio] = decimalFormat.format(emprestimoInicialExibir).toString()
                linhaEmpIni[12] = decimalFormat.format(emprestimoInicialExibir).toString()
                items.add(
                    ConsultaRCItem(
                        "Saldo Inicial Empréstimo",
                        linhaEmpIni[0],
                        linhaEmpIni[1],
                        linhaEmpIni[2],
                        linhaEmpIni[3],
                        linhaEmpIni[4],
                        linhaEmpIni[5],
                        linhaEmpIni[6],
                        linhaEmpIni[7],
                        linhaEmpIni[8],
                        linhaEmpIni[9],
                        linhaEmpIni[10],
                        linhaEmpIni[11],
                        linhaEmpIni[12]
                    )
                )

                // Empréstimos Obtidos e Pagos
                fun addEmp(nome: String, valores: DoubleArray, inverter: Boolean = false) {
                    val sinal = if (inverter) -1 else 1
                    val total = valores.sum() * sinal

                    items.add(
                        ConsultaRCItem(
                            nome,
                            decimalFormat.format(valores[0] * sinal).toString(),
                            decimalFormat.format(valores[1] * sinal).toString(),
                            decimalFormat.format(valores[2] * sinal).toString(),
                            decimalFormat.format(valores[3] * sinal).toString(),
                            decimalFormat.format(valores[4] * sinal).toString(),
                            decimalFormat.format(valores[5] * sinal).toString(),
                            decimalFormat.format(valores[6] * sinal).toString(),
                            decimalFormat.format(valores[7] * sinal).toString(),
                            decimalFormat.format(valores[8] * sinal).toString(),
                            decimalFormat.format(valores[9] * sinal).toString(),
                            decimalFormat.format(valores[10] * sinal).toString(),
                            decimalFormat.format(valores[11] * sinal).toString(),
                            decimalFormat.format(total).toString()
                        )
                    )
                }

                addEmp("Emprést.Obt.C.Prazo-Recebidos", empObtidos, inverter = true)
                addEmp("Emprést.Obt.C.Prazo-Pagos", empPagos, inverter = true)

                // Saldo Empréstimos C.Prazo (sempre negativo)
                val linhaSaldoEmp = Array(13) { i ->
                    if (i in mesInicio..mesFim) {
                        val valor = saldoAcumuladoEmprestimo[i]
                        val valorExibir = if (valor > 0) -valor else valor
                        decimalFormat.format(valorExibir).toString()
                    } else ""
                }
                linhaSaldoEmp[12] = decimalFormat.format(
                    if (saldoAcumuladoEmprestimo[mesFim] > 0) -saldoAcumuladoEmprestimo[mesFim] else saldoAcumuladoEmprestimo[mesFim]
                ).toString()
                items.add(
                    ConsultaRCItem(
                        "Saldo Empréstimos C.Prazo",
                        linhaSaldoEmp[0],
                        linhaSaldoEmp[1],
                        linhaSaldoEmp[2],
                        linhaSaldoEmp[3],
                        linhaSaldoEmp[4],
                        linhaSaldoEmp[5],
                        linhaSaldoEmp[6],
                        linhaSaldoEmp[7],
                        linhaSaldoEmp[8],
                        linhaSaldoEmp[9],
                        linhaSaldoEmp[10],
                        linhaSaldoEmp[11],
                        linhaSaldoEmp[12]
                    )
                )

                items.add(ConsultaRCItem("", "", "", "", "", "", "", "", "", "", "", "", "", ""))

                // Saldo Final sem Empréstimos (só o limpo)
                val linhaFinalSem = Array(13) { i ->
                    if (i in mesInicio..mesFim) decimalFormat.format(saldosFinais[i])
                        .toString() else ""
                }
                linhaFinalSem[12] = decimalFormat.format(saldosFinais[mesFim]).toString()
                items.add(
                    ConsultaRCItem(
                        "Saldo Final sem Empréstimos",
                        linhaFinalSem[0],
                        linhaFinalSem[1],
                        linhaFinalSem[2],
                        linhaFinalSem[3],
                        linhaFinalSem[4],
                        linhaFinalSem[5],
                        linhaFinalSem[6],
                        linhaFinalSem[7],
                        linhaFinalSem[8],
                        linhaFinalSem[9],
                        linhaFinalSem[10],
                        linhaFinalSem[11],
                        linhaFinalSem[12]
                    )
                )

                // Saldo Total Disponível (limpo + empréstimo)
                val linhaTotal = Array(13) { i ->
                    if (i in mesInicio..mesFim) decimalFormat.format(saldosFinais[i] + saldoAcumuladoEmprestimo[i])
                        .toString() else ""
                }
                linhaTotal[12] =
                    decimalFormat.format(saldosFinais[mesFim] + saldoAcumuladoEmprestimo[mesFim])
                        .toString()
                items.add(
                    ConsultaRCItem(
                        "Saldo Total Disponível (com empréstimos)",
                        linhaTotal[0],
                        linhaTotal[1],
                        linhaTotal[2],
                        linhaTotal[3],
                        linhaTotal[4],
                        linhaTotal[5],
                        linhaTotal[6],
                        linhaTotal[7],
                        linhaTotal[8],
                        linhaTotal[9],
                        linhaTotal[10],
                        linhaTotal[11],
                        linhaTotal[12]
                    )
                )

                // === PREENCHER ARRAYS PARA O GRÁFICO ===
                for (i in 0..11) {
                    receitasGrafico[i] = receitas[i]
                    despesasGrafico[i] = despesas[i]

                    // preenchimento das variáveis globais:
                    saldosFinaisGrafico[i] = saldosFinais[i]
                    saldoAcumuladoEmprestimoGrafico[i] = saldoAcumuladoEmprestimo[i]
                    resultadosMensais[i] = receitas[i] + despesas[i]
                    monthlyFinalBalances[i] = saldosFinais[i] + saldoAcumuladoEmprestimo[i]
                }
                mesInicioAtual = mesInicio
                mesFimAtual = mesFim

                // ATUALIZA UI
                withContext(Dispatchers.Main) {
                    //val header = binding.headerContainer.findViewById<LinearLayout>(R.id.headerValues)
                    val header = binding.headerValues
                    header.removeAllViews()

                    val meses = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

                    // Converte 100dp para pixels (mesmo valor que usamos nos itens da lista)
                    val larguraFixaMes = (100 * resources.displayMetrics.density).toInt()

                    for (i in mesInicio..mesFim) {
                        TextView(this@ConsultaRCActivity).apply {
                            text = meses[i]
                            gravity = Gravity.CENTER
                            setPadding(16, 16, 16, 16)

                            // REMOVIDO O PESO (1f) E ADICIONADA LARGURA FIXA
                            layoutParams = LinearLayout.LayoutParams(
                                larguraFixaMes,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                            setTypeface(null, Typeface.BOLD)
                            textSize = 13f
                            setTextColor(Color.WHITE) // Para destacar no fundo cinza escuro
                        }.let(header::addView)
                    }

                    // Atualiza o Adapter com as novas configurações de colunas
                    (binding.recyclerViewConsultaRC.adapter as ConsultaRCAdapter).apply {
                        updateData(
                            this@ConsultaRCActivity.items,
                            meses,
                            mesInicio,
                            mesFim
                        )
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ConsultaRCActivity, "Erro: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
            } finally {
                db.close()
            }
        }
    }

    private fun calcularSaldoFechamentoAnoAnterior(ano: Int): Pair<Double, Double> {
        val db = DatabaseHelper(this@ConsultaRCActivity).readableDatabase
        // Para consultar 2026, o limite é 31/12/2025
        val dataLimite = "${ano - 1}-12-31"

        // SQL 1: Saldo Caixa/Banco (IGUAL AO SEU JAVA)
        val sqlCaixa = """
        SELECT COALESCE(SUM(Valor), 0) AS saldo 
        FROM tbmovimento 
        WHERE dtlancto <= ? 
          AND vrecurso IN ('1.001.001', '1.001.002', '1.003.001', '2.001.004') 
          AND recurso NOT IN ('0079', '0022', '0080') 
          AND dtApr IS NOT NULL AND TRIM(COALESCE(dtApr,'')) <> ''
    """.trimIndent()

        // SQL 2: Saldo Empréstimo (IGUAL AO SEU JAVA)
        val sqlEmp = """
        SELECT COALESCE(SUM(Valor), 0) AS saldo 
        FROM tbmovimento 
        WHERE recurso = '0080' AND dtlancto <= ?
    """.trimIndent()

        var saldoC = 0.0
        var saldoE = 0.0

        db.rawQuery(sqlCaixa, arrayOf(dataLimite)).use { c ->
            if (c.moveToFirst()) saldoC = c.getDouble(0)
        }

        db.rawQuery(sqlEmp, arrayOf(dataLimite)).use { c ->
            if (c.moveToFirst()) saldoE = c.getDouble(0)
        }

        return Pair(saldoC, saldoE)
    }

    private fun limpar() {
        binding.editTextDataInicial.text?.clear()
        binding.editTextDataFinal.text?.clear()
        items.clear()
        (binding.recyclerViewConsultaRC.adapter as ConsultaRCAdapter).apply {
            this.items = items
            notifyDataSetChanged()
        }
    }

    private fun imprimir() {
        val adapter = binding.recyclerViewConsultaRC.adapter as? ConsultaRCAdapter
        val items = adapter?.items ?: emptyList()

        if (items.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para imprimir!", Toast.LENGTH_SHORT).show()
            return
        }

        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter = ConsultaRCPrintAdapter(this, items)
        printManager.print("ConsultaRC", printAdapter, PrintAttributes.Builder().build())
    }

    private fun exportar() {
        if (items.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para exportar!", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "Referencia_Cruzada_${
            SimpleDateFormat("dd_MM_yyyy_HH_mm", Locale.getDefault()).format(Date())
        }.xlsx"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Referência Cruzada")

            // 1. AJUSTE DE SEGURANÇA DOS ÍNDICES (Evita o erro fromIndex: 0, toIndex: 12)
            // O subList(de, ate) no Kotlin/Java vai até 'ate - 1'.
            // Se endMonth for 11 (Dezembro), usamos 12 para pegar até o índice 11.
            val sMonth = startMonth.coerceAtLeast(0)
            val eMonth = endMonth.coerceIn(0, 11)

            // 2. CABEÇALHO
            val headerRow = sheet.createRow(0)

            // Months deve ser sua lista de strings ["Jan", "Fev", ...]
            val mesesFiltrados = months.subList(sMonth, (eMonth + 1).coerceAtMost(months.size))
            val headers = listOf("Contas") + mesesFiltrados + listOf("Total")

            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
            }

            // 3. DADOS
            items.forEachIndexed { rowIndex, item ->
                val row = sheet.createRow(rowIndex + 1)

                // Coluna 0: Nome da Conta
                row.createCell(0).setCellValue(item.contas)

                // Lista fixa para mapear os índices
                val values = listOf(
                    item.jan, item.fev, item.mar, item.abr, item.mai, item.jun,
                    item.jul, item.ago, item.set, item.out, item.nov, item.dez
                )

                var colIndexExcel = 1

                // Loop pelos meses selecionados no filtro
                for (i in sMonth..eMonth) {
                    if (i < values.size) {
                        val valorStr = values[i]
                        // Converte "1.250,00" para Double 1250.0
                        val valor =
                            valorStr.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
                        row.createCell(colIndexExcel).setCellValue(valor)
                        colIndexExcel++
                    }
                }

                // Coluna Final: Total
                val total = item.total.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
                row.createCell(colIndexExcel).setCellValue(total)
            }

            // 4. LARGURA DAS COLUNAS
            for (i in headers.indices) {
                sheet.setColumnWidth(i, 18 * 256)
            }

            // 5. GRAVAÇÃO DO ARQUIVO
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()

            Toast.makeText(this, "Excel salvo em:\n${file.absolutePath}", Toast.LENGTH_LONG).show()
            Log.d("ConsultaRCActivity", "Excel exportado com sucesso: ${file.absolutePath}")

        } catch (t: Throwable) {
            Toast.makeText(this, "Erro ao exportar: ${t.message}", Toast.LENGTH_LONG).show()
            Log.e("ConsultaRCActivity", "Erro detalhado ao exportar Excel", t)
        }
    }

    private fun mostrarGrafico() {
        if (mesInicioAtual > mesFimAtual || receitasGrafico.all { it == 0.0 } && despesasGrafico.all { it == 0.0 }) {
            Toast.makeText(this, "Nenhum dado para exibir no gráfico.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Inicializa o Utils do MPChart (resolve o erro do Logcat)
        com.github.mikephil.charting.utils.Utils.init(this)

        val entradasReceitas = mutableListOf<Entry>()
        val entradasDespesas = mutableListOf<Entry>()
        val entradasResultados = mutableListOf<Entry>()
        val entradasSaldos = mutableListOf<Entry>()

        val mesesAbrev = listOf(
            "Jan",
            "Fev",
            "Mar",
            "Abr",
            "Mai",
            "Jun",
            "Jul",
            "Ago",
            "Set",
            "Out",
            "Nov",
            "Dez"
        )

        var minOutros = Float.MAX_VALUE
        var maxOutros = -Float.MAX_VALUE
        var minSaldo = Float.MAX_VALUE
        var maxSaldo = -Float.MAX_VALUE

        for (i in mesInicioAtual..mesFimAtual) {
            val x = (i - mesInicioAtual).toFloat()

            val r = receitasGrafico[i].toFloat()
            val d = Math.abs(despesasGrafico[i]).toFloat()
            val res = (receitasGrafico[i] + despesasGrafico[i]).toFloat()
            val s = (saldosFinaisGrafico[i] + saldoAcumuladoEmprestimoGrafico[i]).toFloat()

            // Adicione os pontos sempre, mesmo que sejam 0.0
            entradasReceitas.add(Entry(x, r))
            entradasDespesas.add(Entry(x, d))
            entradasResultados.add(Entry(x, res))
            entradasSaldos.add(Entry(x, s))

            // Atualiza limites dinâmicos separadamente
            minOutros = minOf(minOutros, minOf(r, minOf(d, res)))
            maxOutros = maxOf(maxOutros, maxOf(r, maxOf(d, res)))
            minSaldo = minOf(minSaldo, s)
            maxSaldo = maxOf(maxSaldo, s)
        }

        // Padding dinâmico para os eixos
        val rangeOutros = maxOutros - minOutros
        val paddingOutros = if (rangeOutros == 0f) (if (maxOutros == 0f) 100f else Math.abs(maxOutros) * 0.2f) else rangeOutros * 0.15f
        val dynamicMinOutros = minOutros - paddingOutros
        val dynamicMaxOutros = maxOutros + paddingOutros

        val rangeSaldo = maxSaldo - minSaldo
        val paddingSaldo = if (rangeSaldo == 0f) (if (maxSaldo == 0f) 1000f else Math.abs(maxSaldo) * 0.2f) else rangeSaldo * 0.15f
        val dynamicMinSaldo = minSaldo - paddingSaldo
        val dynamicMaxSaldo = maxSaldo + paddingSaldo

        val setReceitas = LineDataSet(entradasReceitas, "Receitas").apply {
            color = Color.rgb(76, 175, 80)
            setCircleColor(color)
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawValues(true)
            valueTextSize = 7f
            valueTextColor = Color.BLACK
            mode = LineDataSet.Mode.CUBIC_BEZIER
            axisDependency = YAxis.AxisDependency.LEFT
        }

        val setDespesas = LineDataSet(entradasDespesas, "Despesas").apply {
            color = Color.rgb(255, 82, 82)
            setCircleColor(color)
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawValues(true)
            valueTextSize = 7f
            valueTextColor = Color.BLACK
            mode = LineDataSet.Mode.CUBIC_BEZIER
            axisDependency = YAxis.AxisDependency.LEFT
        }

        val setResultados = LineDataSet(entradasResultados, "Resultado").apply {
            color = Color.rgb(33, 150, 243)
            setCircleColor(color)
            lineWidth = 2f
            circleRadius = 3f
            //setDrawValues(false) // Desabilitado para não poluir
            mode = LineDataSet.Mode.CUBIC_BEZIER
            axisDependency = YAxis.AxisDependency.LEFT
        }

        val setSaldos = LineDataSet(entradasSaldos, "Saldo Total (Eixo Dir.)").apply {
            color = Color.rgb(0, 70, 140)
            setCircleColor(color)
            lineWidth = 3.5f
            circleRadius = 5f
            setDrawValues(true)
            valueTextSize = 8f
            valueTextColor = Color.rgb(0, 70, 140)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            axisDependency = YAxis.AxisDependency.RIGHT
        }

        val lineData = LineData(setReceitas, setDespesas, setResultados, setSaldos)

        val chart = LineChart(this).apply {
            data = lineData
            description.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter =
                    IndexAxisValueFormatter(mesesAbrev.subList(mesInicioAtual, mesFimAtual + 1))
                granularity = 1f
                setDrawGridLines(false)
                textSize = 8f
            }

            // EIXO ESQUERDO -> Receitas, Despesas, Resultados
            axisLeft.apply {
                setDrawGridLines(true)
                textSize = 8f
                
                // LIMITES INFORMADOS VIA CÓDIGO
                axisMinimum = -20000f
                axisMaximum = 20000f
                
                // Intervalo de 100 em 100 como solicitado anteriormente
                granularity = 100f
                isGranularityEnabled = true
                setLabelCount(15, false) 
                
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return decimalFormat.format(value.toDouble())
                    }
                }
            }

            // EIXO DIREITO -> Saldo Total
            axisRight.apply {
                isEnabled = true
                setDrawGridLines(false) // Não desenhar grades duplicatedas
                textSize = 8f
                textColor = Color.rgb(0, 70, 140)
                axisMinimum = -20000f
                axisMaximum = 20000f
                //axisMinimum = 0f //dynamicMinSaldo
                //axisMaximum = //dynamicMaxSaldo
                
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "S: " + decimalFormat.format(value.toDouble())
                    }
                }
            }

            legend.apply {
                isEnabled = true
                textSize = 8f
                form = Legend.LegendForm.LINE
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
            }

            setBackgroundColor(Color.WHITE)
            setExtraOffsets(16f, 16f, 16f, 16f)
            animateX(800)
        }

        // Altura grande
        val height = (resources.displayMetrics.heightPixels * 0.9).toInt() // 90% da tela

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            addView(chart, LinearLayout.LayoutParams.MATCH_PARENT, height)
        }

        AlertDialog.Builder(this)
            .setTitle("Gráfico de Referência Cruzada")
            .setView(container)
            .setPositiveButton("Fechar", null)
            .show()
    }

    private fun salvarPrevistos() {
        if (items.isEmpty()) {
            Toast.makeText(this, "Execute a pesquisa primeiro!", Toast.LENGTH_SHORT).show()
            return
        }

        val input = android.widget.EditText(this).apply {
            hint = "Expectativa de Inflação/Reajuste (%)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        AlertDialog.Builder(this)
            .setTitle("Salvar Previstos")
            .setMessage("Informe a expectativa de inflação/reajuste (%) considerada:")
            .setView(input)
            .setPositiveButton("Salvar") { _, _ ->
                val inflacaoStr = input.text.toString()
                if (inflacaoStr.isEmpty()) {
                    Toast.makeText(this, "Informe a inflação!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val inflacao = inflacaoStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                val anoAtual = Calendar.getInstance().get(Calendar.YEAR)

                lifecycleScope.launch(Dispatchers.IO) {
                    val db = DatabaseHelper(this@ConsultaRCActivity)

                    // Primeiro limpa dados do ano
                    db.deletePlanoDiretor(anoAtual)

                    // Filtra apenas contas válidas (não totais/saldos)
                    val contasValidas = items.filter { item ->
                        !item.contas.startsWith("Saldo") &&
                        !item.contas.startsWith("Resultado") &&
                        !item.contas.startsWith("Receitas") &&
                        !item.contas.startsWith("Despesas") &&
                        item.contas.isNotEmpty()
                    }

                    val registros = mutableListOf<PlanoDiretorRecord>()

                    for (item in contasValidas) {
                        val valores = listOf(
                            item.jan, item.fev, item.mar, item.abr, item.mai, item.jun,
                            item.jul, item.ago, item.set, item.out, item.nov, item.dez
                        )

                        for ((mes, valorStr) in valores.withIndex()) {
                            if (valorStr.isNotEmpty()) {
                                val valor = valorStr.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
                                if (valor != 0.0) {
                                    registros.add(
                                        PlanoDiretorRecord(
                                            anoReferencia = anoAtual,
                                            mes = mes + 1,
                                            classificacao = "",
                                            conta = item.contas,
                                            valorPlanejado = valor,
                                            inflacaoPremissa = inflacao
                                        )
                                    )
                                }
                            }
                        }
                    }

                    val inseridos = db.salvarPlanoDiretorBatch(registros)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ConsultaRCActivity,
                            "Salvo $inseridos registros para análise!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun abrirAuditoria() {
        val intent = android.content.Intent(this, ConsultaAuditoriaActivity::class.java)
        startActivity(intent)
    }
}