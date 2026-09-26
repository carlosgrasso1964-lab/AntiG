package com.carlos.finas.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.R
import com.carlos.finas.databinding.ActivityGerarLancamentosBinding
import com.carlos.finas.models.Clifor
import com.carlos.finas.models.PlanoContas
import com.carlos.finas.models.Recurso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class GerarLancamentosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGerarLancamentosBinding
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var recursos: List<Recurso>
    private lateinit var clifors: List<Clifor>
    private lateinit var planosContas: List<PlanoContas>

    private var selectedRecurso: Recurso? = null
    private var selectedClifor: Clifor? = null
    private var selectedClassificacao: PlanoContas? = null

    private val itensAdicionados = mutableListOf<LancamentoItem>()
    private lateinit var adapter: LancamentoItemAdapter

    // Data class to hold items to be exported
    data class LancamentoItem(
        val tipo: String,         // "E" or "S"
        val recursoCodigo: String,
        val recursoNome: String,
        val recursoFkGpprinc: String?,  // fk_gpprinc do recurso (detecta cartão)
        val cliforCodigo: String,
        val cliforNome: String,
        val dtLancto: String,
        val dtEmi: String,
        val dtVcto: String,
        val documento: String,
        val classifCodigo: String,
        val descricao: String,
        val valor: Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGerarLancamentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                initializeData()
                setupDefaultDates()
                setupSpinners()
                setupRecyclerView()
                setupButtons()
            } catch (e: Exception) {
                Toast.makeText(
                    this@GerarLancamentosActivity,
                    "Erro ao carregar dados: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private suspend fun initializeData() {
        recursos = withContext(Dispatchers.IO) { dbHelper.getAllRecursos() }
        clifors = withContext(Dispatchers.IO) { dbHelper.getAllClifors() }
        planosContas = withContext(Dispatchers.IO) { dbHelper.getAllPlanosContas() }
    }

    private fun setupDefaultDates() {
        val today = LocalDate.now()
        val todayPlus30 = today.plusDays(30)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        binding.edtDtLancto.setText(today.format(formatter))
        binding.edtDtEmi.setText(today.format(formatter))
        binding.edtDtVcto.setText(todayPlus30.format(formatter))
    }

    private fun setupSpinners() {
        // --- Tipo Spinner ---
        val tipoOptions = listOf("Entrada (E)", "Saída (S)")
        binding.spinnerTipo.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item_dark,
            tipoOptions
        ).apply {
            setDropDownViewResource(R.layout.spinner_item_dark)
        }
        binding.spinnerTipo.setSelection(0)

        // --- Recurso Spinner ---
        val recursoLabels = mutableListOf("Selecione o recurso")
        recursoLabels.addAll(recursos.map { "${it.codigo} - ${it.nomebco}" })
        binding.spinnerRecurso.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item_dark,
            recursoLabels
        ).apply {
            setDropDownViewResource(R.layout.spinner_item_dark)
        }
        binding.spinnerRecurso.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedRecurso = if (position > 0) recursos[position - 1] else null
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // --- Favorecido Spinner ---
        val favLabels = mutableListOf("Selecione o favorecido")
        favLabels.addAll(clifors.map { "${it.codCliFor} - ${it.nomeCliFor}" })
        binding.spinnerFavorecido.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item_dark,
            favLabels
        ).apply {
            setDropDownViewResource(R.layout.spinner_item_dark)
        }
        binding.spinnerFavorecido.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedClifor = if (position > 0) clifors[position - 1] else null
                // Auto-select classificação based on fkCliForGp
                autoSelectClassificacao()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // --- Classificação Spinner ---
        val classifLabels = mutableListOf("Selecione a classificação")
        classifLabels.addAll(planosContas.map { "${it.cod_Geral} - ${it.nome_C}" })
        binding.spinnerClassificacao.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item_dark,
            classifLabels
        ).apply {
            setDropDownViewResource(R.layout.spinner_item_dark)
        }
        binding.spinnerClassificacao.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedClassificacao = if (position > 0) planosContas[position - 1] else null
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun autoSelectClassificacao() {
        val clifor = selectedClifor ?: return
        val fkGp = clifor.fkCliForGp ?: return

        // Find the matching PlanoContas by cod_Geral
        val index = planosContas.indexOfFirst { it.cod_Geral == fkGp }
        if (index >= 0) {
            // +1 because position 0 is the prompt
            binding.spinnerClassificacao.setSelection(index + 1)
        }
    }

    private fun setupRecyclerView() {
        adapter = LancamentoItemAdapter(itensAdicionados) { position ->
            itensAdicionados.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, itensAdicionados.size)
        }
        binding.rvLancamentos.layoutManager = LinearLayoutManager(this)
        binding.rvLancamentos.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnAdicionar.setOnClickListener {
            adicionarLancamento()
        }

        binding.btnExportar.setOnClickListener {
            exportarSql()
        }

        binding.btnInserirSqlite.setOnClickListener {
            inserirNoSqlite()
        }

        binding.btnLimpar.setOnClickListener {
            itensAdicionados.clear()
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Lista limpa", Toast.LENGTH_SHORT).show()
        }

        binding.btnFechar.setOnClickListener {
            finish()
        }
    }

    private fun adicionarLancamento() {
        // Validate fields
        val recurso = selectedRecurso
        if (recurso == null) {
            Toast.makeText(this, "Selecione um recurso", Toast.LENGTH_SHORT).show()
            return
        }

        val clifor = selectedClifor
        if (clifor == null) {
            Toast.makeText(this, "Selecione um favorecido", Toast.LENGTH_SHORT).show()
            return
        }

        val classificacao = selectedClassificacao
        if (classificacao == null) {
            Toast.makeText(this, "Selecione uma classificação", Toast.LENGTH_SHORT).show()
            return
        }

        val dtLancto = binding.edtDtLancto.text.toString().trim()
        if (dtLancto.isEmpty()) {
            Toast.makeText(this, "Preencha a Data de Lançamento", Toast.LENGTH_SHORT).show()
            return
        }

        val dtEmi = binding.edtDtEmi.text.toString().trim()
        if (dtEmi.isEmpty()) {
            Toast.makeText(this, "Preencha a Data de Emissão", Toast.LENGTH_SHORT).show()
            return
        }

        val dtVcto = binding.edtDtVcto.text.toString().trim()
        if (dtVcto.isEmpty()) {
            Toast.makeText(this, "Preencha a Data de Vencimento", Toast.LENGTH_SHORT).show()
            return
        }

        val valorText = binding.edtValor.text.toString().trim()
        if (valorText.isEmpty()) {
            Toast.makeText(this, "Preencha o Valor", Toast.LENGTH_SHORT).show()
            return
        }
        val valor = valorText.replace(",", ".").toDoubleOrNull()
        if (valor == null || valor <= 0) {
            Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show()
            return
        }

        // Determine tipo code
        val tipoSelection = binding.spinnerTipo.selectedItemPosition
        val tipoCodigo = if (tipoSelection == 0) "E" else "S"

        val documento = binding.edtDocumento.text.toString().trim()
        val descricao = binding.edtDescricao.text.toString().trim()

        val item = LancamentoItem(
            tipo = tipoCodigo,
            recursoCodigo = recurso.codigo,
            recursoNome = recurso.nomebco ?: "",
            recursoFkGpprinc = recurso.fk_gpprinc,
            cliforCodigo = clifor.codCliFor,
            cliforNome = clifor.nomeCliFor,
            dtLancto = dtLancto,
            dtEmi = dtEmi,
            dtVcto = dtVcto,
            documento = documento,
            classifCodigo = classificacao.cod_Geral,
            descricao = descricao,
            valor = valor
        )

        itensAdicionados.add(item)
        adapter.notifyItemInserted(itensAdicionados.size - 1)

        // Scroll RecyclerView to bottom
        binding.rvLancamentos.scrollToPosition(itensAdicionados.size - 1)

        // Clear description and valor for next entry
        binding.edtDescricao.setText("")
        binding.edtValor.setText("")

        Toast.makeText(this, "Lançamento adicionado (${itensAdicionados.size})", Toast.LENGTH_SHORT).show()
    }

    private fun inserirNoSqlite() {
        if (itensAdicionados.isEmpty()) {
            Toast.makeText(this, "Nenhum lançamento para inserir", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            var inseridos = 0
            val erros = mutableListOf<String>()

            for ((index, item) in itensAdicionados.withIndex()) {
                try {
                    // Garantir que o clifor existe no banco
                    val cliforExiste = dbHelper.cliforExists(item.cliforCodigo)
                    if (!cliforExiste) {
                        val fkGpPrinc = if (item.tipo == "E") "0022" else "0079"
                        val tipo = if (item.tipo == "E") "CLI" else "FOR"
                        val novoClifor = com.carlos.finas.models.Clifor(
                            codCliFor = item.cliforCodigo,
                            Tipo = tipo,
                            nomeCliFor = item.cliforNome,
                            apelidoCliFor = item.cliforNome,
                            email = "", celular = "", telefone = "",
                            cep = "", endereco = "", numero = 0,
                            complemento = "", bairro = "", cidade = "", estado = "",
                            rg = "", cpf = "", contatoCliFor = "", obs = "",
                            fkCliForGp = fkGpPrinc
                        )
                        dbHelper.insertClifor(novoClifor)
                    }

                    val isCartao = item.recursoFkGpprinc?.trim() == "2.001.003"

                    if (isCartao) {
                        // 1º Contas a Pagar (menor ID, negativo)
                        val mainLancamento = com.carlos.finas.models.Lancamento(
                            idMov = 0,
                            recurso = "0079",
                            vrecurso = "2.001.003",
                            clifor = item.cliforCodigo,
                            vCliFor = item.classifCodigo,
                            dtLancto = item.dtLancto,
                            dtEmi = item.dtEmi,
                            dtVcto = item.dtVcto,
                            documento = item.documento,
                            classif = item.classifCodigo,
                            Descr = item.descricao,
                            Valor = -Math.abs(item.valor),
                            dtApr = item.dtVcto,
                            statusMov = "PG",
                            Prev = "V"
                        )
                        dbHelper.insertLancamento(mainLancamento)

                        // 2º Baixa no favorecido (meio)
                        val baixaLancamento = com.carlos.finas.models.Lancamento(
                            idMov = 0,
                            recurso = "0079",
                            vrecurso = "2.001.003",
                            clifor = item.cliforCodigo,
                            vCliFor = item.classifCodigo,
                            dtLancto = item.dtLancto,
                            dtEmi = item.dtEmi,
                            dtVcto = item.dtVcto,
                            documento = item.documento,
                            classif = "9.001.003",
                            Descr = item.descricao,
                            Valor = Math.abs(item.valor),
                            dtApr = item.dtVcto,
                            statusMov = "PG",
                            Prev = "V"
                        )
                        dbHelper.insertLancamento(baixaLancamento)

                        // 3º Lançamento real no cartão (maior ID)
                        val cartaoLancamento = com.carlos.finas.models.Lancamento(
                            idMov = 0,
                            recurso = item.recursoCodigo,
                            vrecurso = item.recursoFkGpprinc ?: "",
                            clifor = item.cliforCodigo,
                            vCliFor = item.classifCodigo,
                            dtLancto = item.dtLancto,
                            dtEmi = item.dtEmi,
                            dtVcto = item.dtVcto,
                            documento = item.documento,
                            classif = "9.001.003",
                            Descr = item.descricao,
                            Valor = -Math.abs(item.valor),
                            dtApr = null,
                            statusMov = "",
                            Prev = "V"
                        )
                        dbHelper.insertLancamento(cartaoLancamento)
                    } else {
                        // Lançamento simples (não é cartão)
                        val lancamento = com.carlos.finas.models.Lancamento(
                            idMov = 0,
                            recurso = item.recursoCodigo,
                            vrecurso = item.recursoFkGpprinc ?: "",
                            clifor = item.cliforCodigo,
                            vCliFor = item.classifCodigo,
                            dtLancto = item.dtLancto,
                            dtEmi = item.dtEmi,
                            dtVcto = item.dtVcto,
                            documento = item.documento,
                            classif = item.classifCodigo,
                            Descr = item.descricao,
                            Valor = if (item.tipo == "S") -Math.abs(item.valor) else Math.abs(item.valor),
                            dtApr = null,
                            statusMov = "",
                            Prev = "N"
                        )
                        dbHelper.insertLancamento(lancamento)
                    }

                    inseridos++
                } catch (e: Exception) {
                    erros.add("Item ${index + 1}: ${e.message}")
                }
            }

            withContext(Dispatchers.Main) {
                if (erros.isEmpty()) {
                    Toast.makeText(
                        this@GerarLancamentosActivity,
                        "$inseridos lançamento(s) inserido(s) com sucesso!",
                        Toast.LENGTH_LONG
                    ).show()
                    itensAdicionados.clear()
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        this@GerarLancamentosActivity,
                        "$inseridos inseridos, ${erros.size} erro(s): ${erros.take(3).joinToString("; ")}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun exportarSql() {
        if (itensAdicionados.isEmpty()) {
            Toast.makeText(this, "Nenhum lançamento para exportar", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val sqlContent = buildSqlStatements().toString()

                // Create file in app's cache directory
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
                val fileName = "lancamentos_$timestamp.sql"
                val file = File(cacheDir, fileName)
                file.writeText(sqlContent)

                withContext(Dispatchers.Main) {
                    shareFile(file, fileName)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@GerarLancamentosActivity,
                        "Erro ao exportar: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun buildSqlStatements(): StringBuilder {
        val sb = StringBuilder()
        sb.appendLine("-- Lançamentos gerados pelo FinAS")
        sb.appendLine()

        for (item in itensAdicionados) {
            val escapedDescr = item.descricao.replace("'", "''")
            val escapedDoc = item.documento.replace("'", "''")
            val isCartao = item.recursoFkGpprinc == "2.001.003"

            if (isCartao && item.tipo == "S") {
                // === COMPRA COM CARTÃO: 3 lançamentos ===

                // 1) Contas a Pagar (negativo, PG, prev V)
                sb.appendLine(
                    "INSERT INTO tbmovimento (recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,Valor,dtApr,statusMov,Prev) " +
                    "VALUES ('0079','2.001.003','${item.cliforCodigo}','${item.classifCodigo}','${item.dtLancto}','${item.dtEmi}','${item.dtVcto}','${escapedDoc}','${item.classifCodigo}','${escapedDescr}',-${String.format(Locale.US, "%.2f", item.valor)},'${item.dtVcto}','PG','V');"
                )

                // 2) Baixa do Contas a Pagar (positivo, PG, prev V, classif 9.001.003)
                sb.appendLine(
                    "INSERT INTO tbmovimento (recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,Valor,dtApr,statusMov,Prev) " +
                    "VALUES ('0079','2.001.003','${item.cliforCodigo}','${item.classifCodigo}','${item.dtLancto}','${item.dtEmi}','${item.dtVcto}','${escapedDoc}','9.001.003','Baixa de: $escapedDescr -PG-',${String.format(Locale.US, "%.2f", item.valor)},'${item.dtVcto}','PG','V');"
                )

                // 3) Lançamento no cartão (negativo, vazio, prev V, classif 9.001.003)
                sb.appendLine(
                    "INSERT INTO tbmovimento (recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,Valor,dtApr,statusMov,Prev) " +
                    "VALUES ('${item.recursoCodigo}','${item.recursoFkGpprinc}','${item.cliforCodigo}','${item.classifCodigo}','${item.dtLancto}','${item.dtEmi}','${item.dtVcto}','${escapedDoc}','9.001.003','Pagto. de: $escapedDescr -PG-',-${String.format(Locale.US, "%.2f", item.valor)},NULL,'','V');"
                )
            } else {
                // === LANÇAMENTO NORMAL (1 INSERT) ===
                sb.appendLine(
                    "INSERT INTO tbmovimento (recurso,vrecurso,clifor,vCliFor,dtlancto,dtEmi,dtVcto,documento,classif,Descr,Valor,dtApr,statusMov,Prev) " +
                    "VALUES ('${item.recursoCodigo}','${item.recursoFkGpprinc}','${item.cliforCodigo}','${item.classifCodigo}','${item.dtLancto}','${item.dtEmi}','${item.dtVcto}','${escapedDoc}','${item.classifCodigo}','${escapedDescr}',${String.format(Locale.US, "%.2f", item.valor)},NULL,'A','N');"
                )
            }
        }

        return sb
    }

    private fun shareFile(file: File, fileName: String) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Lançamentos SQL - $fileName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Compartilhar SQL"))
        } catch (e: IllegalArgumentException) {
            // FileProvider not configured — fallback: use file:// URI
            try {
                val uri = android.net.Uri.fromFile(file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Lançamentos SQL - $fileName")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Compartilhar SQL"))
            } catch (e2: Exception) {
                Toast.makeText(this, "Erro ao compartilhar: ${e2.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // --- RecyclerView Adapter ---
    inner class LancamentoItemAdapter(
        private val items: List<LancamentoItem>,
        private val onDelete: (Int) -> Unit
    ) : RecyclerView.Adapter<LancamentoItemAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTipo: TextView = itemView.findViewById(R.id.tvItemTipo)
            val tvData: TextView = itemView.findViewById(R.id.tvItemData)
            val tvVcto: TextView = itemView.findViewById(R.id.tvItemVcto)
            val tvFavorecido: TextView = itemView.findViewById(R.id.tvItemFavorecido)
            val tvDoc: TextView = itemView.findViewById(R.id.tvItemDoc)
            val tvClassif: TextView = itemView.findViewById(R.id.tvItemClassif)
            val tvValor: TextView = itemView.findViewById(R.id.tvItemValor)
            val btnDelete: ImageButton = itemView.findViewById(R.id.btnItemDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_gerar_lancamento, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]

            holder.tvTipo.text = if (item.tipo == "E") "E" else "S"
            holder.tvTipo.setTextColor(
                if (item.tipo == "E")
                    android.graphics.Color.parseColor("#10B981") // green for Entrada
                else
                    android.graphics.Color.parseColor("#EF4444") // red for Saída
            )
            holder.tvData.text = item.dtLancto
            holder.tvVcto.text = item.dtVcto
            holder.tvFavorecido.text = item.cliforNome
            holder.tvDoc.text = item.documento
            holder.tvClassif.text = item.classifCodigo
            holder.tvValor.text = String.format(Locale.US, "%.2f", item.valor)

            holder.btnDelete.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onDelete(pos)
                }
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
