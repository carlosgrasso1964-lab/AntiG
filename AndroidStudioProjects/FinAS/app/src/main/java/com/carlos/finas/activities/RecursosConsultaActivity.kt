package com.carlos.finas.activities

import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlos.finas.databinding.ActivityRecursosConsultaBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Color
import android.net.Uri
import android.widget.AdapterView
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.models.Lancamento
import com.carlos.finas.adapters.LancamentosAdapter
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.iterator

class RecursosConsultaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecursosConsultaBinding
    private lateinit var dbHelper: DatabaseHelper
    private val dateFormatInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateFormatDb = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val decimalFormat = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))

    private val createDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            CoroutineScope(Dispatchers.Main).launch {
                generateAndSavePdf(uri)
            }
        } else {
            Toast.makeText(this, "Nenhum local selecionado para salvar o PDF!", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecursosConsultaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DatabaseHelper(this)

        loadRecursos()
        binding.edtDataIni.addTextChangedListener(DateMaskWatcher())
        binding.edtDataFim.addTextChangedListener(DateMaskWatcher())
        binding.edtBxCart.addTextChangedListener(DateMaskWatcher())
        binding.recyclerViewResultados.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewResultados.adapter = LancamentosAdapter.Companion.create(emptyList()) { lancamento ->
            Toast.makeText(this, "Clicou em ${lancamento.Descr}", Toast.LENGTH_SHORT).show()
        }

        binding.spinnerRecursos.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val recursos = parent.tag as? List<*> ?: run {
                    Log.w("RecursosConsultaActivity", "Tag do spinner não é uma lista: ${parent.tag}")
                    return
                }
                if (recursos.isNotEmpty() && recursos[0] !is Pair<*, *>) {
                    Log.w("RecursosConsultaActivity", "Elementos do tag não são do tipo Pair: ${parent.tag}")
                    return
                }
                @Suppress("UNCHECKED_CAST")
                val typedRecursos = recursos as List<Pair<String, Pair<String, String>>>
                if (position > 0) {
                    val selected = typedRecursos[position]
                    binding.edtNrRecurso.setText(selected.second.first)
                    binding.edtRecurso.setText(selected.second.second)
                    binding.edtRecNome.setText(selected.first.substringBefore(" - ${selected.second.first}"))
                    binding.btnAtualizaApr.visibility = if (selected.second.second == "2.001.003") View.VISIBLE else View.GONE
                    binding.edtBxCart.visibility = if (selected.second.second == "2.001.003") View.VISIBLE else View.GONE
                } else {
                    binding.edtNrRecurso.setText("")
                    binding.edtRecurso.setText("")
                    binding.edtRecNome.setText("")
                    binding.btnAtualizaApr.visibility = View.GONE
                    binding.edtBxCart.visibility = View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
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
            pesquisarMovimentos()
        }

        binding.btnLimpar.setOnClickListener {
            binding.edtNrRecurso.setText("")
            binding.edtRecurso.setText("")
            binding.edtRecNome.setText("")
            binding.edtDataIni.setText("")
            binding.edtDataFim.setText("")
            binding.edtSaldoAnterior.setText("R$ 0,00")
            binding.edtBxCart.setText("")
            binding.spinnerRecursos.setSelection(0)
            binding.btnAtualizaApr.visibility = View.GONE
            binding.edtBxCart.visibility = View.GONE
            binding.recyclerViewResultados.adapter = LancamentosAdapter.Companion.create(emptyList()) { lancamento ->
                Toast.makeText(this, "Clicou em ${lancamento.Descr}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnImprimir.setOnClickListener {
            Log.d("RecursosConsultaActivity", "Botão Imprimir clicado")
            val adapter = binding.recyclerViewResultados.adapter as? LancamentosAdapter
            if (adapter == null || adapter.itemCount == 0) {
                Toast.makeText(this, "Nenhum dado para gerar o PDF!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val nomeRecurso = binding.edtRecNome.text.toString()
            createDocument.launch("Extrato_$nomeRecurso.pdf")
        }

        binding.btnAtualizaApr.setOnClickListener {
            atualizarBaixaCartao()
        }
    }

    private fun loadRecursos() {
        val recursos = mutableListOf<Pair<String, Pair<String, String>>>().apply {
            add(Pair("Selecione um recurso", Pair("", "")))
            addAll(dbHelper.carregarRecursos())
            addAll(dbHelper.carregarRecursosCartoes())
        }
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, recursos.map { it.first })
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerRecursos.adapter = adapter
        binding.spinnerRecursos.tag = recursos
    }

    private fun pesquisarMovimentos() {
        val recurso = binding.edtNrRecurso.text.toString()
        val dataIni = binding.edtDataIni.text.toString()
        val dataFim = binding.edtDataFim.text.toString()

        if (recurso.isEmpty() || dataIni.isEmpty() || dataFim.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val parsedDataIni = dateFormatInput.parse(dataIni) ?: throw IllegalArgumentException("Data inicial inválida: $dataIni")
                val parsedDataFim = dateFormatInput.parse(dataFim) ?: throw IllegalArgumentException("Data final inválida: $dataFim")
                val dataIniDb = dateFormatDb.format(parsedDataIni)
                val dataFimDb = dateFormatDb.format(parsedDataFim)

                val (lancamentos, saldoInicial) = withContext(Dispatchers.IO) {
                    val db = dbHelper.readableDatabase
                    val isCartao = dbHelper.isRecursoCartao(recurso)
                    val statusFilter = if (isCartao) " AND statusMov NOT IN ('RC', 'PG')" else ""
                    val query = """
                        SELECT * FROM tbmovimento 
                        WHERE dtVcto BETWEEN ? AND ? AND recurso = ? $statusFilter 
                        ORDER BY dtVcto
                    """.trimIndent()

                    val cursor = db.rawQuery(query, arrayOf(dataIniDb, dataFimDb, recurso))
                    val lancamentos = mutableListOf<Lancamento>()
                    cursor.use {
                        while (it.moveToNext()) {
                            lancamentos.add(
                                Lancamento(
                                    idMov = it.getInt(it.getColumnIndexOrThrow("idMov")),
                                    recurso = it.getString(it.getColumnIndexOrThrow("recurso"))
                                        ?: "",
                                    vrecurso = it.getString(it.getColumnIndexOrThrow("vrecurso"))
                                        ?: "",
                                    clifor = it.getString(it.getColumnIndexOrThrow("clifor")) ?: "",
                                    vCliFor = it.getString(it.getColumnIndexOrThrow("vCliFor"))
                                        ?: "",
                                    dtLancto = it.getString(it.getColumnIndexOrThrow("dtlancto"))
                                        ?: "",
                                    dtEmi = it.getString(it.getColumnIndexOrThrow("dtEmi")) ?: "",
                                    dtVcto = it.getString(it.getColumnIndexOrThrow("dtVcto")) ?: "",
                                    documento = it.getString(it.getColumnIndexOrThrow("documento")),
                                    classif = it.getString(it.getColumnIndexOrThrow("classif"))
                                        ?: "",
                                    Descr = it.getString(it.getColumnIndexOrThrow("Descr")) ?: "",
                                    Valor = it.getDouble(it.getColumnIndexOrThrow("Valor")),
                                    dtApr = it.getString(it.getColumnIndexOrThrow("dtApr")),
                                    statusMov = it.getString(it.getColumnIndexOrThrow("statusMov"))
                                        ?: "",
                                    Prev = it.getString(it.getColumnIndexOrThrow("Prev")) ?: "",
                                    saldoAcumulado = null
                                )
                            )
                        }
                    }
                    val saldoInicial = dbHelper.getSaldoByRecurso(recurso, dataIniDb)
                    Pair(lancamentos, saldoInicial)
                }

                val adapter = LancamentosAdapter.Companion.create(lancamentos) { lancamento ->
                    Toast.makeText(this@RecursosConsultaActivity, "Clicou em ${lancamento.Descr}", Toast.LENGTH_SHORT).show()
                }
                binding.recyclerViewResultados.adapter = adapter
                adapter.updateData(lancamentos, saldoInicial)
                binding.edtSaldoAnterior.setText(decimalFormat.format(saldoInicial))
            } catch (e: Exception) {
                Toast.makeText(this@RecursosConsultaActivity, "Erro ao consultar: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("RecursosConsultaActivity", "Erro em pesquisarMovimentos: ${e.message}", e)
            }
        }
    }

    private fun atualizarBaixaCartao() {
        val recurso = binding.edtNrRecurso.text.toString()
        val dataInicio = binding.edtDataIni.text.toString()
        val dataFim = binding.edtDataFim.text.toString()
        val dataBaixa = binding.edtBxCart.text.toString()

        if (recurso.isEmpty() || dataInicio.isEmpty() || dataFim.isEmpty() || dataBaixa.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val parsedDataBaixa = dateFormatInput.parse(dataBaixa) ?: throw IllegalArgumentException("Data de baixa inválida: $dataBaixa")
                val parsedDataInicio = dateFormatInput.parse(dataInicio) ?: throw IllegalArgumentException("Data inicial inválida: $dataInicio")
                val parsedDataFim = dateFormatInput.parse(dataFim) ?: throw IllegalArgumentException("Data final inválida: $dataFim")
                val dataBaixaDb = dateFormatDb.format(parsedDataBaixa)
                val dataInicioDb = dateFormatDb.format(parsedDataInicio)
                val dataFimDb = dateFormatDb.format(parsedDataFim)

                val rowsUpdated = withContext(Dispatchers.IO) {
                    dbHelper.atualizarBaixaCartao(recurso, dataInicioDb, dataFimDb, dataBaixaDb)
                }
                if (rowsUpdated > 0) {
                    Toast.makeText(this@RecursosConsultaActivity, "$rowsUpdated lançamentos baixados com sucesso!", Toast.LENGTH_SHORT).show()
                    pesquisarMovimentos()
                } else {
                    Toast.makeText(this@RecursosConsultaActivity, "Nenhum lançamento pendente encontrado.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecursosConsultaActivity, "Erro na data ou ao processar a baixa: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("RecursosConsultaActivity", "Erro em atualizarBaixaCartao: ${e.message}", e)
            }
        }
    }

    private fun generateAndSavePdf(uri: Uri) {
        val adapter = binding.recyclerViewResultados.adapter as? LancamentosAdapter
        val lancamentos = adapter?.getLancamentos() ?: emptyList()
        val nomeRecurso = binding.edtRecNome.text.toString()
        val saldoInicial = binding.edtSaldoAnterior.text.toString().replace("R$", "").trim().replace(",", ".").toDoubleOrNull() ?: 0.0

        if (lancamentos.isEmpty()) {
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

                canvas.drawText("Recursos - Extrato - $nomeRecurso", 40f, 40f, paint)

                val columnWidths = floatArrayOf(40f, 50f, 50f, 60f, 60f, 60f, 50f, 150f, 60f, 60f, 50f, 50f, 70f)
                val headers = arrayOf(
                    "Nr. Reg.", "Recurso", "Favorecido", "Lançto.", "Emissão", "Venc.", "Doc.", "Descrição",
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
                    canvas.drawLine(x, y - 10f, x, y + (lancamentos.size * 20f), paint)
                    x += width
                }
                canvas.drawLine(x, y - 10f, x, y + (lancamentos.size * 20f), paint)
                canvas.drawLine(40f, y + (lancamentos.size * 20f), 802f, y + (lancamentos.size * 20f), paint)

                lancamentos.forEach { lancamento ->
                    x = 40f
                    canvas.drawText(lancamento.idMov.toString(), x, y, paint)
                    x += columnWidths[0]
                    canvas.drawText(lancamento.recurso, x, y, paint)
                    x += columnWidths[1]
                    canvas.drawText(lancamento.clifor, x, y, paint)
                    x += columnWidths[2]
                    canvas.drawText(formatDateToDisplay(lancamento.dtLancto), x, y, paint)
                    x += columnWidths[3]
                    canvas.drawText(formatDateToDisplay(lancamento.dtEmi), x, y, paint)
                    x += columnWidths[4]
                    canvas.drawText(formatDateToDisplay(lancamento.dtVcto), x, y, paint)
                    x += columnWidths[5]
                    canvas.drawText(lancamento.documento ?: "", x, y, paint)
                    x += columnWidths[6]
                    val descricao = if (lancamento.Descr.length > 20) lancamento.Descr.substring(0, 20) + "..." else lancamento.Descr
                    canvas.drawText(descricao, x, y, paint)
                    x += columnWidths[7]
                    canvas.drawText(decimalFormat.format(lancamento.Valor), x, y, paint)
                    x += columnWidths[8]
                    canvas.drawText(formatDateToDisplay(lancamento.dtApr), x, y, paint)
                    x += columnWidths[9]
                    canvas.drawText(lancamento.statusMov, x, y, paint)
                    x += columnWidths[10]
                    canvas.drawText(lancamento.Prev, x, y, paint)
                    x += columnWidths[11]
                    canvas.drawText(lancamento.saldoAcumulado?.let { decimalFormat.format(it) } ?: "", x, y, paint)
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
                    Toast.makeText(this@RecursosConsultaActivity, "PDF gerado com sucesso!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RecursosConsultaActivity, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("RecursosConsultaActivity", "Erro ao gerar PDF: ${e.message}", e)
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
}

class DateMaskWatcher : TextWatcher {
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