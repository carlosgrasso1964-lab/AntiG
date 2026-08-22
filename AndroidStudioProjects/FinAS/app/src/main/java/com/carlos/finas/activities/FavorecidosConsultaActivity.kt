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
import com.carlos.finas.databinding.ActivityFavorecidosConsultaBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class FavorecidosConsultaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavorecidosConsultaBinding
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
        binding = ActivityFavorecidosConsultaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DatabaseHelper(this)

        loadFavorecidos()
        binding.edtDataIni.addTextChangedListener(DateMaskWatcher())
        binding.edtDataFim.addTextChangedListener(DateMaskWatcher())
        binding.recyclerViewResultados.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewResultados.adapter = LancamentosAdapter.Companion.create(emptyList()) { lancamento ->
            Toast.makeText(this, "Clicou em ${lancamento.Descr}", Toast.LENGTH_SHORT).show()
        }

        binding.spinnerFavorecidos.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val favorecidos = parent.tag as? List<*> ?: run {
                    Log.w("FavorecidosConsultaActivity", "Tag do spinner não é uma lista: ${parent.tag}")
                    return
                }
                if (favorecidos.isNotEmpty() && favorecidos[0] !is Pair<*, *>) {
                    Log.w("FavorecidosConsultaActivity", "Elementos do tag não são do tipo Pair: ${parent.tag}")
                    return
                }
                @Suppress("UNCHECKED_CAST")
                val typedFavorecidos = favorecidos as List<Pair<String, Pair<String, String>>>
                if (position > 0) {
                    val selected = typedFavorecidos[position]
                    binding.edtNrFav.setText(selected.second.first)
                    binding.edtFavorecidos.setText(selected.second.second)
                    binding.edtNomeCli.setText(selected.first.substringBefore(" - ${selected.second.first}"))
                } else {
                    binding.edtNrFav.setText("")
                    binding.edtFavorecidos.setText("")
                    binding.edtNomeCli.setText("")
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
            binding.edtNrFav.setText("")
            binding.edtFavorecidos.setText("")
            binding.edtNomeCli.setText("")
            binding.edtDataIni.setText("")
            binding.edtDataFim.setText("")
            binding.edtSaldoAnterior.setText("R$ 0,00")
            binding.spinnerFavorecidos.setSelection(0)
            binding.recyclerViewResultados.adapter = LancamentosAdapter.Companion.create(emptyList()) { lancamento ->
                Toast.makeText(this, "Clicou em ${lancamento.Descr}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnImprimir.setOnClickListener {
            Log.d("FavorecidosConsultaActivity", "Botão Imprimir clicado")
            val adapter = binding.recyclerViewResultados.adapter as? LancamentosAdapter
            if (adapter == null || adapter.itemCount == 0) {
                Toast.makeText(this, "Nenhum dado para gerar o PDF!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val nomeFavorecido = binding.edtNomeCli.text.toString()
            createDocument.launch("Extrato_$nomeFavorecido.pdf")
        }
    }

    private fun loadFavorecidos() {
        val favorecidos = mutableListOf<Pair<String, Pair<String, String>>>().apply {
            add(Pair("Selecione um favorecido", Pair("", "")))
            addAll(dbHelper.carregarFavorecidos())
        }
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, favorecidos.map { it.first })
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerFavorecidos.adapter = adapter
        binding.spinnerFavorecidos.tag = favorecidos
    }

    private fun pesquisarMovimentos() {
        val clifor = binding.edtNrFav.text.toString()
        val dataIni = binding.edtDataIni.text.toString()
        val dataFim = binding.edtDataFim.text.toString()
        val tipo = binding.edtFavorecidos.text.toString()

        if (clifor.isEmpty() || dataIni.isEmpty() || dataFim.isEmpty() || tipo.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        val recurso = when (tipo) {
            "CLI" -> "0022"
            "FOR" -> "0079"
            else -> {
                Toast.makeText(this, "Tipo de favorecido ($tipo) não suportado!", Toast.LENGTH_SHORT).show()
                return
            }
        }
        Log.d("FavorecidosConsulta", "pesquisarMovimentos: clifor=$clifor, tipo=$tipo, recurso=$recurso, dataIni=$dataIni, dataFim=$dataFim")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val dataIniDb = dateFormatDb.format(dateFormatInput.parse(dataIni)!!)
                val dataFimDb = dateFormatDb.format(dateFormatInput.parse(dataFim)!!)

                val (lancamentos, saldoInicial) = withContext(Dispatchers.IO) {
                    val db = dbHelper.readableDatabase
                    val query = """
                        SELECT * FROM ${DatabaseHelper.Companion.TABLE_LANCAMENTOS} 
                        WHERE dtVcto BETWEEN ? AND ? AND clifor = ? AND recurso = ? 
                        ORDER BY dtVcto
                    """.trimIndent()

                    val cursor = db.rawQuery(query, arrayOf(dataIniDb, dataFimDb, clifor, recurso))
                    val lancamentos = mutableListOf<Lancamento>()
                    cursor.use {
                        while (it.moveToNext()) {
                            val lancamento = Lancamento(
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
                                saldoAcumulado = null
                            )
                            Log.d("FavorecidosConsulta", "Carregado lancamento: idMov=${lancamento.idMov}, clifor=${lancamento.clifor}, recurso=${lancamento.recurso}, valor=${lancamento.Valor}")
                            lancamentos.add(lancamento)
                        }
                    }
                    db.close()
                    val saldoInicial = dbHelper.getSaldoByClifor(clifor, dataIniDb)
                    Pair(lancamentos, saldoInicial)
                }

                val adapter = LancamentosAdapter.Companion.create(lancamentos) { lancamento ->
                    Toast.makeText(this@FavorecidosConsultaActivity, "Clicou em ${lancamento.Descr}", Toast.LENGTH_SHORT).show()
                }
                binding.recyclerViewResultados.adapter = adapter
                adapter.updateData(lancamentos, saldoInicial)
                binding.edtSaldoAnterior.setText(decimalFormat.format(saldoInicial))
                if (lancamentos.isEmpty()) {
                    Toast.makeText(this@FavorecidosConsultaActivity, "Nenhum movimento encontrado para $clifor!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@FavorecidosConsultaActivity, "Erro ao consultar: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("FavorecidosConsulta", "Erro em pesquisarMovimentos: ${e.message}", e)
            }
        }
    }

    private fun generateAndSavePdf(uri: Uri) {
        val adapter = binding.recyclerViewResultados.adapter as? LancamentosAdapter
        val lancamentos = adapter?.getLancamentos() ?: emptyList()
        val nomeFavorecido = binding.edtNomeCli.text.toString()
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

                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val currentDate = dateFormat.format(Date())
                canvas.drawText("Consulta - Favorecidos - $nomeFavorecido - $currentDate", 40f, 40f, paint)

                val columnWidths = floatArrayOf(40f, 50f, 60f, 60f, 50f, 70f, 180f, 60f, 60f, 30f, 20f, 70f)
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
                    canvas.drawText(formatDateToDisplay(lancamento.dtEmi), x, y, paint)
                    x += columnWidths[2]
                    canvas.drawText(formatDateToDisplay(lancamento.dtVcto), x, y, paint)
                    x += columnWidths[3]
                    canvas.drawText(lancamento.documento ?: "", x, y, paint)
                    x += columnWidths[4]
                    canvas.drawText(lancamento.classif, x, y, paint)
                    x += columnWidths[5]
                    val descricao = if (lancamento.Descr.length > 25) lancamento.Descr.substring(0, 25) + "..." else lancamento.Descr
                    canvas.drawText(descricao, x, y, paint)
                    x += columnWidths[6]
                    canvas.drawText(decimalFormat.format(lancamento.Valor), x, y, paint)
                    x += columnWidths[7]
                    canvas.drawText(formatDateToDisplay(lancamento.dtApr), x, y, paint)
                    x += columnWidths[8]
                    canvas.drawText(lancamento.statusMov, x, y, paint)
                    x += columnWidths[9]
                    canvas.drawText(lancamento.Prev, x, y, paint)
                    x += columnWidths[10]
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
                    Toast.makeText(this@FavorecidosConsultaActivity, "PDF gerado com sucesso!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FavorecidosConsultaActivity, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
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
        private var isUpdating: Boolean = false
        private var current: String = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            current = s.toString()
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (isUpdating) return
            isUpdating = true

            val input = s.toString().replace("[^0-9]".toRegex(), "")
            val formatted = StringBuilder()

            if (input.length > 8) {
                s?.replace(0, s.length, current)
                isUpdating = false
                return
            }

            when {
                input.length >= 5 -> {
                    val day = input.substring(0, minOf(2, input.length))
                    val month = input.substring(2, minOf(4, input.length))
                    val year = input.substring(4, minOf(input.length, 8))
                    formatted.append(day)
                    if (day.length == 2) formatted.append("/")
                    if (month.isNotEmpty()) formatted.append(month)
                    if (month.length == 2 && input.length > 4) formatted.append("/")
                    if (year.isNotEmpty()) formatted.append(year)
                }
                input.length >= 3 -> {
                    val day = input.substring(0, minOf(2, input.length))
                    val month = input.substring(2, minOf(input.length, 4))
                    formatted.append(day)
                    if (day.length == 2) formatted.append("/")
                    if (month.isNotEmpty()) formatted.append(month)
                }
                input.length > 0 -> {
                    formatted.append(input)
                }
            }

            s?.replace(0, s.length, formatted.toString())
            isUpdating = false
        }
    }
}