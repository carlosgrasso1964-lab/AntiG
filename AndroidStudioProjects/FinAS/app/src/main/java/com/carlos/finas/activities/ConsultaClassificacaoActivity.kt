package com.carlos.finas.activities

import android.R
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.models.Lancamento
import com.carlos.finas.adapters.LancamentosAdapter
import com.carlos.finas.databinding.ActivityConsultaClassificacaoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.iterator

class ConsultaClassificacaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsultaClassificacaoBinding
    private lateinit var dbHelper: DatabaseHelper
    private val dateFormatInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateFormatDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val decimalFormat =
        DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
    private var selectedClassif: String = ""

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
        binding = ActivityConsultaClassificacaoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DatabaseHelper(this)

        // Carregar classificações de forma assíncrona
        loadClassificacoesAsync()
        binding.edtDataIni.addTextChangedListener(DateMaskWatcher())
        binding.edtDataFim.addTextChangedListener(DateMaskWatcher())
        binding.recyclerViewResultados.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewResultados.adapter =
            LancamentosAdapter.Companion.create(emptyList()) { lancamento ->
                Toast.makeText(this, "Clicou em ${lancamento.Descr}", Toast.LENGTH_SHORT).show()
            }

        binding.spinnerClassificacao.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            @Suppress("UNCHECKED_CAST")
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val accounts = parent.tag as? List<Pair<String, String>>
                if (accounts == null) {
                    Log.e("ConsultaClassificacao", "Tag do spinner não é uma List<Pair<String, String>>")
                    selectedClassif = ""
                    Toast.makeText(
                        this@ConsultaClassificacaoActivity,
                        "Erro: Classificações não carregadas corretamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                if (position > 0) {
                    val selected = accounts[position]
                    selectedClassif = selected.second
                } else {
                    selectedClassif = ""
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedClassif = ""
            }
        })

        binding.btnPesquisar.setOnClickListener {
            if (binding.edtDataIni.text.toString().trim().length < 10) {
                Toast.makeText(this, "Preencha a data inicial!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.edtDataFim.text.toString().trim().length < 10) {
                Toast.makeText(this, "Preencha a data final!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedClassif.isEmpty()) {
                Toast.makeText(this, "Selecione uma classificação!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            pesquisarMovimentos()
        }

        binding.btnLimpar.setOnClickListener {
            binding.spinnerClassificacao.setSelection(0)
            selectedClassif = ""
            binding.edtDataIni.setText("")
            binding.edtDataFim.setText("")
            binding.recyclerViewResultados.adapter =
                LancamentosAdapter.Companion.create(emptyList()) { lancamento ->
                    Toast.makeText(this, "Clicou em ${lancamento.Descr}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.btnImprimir.setOnClickListener {
            Log.d("ConsultaClassificacaoActivity", "Botão Imprimir clicado")
            val adapter = binding.recyclerViewResultados.adapter as? LancamentosAdapter
            if (adapter == null || adapter.itemCount == 0) {
                Toast.makeText(this, "Nenhum dado para gerar o PDF!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val nomeClassificacao = binding.spinnerClassificacao.selectedItem.toString()
            createDocument.launch("Consulta_Classificacao_$nomeClassificacao.pdf")
        }

        // binding.btnFechar.setOnClickListener {
        //     finish()
        // }
    }

    private fun loadClassificacoesAsync() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val accounts = withContext(Dispatchers.IO) {
                    val db = dbHelper.readableDatabase
                    val cursor = db.rawQuery(
                        "SELECT nome_C, cod_Geral FROM gpprincipal WHERE nome_C <> '-' ORDER BY nome_C",
                        null
                    )
                    val accountsList = mutableListOf<Pair<String, String>>().apply {
                        add(Pair("Selecione uma classificação", "")) // Item padrão
                    }
                    cursor.use {
                        while (it.moveToNext()) {
                            val nomeC = it.getString(it.getColumnIndexOrThrow("nome_C"))
                            val codGeral = it.getString(it.getColumnIndexOrThrow("cod_Geral"))
                            accountsList.add(Pair("$nomeC - $codGeral", codGeral))
                            Log.d(
                                "ConsultaClassificacao",
                                "Carregado: nome_C=$nomeC, cod_Geral=$codGeral"
                            )
                        }
                    }
                    db.close()
                    accountsList
                }

                val adapter = ArrayAdapter(
                    this@ConsultaClassificacaoActivity,
                    R.layout.simple_spinner_item,
                    accounts.map { it.first })
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                binding.spinnerClassificacao.adapter = adapter
                binding.spinnerClassificacao.tag = accounts
            } catch (e: Exception) {
                Log.e("ConsultaClassificacao", "Erro ao carregar classificações: ${e.message}", e)
                Toast.makeText(
                    this@ConsultaClassificacaoActivity,
                    "Erro ao carregar classificações: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @SuppressLint("LongLogTag")
    private fun pesquisarMovimentos() {
        val dataIni = binding.edtDataIni.text.toString()
        val dataFim = binding.edtDataFim.text.toString()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val dataIniDb = dateFormatDb.format(dateFormatInput.parse(dataIni)!!)
                val dataFimDb = dateFormatDb.format(dateFormatInput.parse(dataFim)!!)

                val movimentos = withContext(Dispatchers.IO) {
                    val db = dbHelper.readableDatabase
                    val query = """
                        SELECT * FROM ${DatabaseHelper.Companion.TABLE_LANCAMENTOS} 
                        WHERE dtVcto BETWEEN ? AND ? AND classif = ? 
                        ORDER BY dtVcto
                    """.trimIndent()
                    val cursor = db.rawQuery(query, arrayOf(dataIniDb, dataFimDb, selectedClassif))
                    val movimentos = mutableListOf<Lancamento>()
                    var saldoAcumulado: Double? = 0.0
                    cursor.use {
                        while (it.moveToNext()) {
                            val movimento = Lancamento(
                                idMov = it.getInt(it.getColumnIndexOrThrow("idMov")),
                                recurso = it.getString(it.getColumnIndexOrThrow("recurso")) ?: "",
                                vrecurso = it.getString(it.getColumnIndexOrThrow("vrecurso")) ?: "",
                                clifor = it.getString(it.getColumnIndexOrThrow("clifor")) ?: "",
                                vCliFor = it.getString(it.getColumnIndexOrThrow("vCliFor")) ?: "",
                                dtLancto = it.getString(it.getColumnIndexOrThrow("dtlancto")) ?: "",
                                dtEmi = it.getString(it.getColumnIndexOrThrow("dtEmi")) ?: "",
                                dtVcto = it.getString(it.getColumnIndexOrThrow("dtVcto")) ?: "",
                                documento = it.getString(it.getColumnIndexOrThrow("documento")),
                                classif = it.getString(it.getColumnIndexOrThrow("classif")) ?: "",
                                Descr = it.getString(it.getColumnIndexOrThrow("Descr")) ?: "",
                                Valor = it.getDouble(it.getColumnIndexOrThrow("Valor")),
                                dtApr = it.getString(it.getColumnIndexOrThrow("dtApr")),
                                statusMov = it.getString(it.getColumnIndexOrThrow("statusMov"))
                                    ?: "",
                                Prev = it.getString(it.getColumnIndexOrThrow("Prev")) ?: "",
                                saldoAcumulado = saldoAcumulado?.plus(
                                    it.getDouble(
                                        it.getColumnIndexOrThrow(
                                            "Valor"
                                        )
                                    )
                                )
                            )
                            saldoAcumulado = movimento.saldoAcumulado
                            movimentos.add(movimento)
                        }
                    }
                    db.close()
                    movimentos
                }

                val adapter = LancamentosAdapter.Companion.create(movimentos) { lancamento ->
                    Toast.makeText(
                        this@ConsultaClassificacaoActivity,
                        "Clicou em ${lancamento.Descr}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.recyclerViewResultados.adapter = adapter
                adapter.updateData(
                    movimentos,
                    0.0
                ) // saldoInicial é 0.0, pois não há saldo anterior
            } catch (e: Exception) {
                Toast.makeText(
                    this@ConsultaClassificacaoActivity,
                    "Erro ao consultar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(
                    "ConsultaClassificacaoActivity",
                    "Erro em pesquisarMovimentos: ${e.message}",
                    e
                )
            }
        }
    }

    private fun generateAndSavePdf(uri: Uri) {
        val adapter = binding.recyclerViewResultados.adapter as? LancamentosAdapter
        val movimentos = adapter?.getLancamentos() ?: emptyList()
        val nomeClassificacao = binding.spinnerClassificacao.selectedItem.toString()

        if (movimentos.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para gerar o PDF!", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint().apply {
                    color = Color.BLACK
                    textSize = 10f
                    isAntiAlias = true
                }

                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val currentDate = dateFormat.format(Date())
                canvas.drawText(
                    "Consulta - Classificação - $nomeClassificacao - $currentDate",
                    40f,
                    40f,
                    paint
                )

                val columnWidths =
                    floatArrayOf(40f, 50f, 60f, 60f, 50f, 70f, 180f, 60f, 60f, 30f, 20f, 70f)
                val headers = arrayOf(
                    "Reg.", "Recurso", "Emissão", "Venc.", "Doc.", "Classif.", "Descrição",
                    "Valor", "Apresent.", "Status", "Prev.", "Saldo"
                )
                var y = 60f
                var x = 40f

                headers.forEachIndexed { index, header ->
                    canvas.drawText(header, x, y, paint)
                    x += columnWidths[index]
                }
                y += 20f

                canvas.drawLine(40f, y - 10f, 802f, y - 10f, paint)
                x = 40f
                columnWidths.forEach { width ->
                    canvas.drawLine(x, y - 10f, x, y + (movimentos.size * 20f), paint)
                    x += width
                }
                canvas.drawLine(x, y - 10f, x, y + (movimentos.size * 20f), paint)
                canvas.drawLine(
                    40f,
                    y + (movimentos.size * 20f),
                    802f,
                    y + (movimentos.size * 20f),
                    paint
                )

                movimentos.forEach { movimento ->
                    x = 40f
                    canvas.drawText(movimento.idMov.toString(), x, y, paint)
                    x += columnWidths[0]
                    canvas.drawText(movimento.recurso, x, y, paint)
                    x += columnWidths[1]
                    canvas.drawText(formatDateToDisplay(movimento.dtEmi), x, y, paint)
                    x += columnWidths[2]
                    canvas.drawText(formatDateToDisplay(movimento.dtVcto), x, y, paint)
                    x += columnWidths[3]
                    canvas.drawText(movimento.documento ?: "", x, y, paint)
                    x += columnWidths[4]
                    canvas.drawText(movimento.classif, x, y, paint)
                    x += columnWidths[5]
                    val descricao = if (movimento.Descr.length > 25) movimento.Descr.substring(
                        0,
                        25
                    ) + "..." else movimento.Descr
                    canvas.drawText(descricao, x, y, paint)
                    x += columnWidths[6]
                    canvas.drawText(decimalFormat.format(movimento.Valor), x, y, paint)
                    x += columnWidths[7]
                    canvas.drawText(formatDateToDisplay(movimento.dtApr), x, y, paint)
                    x += columnWidths[8]
                    canvas.drawText(movimento.statusMov, x, y, paint)
                    x += columnWidths[9]
                    canvas.drawText(movimento.Prev, x, y, paint)
                    x += columnWidths[10]
                    canvas.drawText(movimento.saldoAcumulado?.let { decimalFormat.format(it) }
                        ?: "", x, y, paint)
                    y += 20f
                }

                canvas.drawText("Página 1", 40f, 575f, paint)
                pdfDocument.finishPage(page)

                contentResolver.openFileDescriptor(uri, "w")?.use { descriptor ->
                    FileOutputStream(descriptor.fileDescriptor).use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                }
                pdfDocument.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ConsultaClassificacaoActivity,
                        "PDF gerado com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ConsultaClassificacaoActivity,
                        "Erro ao gerar PDF: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun formatDateToDisplay(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            displayFormat.format(dbFormat.parse(dateStr)!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    inner class DateMaskWatcher : TextWatcher {
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
}