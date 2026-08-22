package com.carlos.finas.activities

import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.adapters.SaldosAdapter
import com.carlos.finas.databinding.ActivityConsultaSaldosBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.forEach

data class Saldo(
    val principal: String?,
    val subprincipal: String?,
    val conta: String?,
    val fonte: String?,
    val vrecurso: String?,
    val recurso: String?,
    val saldo: Double,
    val acumulado: Double?
)

class ConsultaSaldosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsultaSaldosBinding
    private lateinit var dbHelper: DatabaseHelper
    private val decimalFormat = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss", Locale.getDefault())

    private val createDocument = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            CoroutineScope(Dispatchers.Main).launch {
                generateAndSavePdf(uri)
            }
        } else {
            Toast.makeText(this, "Nenhum local selecionado para salvar o PDF!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsultaSaldosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = DatabaseHelper(this)

        // Garantir que as views existam ao inicializar
        dbHelper.ensureViewsExist()

        // Exibir data/hora do sistema
        binding.txtDataHora.text = dateFormat.format(Date())

        // Configurar RecyclerView
        binding.recyclerViewResultados.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewResultados.adapter = SaldosAdapter.Companion.create(emptyList()) { saldo ->
            Toast.makeText(this, "Clicou em ${saldo.fonte}", Toast.LENGTH_SHORT).show()
        }

        // Configurar listeners dos botões
        binding.btnPesquisar.setOnClickListener {
            pesquisarSaldos()
        }

        binding.btnLimpar.setOnClickListener {
            binding.recyclerViewResultados.adapter = SaldosAdapter.Companion.create(emptyList()) { saldo ->
                Toast.makeText(this, "Clicou em ${saldo.fonte}", Toast.LENGTH_SHORT).show()
            }
            binding.txtDataHora.text = dateFormat.format(Date())
        }

        binding.btnImprimir.setOnClickListener {
            Log.d("ConsultaSaldosActivity", "Botão Imprimir clicado")
            val adapter = binding.recyclerViewResultados.adapter as? SaldosAdapter
            if (adapter == null || adapter.itemCount == 0) {
                Toast.makeText(this, "Nenhum dado para gerar o PDF!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createDocument.launch("Consulta_Saldos_${dateFormat.format(Date()).replace(":", "-")}.pdf")
        }

        binding.btnFechar.setOnClickListener {
            finish()
        }
    }

    private fun pesquisarSaldos() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Garantir que as views existam antes da consulta
                dbHelper.ensureViewsExist()

                val saldos = withContext(Dispatchers.IO) {
                    val db = dbHelper.readableDatabase
                    val query = """
                        SELECT principal, subprincipal, conta, Fonte, vrecurso, recurso, saldos 
                        FROM vsaldos 
                        WHERE recurso NOT IN (
                            SELECT DISTINCT recurso FROM view_movimento 
                            WHERE classif IN ('2.001.004', '2.002.001', '4.008.011') 
                            AND Prev <> 'F'
                        )
                    """.trimIndent()
                    val cursor = db.rawQuery(query, null)
                    val saldosList = mutableListOf<Saldo>()
                    var acumulado = 0.0

                    cursor.use {
                        while (it.moveToNext()) {
                            val saldo = it.getDouble(it.getColumnIndexOrThrow("saldos"))
                            acumulado += saldo

                            saldosList.add(
                                Saldo(
                                    principal = it.getString(it.getColumnIndexOrThrow("principal")),
                                    subprincipal = it.getString(it.getColumnIndexOrThrow("subprincipal")),
                                    conta = it.getString(it.getColumnIndexOrThrow("conta")),
                                    fonte = it.getString(it.getColumnIndexOrThrow("Fonte")),
                                    vrecurso = it.getString(it.getColumnIndexOrThrow("vrecurso")),
                                    recurso = it.getString(it.getColumnIndexOrThrow("recurso")),
                                    saldo = saldo,
                                    acumulado = acumulado
                                )
                            )
                        }
                    }
                    
                    val queryEmprestimos = """
                        SELECT (SUM(CASE WHEN classif = '3.003.005' AND dtApr IS NOT NULL THEN Valor ELSE 0 END) + 
                               SUM(CASE WHEN classif = '4.008.011' AND dtApr IS NOT NULL THEN Valor ELSE 0 END)) * -1 AS saldo_emprestimos 
                        FROM view_movimento 
                        WHERE Prev <> 'F'
                    """.trimIndent()
                    val cursorEmp = db.rawQuery(queryEmprestimos, null)
                    var saldoEmprestimos = 0.0
                    cursorEmp.use {
                        if (it.moveToFirst()) {
                            saldoEmprestimos = it.getDouble(it.getColumnIndexOrThrow("saldo_emprestimos"))
                        }
                    }
                    acumulado += saldoEmprestimos
                    saldosList.add(
                        Saldo(
                            principal = "",
                            subprincipal = "",
                            conta = "",
                            fonte = "Empréstimos (saldo)",
                            vrecurso = "",
                            recurso = "",
                            saldo = saldoEmprestimos,
                            acumulado = acumulado
                        )
                    )

                    db.close()
                    saldosList
                }

                val adapter = SaldosAdapter.Companion.create(saldos) { saldo ->
                    Toast.makeText(this@ConsultaSaldosActivity, "Clicou em ${saldo.fonte}", Toast.LENGTH_SHORT).show()
                }
                binding.recyclerViewResultados.adapter = adapter
                binding.txtDataHora.text = dateFormat.format(Date())
            } catch (e: Exception) {
                Toast.makeText(this@ConsultaSaldosActivity, "Erro ao consultar: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("ConsultaSaldosActivity", "Erro em pesquisarSaldos: ${e.message}", e)
            }
        }
    }

    private fun generateAndSavePdf(uri: Uri) {
        val adapter = binding.recyclerViewResultados.adapter as? SaldosAdapter
        val saldos = adapter?.getSaldos() ?: emptyList()

        if (saldos.isEmpty()) {
            Toast.makeText(this, "Nenhum dado para gerar o PDF!", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 retrato
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint().apply {
                    color = Color.BLACK
                    textSize = 10f
                    isAntiAlias = true
                }

                // Cabeçalho
                val currentDate = dateFormat.format(Date())
                canvas.drawText("Relatório de Consulta - Saldos - $currentDate", 40f, 40f, paint)

                // Tabela
                val columnWidths = floatArrayOf(200f, 150f, 150f)
                val headers = arrayOf("Banco", "Saldo", "Acumulado")
                var y = 60f
                var x = 40f

                // Desenhar cabeçalhos
                headers.forEachIndexed { index, header ->
                    canvas.drawText(header, x, y, paint)
                    x += columnWidths[index]
                }
                y += 20f

                // Desenhar linhas da tabela
                canvas.drawLine(40f, y - 10f, 555f, y - 10f, paint)
                x = 40f
                columnWidths.forEach { width ->
                    canvas.drawLine(x, y - 10f, x, y + (saldos.size * 20f), paint)
                    x += width
                }
                canvas.drawLine(x, y - 10f, x, y + (saldos.size * 20f), paint)
                canvas.drawLine(40f, y + (saldos.size * 20f), 555f, y + (saldos.size * 20f), paint)

                // Desenhar dados
                saldos.forEach { saldo ->
                    x = 40f
                    canvas.drawText(saldo.fonte ?: "-", x, y, paint)
                    x += columnWidths[0]
                    canvas.drawText(decimalFormat.format(saldo.saldo), x, y, paint)
                    x += columnWidths[1]
                    canvas.drawText(saldo.acumulado?.let { decimalFormat.format(it) } ?: "-", x, y, paint)
                    y += 20f
                }

                // Rodapé
                canvas.drawText("Página 1", 40f, 822f, paint)
                pdfDocument.finishPage(page)

                // Salvar PDF
                contentResolver.openFileDescriptor(uri, "w")?.use { descriptor ->
                    FileOutputStream(descriptor.fileDescriptor).use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                }
                pdfDocument.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ConsultaSaldosActivity, "PDF gerado com sucesso!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ConsultaSaldosActivity, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}