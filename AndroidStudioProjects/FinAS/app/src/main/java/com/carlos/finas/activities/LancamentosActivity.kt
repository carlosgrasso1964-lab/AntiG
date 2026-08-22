package com.carlos.finas.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.models.Clifor
import com.carlos.finas.databinding.ActivityLancamentosBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.models.Lancamento
import com.carlos.finas.adapters.LancamentosAdapter
import com.carlos.finas.models.PlanoContas
import com.carlos.finas.R
import com.carlos.finas.models.Recurso
import java.util.Date

class LancamentosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLancamentosBinding
    private lateinit var adapter: LancamentosAdapter
    private lateinit var dbHelper: DatabaseHelper
    private var selectedRecursoCodigo: String? = null
    private var selectedCliforCodigo: String? = null
    private var selectedClassifCodigo: String? = null
    private var selectedTipoOperacao: String? = null
    private var currentIdMov: Int = 0
    private var valorTextWatcher: TextWatcher? = null
    private var isLoading = false
    private var lastLoadedCount = 0
    private var offset = 0
    private val pageSize = 50
    private var isRecursoCartao = false
    private var isEditing = false
    private var saldoInicial = 0.0
    private lateinit var recursos: List<Recurso>
    private lateinit var clifors: List<Clifor>
    private lateinit var classifs: List<PlanoContas>
    private lateinit var recursosAtivos: List<Recurso>
    private var lastUpdateTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        System.setProperty("jankmonitor.enabled", "false")
        super.onCreate(savedInstanceState)
        binding = ActivityLancamentosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("LancamentosActivity", "Locale atual: ${Locale.getDefault().toString()}")

        dbHelper = DatabaseHelper(this)
        binding.rvLancamentos.visibility = View.GONE
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                initializeData()
                setupRecyclerView()
                setupButtons()
                setupValorField()
                setupDateFields()
                setupSpinners()
                updateLancamentos()
                debugDatabase()
                binding.rvLancamentos.visibility = View.VISIBLE
            } catch (e: Exception) {
                Log.e("LancamentosActivity", "Erro ao inicializar dados: ${e.message}")
                Toast.makeText(
                    this@LancamentosActivity,
                    "Erro ao carregar dados: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.scrollView.post {
            binding.scrollView.scrollTo(0, 0)
        }
    }

    private suspend fun initializeData() {
        recursos = withContext(Dispatchers.IO) { dbHelper.getAllRecursos() }
        clifors = withContext(Dispatchers.IO) {
            when (selectedTipoOperacao) {
                "Entrada" -> dbHelper.getClientes()
                "Saída" -> dbHelper.getFornecedores()
                else -> dbHelper.getAllClifors()
            }
        }
        classifs = withContext(Dispatchers.IO) {
            dbHelper.getAllPlanosContas()
                .filter { plano ->
                    val name = plano.nome_C
                    !name.isNullOrBlank() && name.any { it.isLowerCase() }
                }
                .sortedWith(compareBy { it.nome_C?.lowercase(Locale.getDefault()) ?: "" })
        }
        recursosAtivos = withContext(Dispatchers.IO) { dbHelper.getRecursosAtivos() }
    }

    fun refreshData() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                dbHelper = DatabaseHelper(this@LancamentosActivity)
                selectedTipoOperacao = binding.spinnerTipoOperacao.selectedItem?.toString()
                    .takeIf { it != getString(R.string.prompt_tipo_operacao) }
                initializeData()
                setupSpinners()
                selectedRecursoCodigo = null
                selectedCliforCodigo = null
                selectedClassifCodigo = null
                isRecursoCartao = false
                offset = 0
                lastLoadedCount = 0
                saldoInicial = 0.0
                updateLancamentos()
                Toast.makeText(
                    this@LancamentosActivity,
                    "Dados recarregados com sucesso",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("LancamentosActivity", "Erro ao recarregar dados: ${e.message}")
                Toast.makeText(
                    this@LancamentosActivity,
                    "Erro ao recarregar dados: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = LancamentosAdapter.Companion.create(emptyList()) { lancamento ->
            loadLancamentoForEdit(lancamento)
            binding.scrollView.post {
                binding.scrollView.scrollTo(0, 0)
            }
        }
        binding.rvLancamentos.layoutManager = LinearLayoutManager(this)
        binding.rvLancamentos.adapter = adapter
        binding.rvLancamentos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var lastCallTime = 0L
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastCallTime < 500) return
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (!isLoading && totalItemCount <= (lastVisibleItem + 5) && lastLoadedCount >= pageSize) {
                    lastCallTime = currentTime
                    loadMoreLancamentos()
                }
            }
        })
    }

    private fun loadMoreLancamentos() {
        if (isLoading) return
        isLoading = true
        val sixtyDaysAgo = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -60) }.time
        )
        val startDate = formatDate(binding.edtDtEmi.text.toString()).takeIf { it.isNotEmpty() }
            ?: sixtyDaysAgo
        
        val dtLanctoStr = formatDate(binding.edtDtLancto.text.toString())
        val dtEmiStr = formatDate(binding.edtDtEmi.text.toString())
        val isDateFilterMode = dtLanctoStr.isNotEmpty() || dtEmiStr.isNotEmpty()

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val (newLancamentos, saldo) = withContext(Dispatchers.IO) {
                    if (isDateFilterMode) {
                        dbHelper.getLancamentosFiltered(
                            dtLancto = dtLanctoStr.takeIf { it.isNotEmpty() },
                            dtEmi = dtEmiStr.takeIf { it.isNotEmpty() },
                            recurso = selectedRecursoCodigo,
                            clifor = selectedCliforCodigo,
                            isRecursoCartao = isRecursoCartao,
                            offset = offset,
                            pageSize = pageSize
                        )
                    } else {
                        when (selectedRecursoCodigo) {
                            "0401" -> dbHelper.getLancamentosByClifor("0022", offset, pageSize)
                            "0402" -> dbHelper.getLancamentosByClifor("0079", offset, pageSize)
                            null -> Pair(
                                dbHelper.getLancamentosPendentesWithOffset(
                                    sixtyDaysAgo,
                                    offset,
                                    pageSize
                                ), 0.0
                            )
                            else -> if (isRecursoCartao) {
                                dbHelper.getLancamentosCartao(offset, pageSize)
                            } else {
                                dbHelper.getLancamentosByRecurso(
                                    selectedRecursoCodigo!!,
                                    startDate,
                                    offset,
                                    pageSize
                                )
                            }
                        }
                    }
                }
                lastLoadedCount = newLancamentos.size
                saldoInicial = saldo
                adapter.addData(newLancamentos, saldoInicial)
                offset += newLancamentos.size
                isLoading = false
                Log.d(
                    "LancamentosActivity",
                    "Carregados mais $lastLoadedCount lançamentos (Recurso: $selectedRecursoCodigo), offset agora é $offset, saldo inicial: $saldoInicial"
                )
            } catch (e: Exception) {
                Log.e("LancamentosActivity", "Erro ao carregar mais lançamentos: ${e.message}")
                Toast.makeText(
                    this@LancamentosActivity,
                    "Erro ao carregar lançamentos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                isLoading = false
            }
        }
    }

    private fun setupSpinners() {
        // Spinner Tipo Operação
        val tipoOperacaoOptions = listOf(getString(R.string.prompt_tipo_operacao), "Entrada", "Saída", "Nulo")
        binding.spinnerTipoOperacao.adapter = ArrayAdapter(
            this@LancamentosActivity,
            android.R.layout.simple_spinner_item,
            tipoOperacaoOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerTipoOperacao.prompt = getString(R.string.prompt_tipo_operacao)
        binding.spinnerTipoOperacao.setSelection(0) // Seleciona o prompt
        binding.spinnerTipoOperacao.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0 || isEditing) return
                val novoTipo = tipoOperacaoOptions[position]
                if (novoTipo != selectedTipoOperacao) {
                    selectedTipoOperacao = novoTipo
                    selectedCliforCodigo = null
                    lifecycleScope.launch(Dispatchers.Main) {
                        clifors = withContext(Dispatchers.IO) {
                            when (selectedTipoOperacao) {
                                "Entrada" -> dbHelper.getClientes()
                                "Saída" -> dbHelper.getFornecedores()
                                else -> dbHelper.getAllClifors()
                            }
                        }
                        updateSpinnerClifor()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        })

        // Spinner Recurso
        val recursoPrompt = getString(R.string.prompt_recurso)
        val recursoNames = listOf(recursoPrompt) + recursos.map { it.nomebco ?: it.codigo }
        binding.spinnerRecurso.adapter = ArrayAdapter(
            this@LancamentosActivity,
            android.R.layout.simple_spinner_item,
            recursoNames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerRecurso.prompt = recursoPrompt
        binding.spinnerRecurso.setSelection(0) // Seleciona o prompt
        binding.spinnerRecurso.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isEditing) return
                if (position == 0) {
                    selectedRecursoCodigo = null
                    isRecursoCartao = false
                    return
                }
                selectedRecursoCodigo = recursos.getOrNull(position - 1)?.codigo
                lifecycleScope.launch {
                    try {
                        isRecursoCartao = selectedRecursoCodigo?.let {
                            withContext(Dispatchers.IO) {
                                dbHelper.isRecursoCartao(it)
                            }
                        } ?: false
                        Log.d(
                            "LancamentosActivity",
                            "Recurso selecionado: $selectedRecursoCodigo, isCartao=$isRecursoCartao"
                        )
                        updateLancamentos()
                    } catch (e: Exception) {
                        Log.e("LancamentosActivity", "Erro ao verificar recurso: ${e.message}")
                        Toast.makeText(
                            this@LancamentosActivity,
                            "Erro ao verificar recurso: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedRecursoCodigo = null
                isRecursoCartao = false
            }
        })

        // Spinner Clifor
        val cliforPrompt = getString(R.string.prompt_clifor)
        val cliforNames = listOf(cliforPrompt) + clifors.map { "${it.nomeCliFor} (${it.codCliFor})" }
        binding.spinnerClifor.adapter = ArrayAdapter(
            this@LancamentosActivity,
            android.R.layout.simple_spinner_item,
            cliforNames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerClifor.prompt = cliforPrompt
        binding.spinnerClifor.setSelection(0) // Seleciona o prompt
        binding.spinnerClifor.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (isEditing) return
                if (position == 0) {
                    selectedCliforCodigo = null
                    return
                }
                selectedCliforCodigo = clifors.getOrNull(position - 1)?.codCliFor
                updateLancamentos()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCliforCodigo = null
            }
        })

        // Spinner Classificação
        val classifPrompt = getString(R.string.prompt_classif)
        val classifNames = listOf(classifPrompt) + classifs.map { "${it.nome_C ?: it.cod_Geral} (${it.cod_Geral})" }
        binding.spinnerClassif.adapter = ArrayAdapter(
            this@LancamentosActivity,
            android.R.layout.simple_spinner_item,
            classifNames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerClassif.prompt = classifPrompt
        binding.spinnerClassif.setSelection(0) // Seleciona o prompt
        binding.spinnerClassif.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    selectedClassifCodigo = null
                    return
                }
                selectedClassifCodigo = classifs.getOrNull(position - 1)?.cod_Geral
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedClassifCodigo = null
            }
        })

        // Spinner Status Movimento
        val statusPrompt = getString(R.string.prompt_status_mov)
        val statusOptions = listOf(statusPrompt, "", "RC", "PG", "TO", "TD", "DO", "JP")
        binding.spinnerStatusMov.adapter = ArrayAdapter(
            this@LancamentosActivity,
            android.R.layout.simple_spinner_item,
            statusOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerStatusMov.prompt = statusPrompt
        binding.spinnerStatusMov.setSelection(0) // Seleciona o prompt
        binding.spinnerStatusMov.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0 || isEditing) return
                updateLancamentos()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        })

        // Spinner Previsão
        val prevPrompt = getString(R.string.prompt_prev)
        val prevOptions = listOf(prevPrompt, "V", "F")
        binding.spinnerPrev.adapter = ArrayAdapter(
            this@LancamentosActivity,
            android.R.layout.simple_spinner_item,
            prevOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerPrev.prompt = prevPrompt
        binding.spinnerPrev.setSelection(0)
    }

    private fun updateSpinnerClifor() {
        val cliforPrompt = getString(R.string.prompt_clifor)
        val cliforNames = listOf(cliforPrompt) + clifors.map { "${it.nomeCliFor} (${it.codCliFor})" }
        binding.spinnerClifor.adapter = ArrayAdapter(
            this@LancamentosActivity,
            android.R.layout.simple_spinner_item,
            cliforNames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun setupDateFields() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun setupDatePicker(editText: EditText) {
            editText.setOnClickListener {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    this,
                    { _, year, month, day ->
                        calendar.set(year, month, day)
                        editText.setText(dateFormat.format(calendar.time))
                        if (editText == binding.edtDtEmi) {
                            updateLancamentos()
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        setupDatePicker(binding.edtDtLancto)
        setupDatePicker(binding.edtDtEmi)
        setupDatePicker(binding.edtDtVcto)
        setupDatePicker(binding.edtDtApr)
    }

    private fun setupValorField() {
        val decimalFormatSymbols = DecimalFormatSymbols(Locale("pt", "BR")).apply {
            currencySymbol = "R$ "
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val decimalFormat = DecimalFormat("R$ #,##0.00", decimalFormatSymbols)

        valorTextWatcher = object : TextWatcher {
            private var isUpdating = false
            private var lastValidValue = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (!isUpdating) {
                    lastValidValue = s.toString()
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true

                val input = s.toString().trim()
                Log.d("LancamentosActivity", "Entrada bruta: $input")

                var cleanString = input.replace("R\\$\\s?".toRegex(), "").replace("-", "")
                Log.d("LancamentosActivity", "Entrada limpa inicial: $cleanString")

                if (cleanString.isEmpty()) {
                    binding.edtValor.setText("")
                    isUpdating = false
                    return
                }

                if (!cleanString.matches("[0-9,.]*".toRegex()) || cleanString.count { it == ',' } > 1 || cleanString.count { it == '.' } > 1) {
                    Toast.makeText(
                        this@LancamentosActivity,
                        "Use apenas números, um ponto ou uma vírgula",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.edtValor.setText(lastValidValue)
                    binding.edtValor.setSelection(lastValidValue.length)
                    isUpdating = false
                    return
                }

                cleanString = cleanString.replace(".", ",")
                Log.d("LancamentosActivity", "Entrada limpa ajustada: $cleanString")

                val isPartialInput =
                    cleanString.isEmpty() || cleanString == "," || cleanString.endsWith(",") || cleanString.count { it == ',' } == 0 || cleanString.substringAfter(
                        ","
                    ).length < 2

                try {
                    if (!isPartialInput) {
                        val parsedValue = cleanString.replace(",", ".").toDoubleOrNull()
                        if (parsedValue != null && cleanString.substringAfter(",").length <= 2) {
                            Log.d("LancamentosActivity", "Valor parseado: $parsedValue")
                            val value = when (binding.spinnerTipoOperacao.selectedItem) {
                                getString(R.string.prompt_tipo_operacao) -> parsedValue
                                "Saída" -> -Math.abs(parsedValue)
                                "Entrada" -> Math.abs(parsedValue)
                                "Nulo" -> parsedValue
                                else -> parsedValue
                            }

                            val formattedValue = decimalFormat.format(value)
                            binding.edtValor.setText(formattedValue)
                            binding.edtValor.setSelection(formattedValue.length)
                        } else {
                            binding.edtValor.setText(cleanString)
                            binding.edtValor.setSelection(cleanString.length)
                        }
                    } else {
                        binding.edtValor.setText(cleanString)
                        binding.edtValor.setSelection(cleanString.length)
                    }
                } catch (e: Exception) {
                    Log.d("LancamentosActivity", "Erro ao parsear: ${e.message}")
                    binding.edtValor.setText(cleanString)
                    binding.edtValor.setSelection(cleanString.length)
                }

                isUpdating = false
            }
        }

        binding.edtValor.addTextChangedListener(valorTextWatcher)

        binding.edtValor.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.edtValor.text.isEmpty()) {
                binding.edtValor.setText("R$ ")
                binding.edtValor.setSelection(binding.edtValor.text.length)
            }
        }
    }

    private fun setupButtons() {
        binding.btnSalvar.setOnClickListener {
            saveLancamento()
        }

        binding.btnExcluir.setOnClickListener {
            if (currentIdMov > 0) {
                AlertDialog.Builder(this, R.style.CustomAlertDialog)
                    .setMessage("Confirmar exclusão do registro $currentIdMov?")
                    .setPositiveButton("Sim") { _, _ ->
                        lifecycleScope.launch(Dispatchers.Main) {
                            try {
                                withContext(Dispatchers.IO) { dbHelper.deleteLancamento(currentIdMov) }
                                clearFields()
                                updateLancamentos()
                                Toast.makeText(
                                    this@LancamentosActivity,
                                    "Registro excluído",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.scrollView.post {
                                    binding.scrollView.scrollTo(0, 0)
                                }
                            } catch (e: Exception) {
                                Log.e("LancamentosActivity", "Erro ao excluir: ${e.message}")
                                Toast.makeText(
                                    this@LancamentosActivity,
                                    "Erro ao excluir: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .setNegativeButton("Não", null)
                    .show()
            } else {
                Toast.makeText(this, "Selecione um registro para excluir", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.btnListar.setOnClickListener {
            updateLancamentos()
            binding.scrollView.post {
                binding.scrollView.scrollTo(0, binding.rvLancamentos.top)
            }
        }

        binding.btnLimpar.setOnClickListener {
            clearFields()
            binding.scrollView.post {
                binding.scrollView.scrollTo(0, 0)
            }
        }

        binding.btnFechar.setOnClickListener {
            finish()
        }

        binding.btnPagamento.setOnClickListener {
            if (currentIdMov <= 0) {
                Toast.makeText(this, "Selecione um lançamento para pagar", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val lancamento = dbHelper.getLancamentoById(currentIdMov)
            if (lancamento == null) {
                Toast.makeText(this, "Lançamento não encontrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (lancamento.dtApr != null || lancamento.Prev != "V") {
                Toast.makeText(
                    this,
                    "Lançamento não pode ser pago (já pago ou inválido)",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!::recursosAtivos.isInitialized || recursosAtivos.isEmpty()) {
                Toast.makeText(this, "Nenhum recurso ativo encontrado", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val dialogView = layoutInflater.inflate(R.layout.dialog_pagamento, null)
            val spinnerRecurso = dialogView.findViewById<Spinner>(R.id.spinner_recurso_pagamento)
            val edtDataPagamento = dialogView.findViewById<EditText>(R.id.edt_data_pagamento)
            val edtValorPago = dialogView.findViewById<EditText>(R.id.edt_valor_pago)
            val edtDesconto = dialogView.findViewById<EditText>(R.id.edt_desconto)
            val edtJuros = dialogView.findViewById<EditText>(R.id.edt_juros)
            val btnConfirmar = dialogView.findViewById<Button>(R.id.btn_confirmar)
            val btnCancelar = dialogView.findViewById<Button>(R.id.btn_cancelar)

            val recursoNames = recursosAtivos.map { it.nomebco ?: it.codigo }
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, recursoNames).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
            spinnerRecurso.adapter = adapter
            spinnerRecurso.prompt = getString(R.string.prompt_recurso)

            val dataAtual =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            edtDataPagamento.setText(dataAtual)
            edtDataPagamento.setOnClickListener {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    this@LancamentosActivity,
                    { _, year, month, day ->
                        calendar.set(year, month, day)
                        edtDataPagamento.setText(
                            SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            ).format(calendar.time)
                        )
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            formatarValorMonetario(edtValorPago, abs(lancamento.Valor))
            formatarValorMonetario(edtDesconto, 0.0)
            formatarValorMonetario(edtJuros, 0.0)

            val formato =
                DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")).apply {
                    currencySymbol = "R$ "
                    groupingSeparator = '.'
                    decimalSeparator = ','
                })
            formato.isParseBigDecimal = true

            val valorTextWatcher = object : TextWatcher {
                private var isUpdating = false
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return
                    isUpdating = true
                    try {
                        val input = s.toString()
                        val parsedValue = formato.parse(input)?.toDouble() ?: 0.0
                        edtValorPago.removeTextChangedListener(this)
                        formatarValorMonetario(edtValorPago, parsedValue)
                        edtValorPago.setSelection(edtValorPago.text.length)
                        edtValorPago.addTextChangedListener(this)
                    } catch (e: Exception) {
                        Log.e("LancamentosActivity", "Erro ao formatar valor: ${e.message}")
                    }
                    isUpdating = false
                }
            }
            val descontoTextWatcher = object : TextWatcher {
                private var isUpdating = false
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return
                    isUpdating = true
                    try {
                        val input = s.toString()
                        val parsedValue = formato.parse(input)?.toDouble() ?: 0.0
                        edtDesconto.removeTextChangedListener(this)
                        formatarValorMonetario(edtDesconto, parsedValue)
                        edtDesconto.setSelection(edtDesconto.text.length)
                        edtDesconto.addTextChangedListener(this)
                    } catch (e: Exception) {
                        Log.e("LancamentosActivity", "Erro ao formatar desconto: ${e.message}")
                    }
                    isUpdating = false
                }
            }
            val jurosTextWatcher = object : TextWatcher {
                private var isUpdating = false
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return
                    isUpdating = true
                    try {
                        val input = s.toString()
                        val parsedValue = formato.parse(input)?.toDouble() ?: 0.0
                        edtJuros.removeTextChangedListener(this)
                        formatarValorMonetario(edtJuros, parsedValue)
                        edtJuros.setSelection(edtJuros.text.length)
                        edtJuros.addTextChangedListener(this)
                    } catch (e: Exception) {
                        Log.e("LancamentosActivity", "Erro ao formatar juros: ${e.message}")
                    }
                    isUpdating = false
                }
            }
            edtValorPago.addTextChangedListener(valorTextWatcher)
            edtDesconto.addTextChangedListener(descontoTextWatcher)
            edtJuros.addTextChangedListener(jurosTextWatcher)

            val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(dialogView)
                .create()

            btnConfirmar.setOnClickListener {
                val selectedRecursoIndex = spinnerRecurso.selectedItemPosition
                if (selectedRecursoIndex < 0 || selectedRecursoIndex >= recursosAtivos.size) {
                    Toast.makeText(this, "Selecione um recurso para pagamento", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                val recursoUtilizado = recursosAtivos[selectedRecursoIndex].codigo
                val fkGpPrinc = recursosAtivos[selectedRecursoIndex].fk_gpprinc ?: ""

                val dataPagamentoStr = formatDate(edtDataPagamento.text.toString())
                if (dataPagamentoStr.isEmpty()) {
                    Toast.makeText(this, "Data de pagamento é obrigatória", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val valorPago = try {
                    formato.parse(edtValorPago.text.toString())?.toDouble() ?: 0.0
                } catch (e: Exception) {
                    Log.e("LancamentosActivity", "Erro ao parsear valor pago: ${e.message}")
                    0.0
                }
                if (valorPago <= 0) {
                    Toast.makeText(this, "Insira um valor pago válido", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val desconto = try {
                    formato.parse(edtDesconto.text.toString())?.toDouble() ?: 0.0
                } catch (e: Exception) {
                    Log.e("LancamentosActivity", "Erro ao parsear desconto: ${e.message}")
                    0.0
                }
                if (desconto < 0) {
                    Toast.makeText(this, "Desconto não pode ser negativo", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val juros = try {
                    formato.parse(edtJuros.text.toString())?.toDouble() ?: 0.0
                } catch (e: Exception) {
                    Log.e("LancamentosActivity", "Erro ao parsear juros: ${e.message}")
                    0.0
                }
                if (juros < 0) {
                    Toast.makeText(this, "Juros não podem ser negativos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val valorOriginal = abs(lancamento.Valor)
                val isPagamentoCompleto = (valorPago + desconto) >= valorOriginal

                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        val success = withContext(Dispatchers.IO) {
                            if (isPagamentoCompleto) {
                                dbHelper.pagarLancamento(
                                    idMov = currentIdMov,
                                    dataPagamento = dataPagamentoStr,
                                    valorPago = valorPago,
                                    recursoUtilizado = recursoUtilizado,
                                    fkGpPrinc = fkGpPrinc,
                                    desconto = desconto,
                                    juros = juros
                                )
                            } else {
                                val valorRestante = valorOriginal - (valorPago + desconto)
                                if (valorRestante <= 0) {
                                    Log.e(
                                        "LancamentosActivity",
                                        "Valor restante inválido: $valorRestante"
                                    )
                                    return@withContext false
                                }
                                dbHelper.pagarLancamentoParcial(
                                    idMov = currentIdMov,
                                    dataPagamento = dataPagamentoStr,
                                    valorPago = valorPago,
                                    valorRestante = valorRestante,
                                    recursoUtilizado = recursoUtilizado,
                                    fkGpPrinc = fkGpPrinc,
                                    juros = juros
                                )
                            }
                        }
                        if (success) {
                            Toast.makeText(
                                this@LancamentosActivity,
                                if (isPagamentoCompleto) "Lançamento pago com sucesso" else "Pagamento parcial registrado com sucesso",
                                Toast.LENGTH_SHORT
                            ).show()
                            clearFields()
                            updateLancamentos()
                            binding.scrollView.post {
                                binding.scrollView.scrollTo(0, binding.rvLancamentos.top)
                            }
                            dialog.dismiss()
                        } else {
                            Toast.makeText(
                                this@LancamentosActivity,
                                "Erro ao processar pagamento",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Log.e("LancamentosActivity", "Erro ao processar pagamento: ${e.message}", e)
                        Toast.makeText(
                            this@LancamentosActivity,
                            "Erro: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            btnCancelar.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        binding.btnRecebimento.setOnClickListener {
            if (currentIdMov <= 0) {
                Toast.makeText(this, "Selecione um lançamento para receber", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val lancamento = dbHelper.getLancamentoById(currentIdMov)
            if (lancamento == null) {
                Toast.makeText(this, "Lançamento não encontrado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (lancamento.dtApr != null || lancamento.Prev != "V") {
                Toast.makeText(
                    this,
                    "Lançamento não pode ser recebido (já recebido ou inválido)",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!::recursosAtivos.isInitialized || recursosAtivos.isEmpty()) {
                Toast.makeText(this, "Nenhum recurso ativo encontrado", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val dialogView = layoutInflater.inflate(R.layout.dialog_recebimento, null)
            val spinnerRecurso = dialogView.findViewById<Spinner>(R.id.spinner_recursorecebimento)
            val edtDataRecebimento = dialogView.findViewById<EditText>(R.id.edt_datarecebimento)
            val edtValorRecebido = dialogView.findViewById<EditText>(R.id.edt_valorrecebido)
            val edtDesconto = dialogView.findViewById<EditText>(R.id.edt_desconto)
            val edtJuros = dialogView.findViewById<EditText>(R.id.edt_juros)
            val btnConfirmar = dialogView.findViewById<Button>(R.id.btn_confirmarrecebimento)
            val btnCancelar = dialogView.findViewById<Button>(R.id.btn_cancelarrecebimento)

            val recursoNames = recursosAtivos.map { it.nomebco ?: it.codigo }
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, recursoNames).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
            spinnerRecurso.adapter = adapter
            spinnerRecurso.prompt = getString(R.string.prompt_recurso)

            val dataAtual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            edtDataRecebimento.setText(dataAtual)
            edtDataRecebimento.setOnClickListener {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    this@LancamentosActivity,
                    { _, year, month, day ->
                        calendar.set(year, month, day)
                        edtDataRecebimento.setText(
                            SimpleDateFormat(
                                "dd/MM/yyyy",
                                Locale.getDefault()
                            ).format(calendar.time)
                        )
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            formatarValorMonetario(edtValorRecebido, lancamento.Valor)
            formatarValorMonetario(edtDesconto, 0.0)
            formatarValorMonetario(edtJuros, 0.0)

            val formato =
                DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")).apply {
                    currencySymbol = "R$ "
                    groupingSeparator = '.'
                    decimalSeparator = ','
                })
            formato.isParseBigDecimal = true

            val valorTextWatcher = object : TextWatcher {
                private var isUpdating = false
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return
                    isUpdating = true
                    try {
                        val input = s.toString()
                        val parsedValue = formato.parse(input)?.toDouble() ?: 0.0
                        edtValorRecebido.removeTextChangedListener(this)
                        formatarValorMonetario(edtValorRecebido, parsedValue)
                        edtValorRecebido.setSelection(edtValorRecebido.text.length)
                        edtValorRecebido.addTextChangedListener(this)
                    } catch (e: Exception) {
                        Log.e(
                            "LancamentosActivity",
                            "Erro ao formatar valor recebido: ${e.message}"
                        )
                    }
                    isUpdating = false
                }
            }
            val descontoTextWatcher = object : TextWatcher {
                private var isUpdating = false
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return
                    isUpdating = true
                    try {
                        val input = s.toString()
                        val parsedValue = formato.parse(input)?.toDouble() ?: 0.0
                        edtDesconto.removeTextChangedListener(this)
                        formatarValorMonetario(edtDesconto, parsedValue)
                        edtDesconto.setSelection(edtDesconto.text.length)
                        edtDesconto.addTextChangedListener(this)
                    } catch (e: Exception) {
                        Log.e("LancamentosActivity", "Erro ao formatar desconto: ${e.message}")
                    }
                    isUpdating = false
                }
            }
            val jurosTextWatcher = object : TextWatcher {
                private var isUpdating = false
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return
                    isUpdating = true
                    try {
                        val input = s.toString()
                        val parsedValue = formato.parse(input)?.toDouble() ?: 0.0
                        edtJuros.removeTextChangedListener(this)
                        formatarValorMonetario(edtJuros, parsedValue)
                        edtJuros.setSelection(edtJuros.text.length)
                        edtJuros.addTextChangedListener(this)
                    } catch (e: Exception) {
                        Log.e("LancamentosActivity", "Erro ao formatar juros: ${e.message}")
                    }
                    isUpdating = false
                }
            }
            edtValorRecebido.addTextChangedListener(valorTextWatcher)
            edtDesconto.addTextChangedListener(descontoTextWatcher)
            edtJuros.addTextChangedListener(jurosTextWatcher)

            val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(dialogView)
                .create()

            btnConfirmar.setOnClickListener {
                val selectedRecursoIndex = spinnerRecurso.selectedItemPosition
                if (selectedRecursoIndex < 0 || selectedRecursoIndex >= recursosAtivos.size) {
                    Toast.makeText(
                        this,
                        "Selecione um recurso para recebimento",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                val recursoUtilizado = recursosAtivos[selectedRecursoIndex].codigo
                val fkGpPrinc = recursosAtivos[selectedRecursoIndex].fk_gpprinc ?: ""

                val dataRecebimentoStr = formatDate(edtDataRecebimento.text.toString())
                if (dataRecebimentoStr.isEmpty()) {
                    Toast.makeText(this, "Data de recebimento é obrigatória", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val valorRecebido = try {
                    formato.parse(edtValorRecebido.text.toString())?.toDouble() ?: 0.0
                } catch (e: Exception) {
                    Log.e("LancamentosActivity", "Erro ao parsear valor recebido: ${e.message}")
                    0.0
                }
                if (valorRecebido <= 0) {
                    Toast.makeText(this, "Insira um valor recebido válido", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val desconto = try {
                    formato.parse(edtDesconto.text.toString())?.toDouble() ?: 0.0
                } catch (e: Exception) {
                    Log.e("LancamentosActivity", "Erro ao parsear desconto: ${e.message}")
                    0.0
                }
                if (desconto < 0) {
                    Toast.makeText(this, "Desconto não pode ser negativo", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                val juros = try {
                    formato.parse(edtJuros.text.toString())?.toDouble() ?: 0.0
                } catch (e: Exception) {
                    Log.e("LancamentosActivity", "Erro ao parsear juros: ${e.message}")
                    0.0
                }
                if (juros < 0) {
                    Toast.makeText(this, "Juros não podem ser negativos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val valorOriginal = lancamento.Valor
                val isRecebimentoCompleto = (valorRecebido + desconto) >= valorOriginal

                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        val success = withContext(Dispatchers.IO) {
                            if (isRecebimentoCompleto) {
                                dbHelper.receberLancamento(
                                    idMov = currentIdMov,
                                    dataRecebimento = dataRecebimentoStr,
                                    valorRecebido = valorRecebido,
                                    recursoUtilizado = recursoUtilizado,
                                    fkGpPrinc = fkGpPrinc,
                                    desconto = desconto,
                                    juros = juros
                                )
                            } else {
                                val valorRestante = valorOriginal - (valorRecebido + desconto)
                                if (valorRestante <= 0) {
                                    Log.e(
                                        "LancamentosActivity",
                                        "Valor restante inválido: $valorRestante"
                                    )
                                    return@withContext false
                                }
                                dbHelper.receberLancamentoParcial(
                                    idMov = currentIdMov,
                                    dataRecebimento = dataRecebimentoStr,
                                    valorRecebido = valorRecebido,
                                    valorRestante = valorRestante,
                                    recursoUtilizado = recursoUtilizado,
                                    fkGpPrinc = fkGpPrinc,
                                    juros = juros
                                )
                            }
                        }
                        if (success) {
                            Toast.makeText(
                                this@LancamentosActivity,
                                if (isRecebimentoCompleto) "Recebimento registrado com sucesso" else "Recebimento parcial registrado com sucesso",
                                Toast.LENGTH_SHORT
                            ).show()
                            clearFields()
                            updateLancamentos()
                            binding.scrollView.post {
                                binding.scrollView.scrollTo(0, binding.rvLancamentos.top)
                            }
                            dialog.dismiss()
                        } else {
                            Toast.makeText(
                                this@LancamentosActivity,
                                "Erro ao processar recebimento",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "LancamentosActivity",
                            "Erro ao processar recebimento: ${e.message}",
                            e
                        )
                        Toast.makeText(
                            this@LancamentosActivity,
                            "Erro: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            btnCancelar.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun saveLancamento() {
        val decimalFormatSymbols = DecimalFormatSymbols(Locale("pt", "BR")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val decimalFormat = DecimalFormat("#,##0.00", decimalFormatSymbols)

        // Validar Tipo Operação
        if (binding.spinnerTipoOperacao.selectedItem == getString(R.string.prompt_tipo_operacao)) {
            Toast.makeText(this, "Selecione o Tipo de Operação", Toast.LENGTH_SHORT).show()
            return
        }

        val valorText = binding.edtValor.text.toString().replace("R\\$\\s?".toRegex(), "").replace("-", "")
        Log.d("LancamentosActivity", "Valor antes de salvar: $valorText")

        val valor = try {
            val cleanValorText = valorText.replace(".", "").replace(",", ".")
            Log.d("LancamentosActivity", "Valor limpo para parsing: $cleanValorText")
            val parsedValue = cleanValorText.toDoubleOrNull()
            if (parsedValue == null) {
                Toast.makeText(
                    this,
                    "Por favor, insira um valor válido (ex.: 1234,56 ou 1.234,56)",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            val decimalPart = valorText.substringAfter(",", "").trim()
            if (decimalPart.isNotEmpty() && decimalPart.length > 2) {
                Toast.makeText(
                    this,
                    "O valor deve ter no máximo duas casas decimais (ex.: 1234,56)",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            when (binding.spinnerTipoOperacao.selectedItem) {
                "Saída" -> -Math.abs(parsedValue)
                "Entrada" -> Math.abs(parsedValue)
                "Nulo" -> parsedValue
                else -> parsedValue
            }
        } catch (e: Exception) {
            Log.e("LancamentosActivity", "Erro ao parsear valor: $valorText, Erro: ${e.message}", e)
            Toast.makeText(
                this,
                "Por favor, insira um valor válido (ex.: 1234,56 ou 1.234,56)",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (binding.edtDtEmi.text.isEmpty()) {
            Toast.makeText(this, "Data de Emissão é obrigatória", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.edtDtVcto.text.isEmpty()) {
            Toast.makeText(this, "Data de Vencimento é obrigatória", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.spinnerRecurso.selectedItem == getString(R.string.prompt_recurso)) {
            Toast.makeText(this, "Selecione um recurso", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.spinnerClifor.selectedItem == getString(R.string.prompt_clifor)) {
            Toast.makeText(this, "Selecione um Cli/For", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.spinnerClassif.selectedItem == getString(R.string.prompt_classif)) {
            Toast.makeText(this, "Selecione uma classificação", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.spinnerStatusMov.selectedItem == getString(R.string.prompt_status_mov)) {
            Toast.makeText(this, "Selecione o Status do Movimento", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.spinnerPrev.selectedItem == getString(R.string.prompt_prev)) {
            Toast.makeText(this, "Selecione a Previsão", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val selectedRecurso = recursos.firstOrNull { it.codigo == selectedRecursoCodigo }
                val selectedClifor = clifors.firstOrNull { it.codCliFor == selectedCliforCodigo }

                if (selectedRecurso == null || selectedClifor == null) {
                    Log.e(
                        "LancamentosActivity",
                        "Recurso=$selectedRecursoCodigo ou Cli/For=$selectedCliforCodigo inválido"
                    )
                    Toast.makeText(
                        this@LancamentosActivity,
                        "Recurso ou Cli/For inválido",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val dtLanctoFormatted = formatDate(binding.edtDtLancto.text.toString())
                val dtEmiFormatted = formatDate(binding.edtDtEmi.text.toString())
                val dtVctoFormatted = formatDate(binding.edtDtVcto.text.toString())
                val dtAprFormatted = formatDate(binding.edtDtApr.text.toString())
                val documento = binding.edtDocumento.text.toString().takeIf { it.isNotEmpty() }
                val descr = binding.edtDescr.text.toString().takeIf { it.isNotEmpty() } ?: "Lançamento"

                if (isRecursoCartao && valor < 0 && currentIdMov == 0) {
                    Log.d("LancamentosActivity", "Processando compra com cartão")
                    val success = withContext(Dispatchers.IO) {
                        dbHelper.salvarCompraCartao(
                            recurso = "0079",
                            vrecurso = "2.001.002",
                            clifor = selectedCliforCodigo!!,
                            vCliFor = selectedClifor.fkCliForGp ?: "",
                            dtlancto = dtLanctoFormatted,
                            dtEmi = dtEmiFormatted,
                            dtVcto = dtVctoFormatted,
                            documento = documento ?: "",
                            classif = selectedClassifCodigo!!,
                            descr = descr,
                            valor = -valor,
                            cartaoRecurso = selectedRecursoCodigo!!,
                            cartaoVrecurso = selectedRecurso.fk_gpprinc ?: ""
                        )
                    }
                    if (success) {
                        Toast.makeText(
                            this@LancamentosActivity,
                            "Compra com cartão salva com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()
                        clearFields()
                        updateLancamentos()
                        binding.scrollView.post {
                            binding.scrollView.scrollTo(0, binding.rvLancamentos.top)
                        }
                    } else {
                        Log.e("LancamentosActivity", "Erro ao salvar compra com cartão")
                        Toast.makeText(
                            this@LancamentosActivity,
                            "Erro ao salvar compra com cartão",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val lancamento = Lancamento(
                        idMov = currentIdMov,
                        recurso = selectedRecursoCodigo!!,
                        vrecurso = selectedRecurso.fk_gpprinc ?: "",
                        clifor = selectedCliforCodigo!!,
                        vCliFor = selectedClifor.fkCliForGp ?: "",
                        dtLancto = dtLanctoFormatted,
                        dtEmi = dtEmiFormatted,
                        dtVcto = dtVctoFormatted,
                        documento = documento,
                        classif = selectedClassifCodigo!!,
                        Descr = descr,
                        Valor = valor,
                        dtApr = if (dtAprFormatted.isEmpty()) null else dtAprFormatted,
                        statusMov = binding.spinnerStatusMov.selectedItem.toString(),
                        Prev = binding.spinnerPrev.selectedItem.toString()
                    )

                    Log.d(
                        "LancamentosActivity",
                        "Salvando lançamento: ID=${lancamento.idMov}, recurso=${lancamento.recurso}, vrecurso=${lancamento.vrecurso}, clifor=${lancamento.clifor}, vCliFor=${lancamento.vCliFor}, Valor=${lancamento.Valor}, statusMov=${lancamento.statusMov}, classif=${lancamento.classif}, Descr=${lancamento.Descr}"
                    )

                    val result = withContext(Dispatchers.IO) {
                        if (currentIdMov == 0) {
                            dbHelper.insertLancamento(lancamento)
                        } else {
                            dbHelper.updateLancamento(currentIdMov, lancamento).toLong()
                        }
                    }

                    if (result != -1L) {
                        Toast.makeText(
                            this@LancamentosActivity,
                            if (currentIdMov == 0) "Lançamento salvo com sucesso" else "Lançamento atualizado com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()
                        clearFields()
                        updateLancamentos()
                        binding.scrollView.post {
                            binding.scrollView.scrollTo(0, binding.rvLancamentos.top)
                        }
                    } else {
                        Log.e("LancamentosActivity", "Falha ao salvar lançamento, result=$result")
                        Toast.makeText(
                            this@LancamentosActivity,
                            "Erro ao salvar lançamento",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("LancamentosActivity", "Erro geral ao salvar lançamento: ${e.message}", e)
                Toast.makeText(
                    this@LancamentosActivity,
                    "Erro ao salvar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadLancamentoForEdit(lancamento: Lancamento) {
        isEditing = true
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                Log.d(
                    "LancamentosActivity",
                    "Carregando lançamento: ID=${lancamento.idMov}, recurso=${lancamento.recurso}, vrecurso=${lancamento.vrecurso}, clifor=${lancamento.clifor}, vCliFor=${lancamento.vCliFor}, Valor=${lancamento.Valor}, statusMov=${lancamento.statusMov}"
                )

                binding.edtValor.removeTextChangedListener(valorTextWatcher)

                val tipoOperacaoLancamento = when {
                    lancamento.Valor < 0 -> "Saída"
                    lancamento.Valor > 0 -> "Entrada"
                    else -> "Nulo"
                }

                clifors = withContext(Dispatchers.IO) {
                    when (tipoOperacaoLancamento) {
                        "Entrada" -> dbHelper.getClientes()
                        "Saída" -> dbHelper.getFornecedores()
                        else -> dbHelper.getAllClifors()
                    }
                }
                
                // Atualiza o adapter do Spinner antes de tentar selecionar
                val cliforPrompt = getString(R.string.prompt_clifor)
                val cliforNames = listOf(cliforPrompt) + clifors.map { "${it.nomeCliFor} (${it.codCliFor})" }
                binding.spinnerClifor.adapter = ArrayAdapter(
                    this@LancamentosActivity,
                    android.R.layout.simple_spinner_item,
                    cliforNames
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

                currentIdMov = lancamento.idMov
                binding.txtIdMov.text = "ID: $currentIdMov"
                binding.txtIdMov.visibility = View.VISIBLE
                
                val opIndex = when {
                    lancamento.Valor < 0 -> 2 // Saída
                    lancamento.Valor > 0 -> 1 // Entrada
                    else -> 3 // Nulo
                }
                binding.spinnerTipoOperacao.setSelection(opIndex)
                selectedTipoOperacao = when(opIndex) {
                    1 -> "Entrada"
                    2 -> "Saída"
                    else -> "Nulo"
                }

                binding.spinnerRecurso.setSelection(
                    recursos.indexOfFirst { it.codigo == lancamento.recurso }
                        .takeIf { it >= 0 }?.let { it + 1 } ?: 0
                )
                selectedRecursoCodigo = lancamento.recurso

                // Agora a seleção deve funcionar pois o adapter foi setado logo acima e isEditing trava o listener
                val cliforIndex = clifors.indexOfFirst { it.codCliFor == lancamento.clifor }
                if (cliforIndex >= 0) {
                    binding.spinnerClifor.setSelection(cliforIndex + 1)
                    selectedCliforCodigo = lancamento.clifor
                } else {
                    binding.spinnerClifor.setSelection(0)
                    selectedCliforCodigo = null
                }

                binding.edtDtLancto.setText(formatDateToDisplay(lancamento.dtLancto))
                binding.edtDtEmi.setText(formatDateToDisplay(lancamento.dtEmi))
                binding.edtDtVcto.setText(formatDateToDisplay(lancamento.dtVcto))
                binding.edtDocumento.setText(lancamento.documento ?: "")

                binding.spinnerClassif.setSelection(
                    classifs.indexOfFirst { it.cod_Geral == lancamento.classif }
                        .takeIf { it >= 0 }?.let { it + 1 } ?: 0
                )

                binding.edtDescr.setText(lancamento.Descr ?: "")

                try {
                    val decimalFormat =
                        DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
                    val formattedValue = decimalFormat.format(lancamento.Valor)
                    binding.edtValor.setText(formattedValue)
                    binding.edtValor.setSelection(formattedValue.length)
                    Log.d("LancamentosActivity", "Valor formatado para edtValor: $formattedValue")
                } catch (e: Exception) {
                    Log.e(
                        "LancamentosActivity",
                        "Erro ao formatar valor: ${lancamento.Valor}, Erro: ${e.message}"
                    )
                    binding.edtValor.setText("")
                }

                binding.edtDtApr.setText(formatDateToDisplay(lancamento.dtApr))
                binding.spinnerStatusMov.setSelection(
                    listOf(getString(R.string.prompt_status_mov), "", "RC", "PG", "TO", "TD", "DO", "JP").indexOf(
                        lancamento.statusMov ?: ""
                    )
                )
                binding.spinnerPrev.setSelection(
                    listOf(getString(R.string.prompt_prev), "V", "F").indexOf(lancamento.Prev)
                )

                binding.btnSalvar.contentDescription = getString(R.string.alterar)

                binding.edtValor.addTextChangedListener(valorTextWatcher)

                binding.scrollView.post {
                    binding.scrollView.scrollTo(0, 0)
                }
                
                isEditing = false
            } catch (e: Exception) {
                isEditing = false
                Log.e("LancamentosActivity", "Erro ao carregar lançamento: ${e.message}")
                Toast.makeText(
                    this@LancamentosActivity,
                    "Erro ao carregar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun formatDate(dateStr: String): String {
        if (dateStr.isEmpty()) return ""
        return try {
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dbFormat.format(displayFormat.parse(dateStr)!!)
        } catch (e: Exception) {
            Log.e("LancamentosActivity", "Erro ao formatar data: $dateStr, ${e.message}")
            ""
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

    private fun updateLancamentos() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime < 1000) return
        lastUpdateTime = currentTime

        val startTime = System.currentTimeMillis()
        offset = 0
        val sixtyDaysAgo = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -60) }.time
        )
        val startDate = formatDate(binding.edtDtEmi.text.toString()).takeIf { it.isNotEmpty() }
            ?: sixtyDaysAgo

        val dtLanctoStr = formatDate(binding.edtDtLancto.text.toString())
        val dtEmiStr = formatDate(binding.edtDtEmi.text.toString())
        val isDateFilterMode = dtLanctoStr.isNotEmpty() || dtEmiStr.isNotEmpty()

        lifecycleScope.launch(Dispatchers.Main) {
            isLoading = true
            try {
                val (lancamentos, saldo) = withContext(Dispatchers.IO) {
                    if (isDateFilterMode) {
                        dbHelper.getLancamentosFiltered(
                            dtLancto = dtLanctoStr.takeIf { it.isNotEmpty() },
                            dtEmi = dtEmiStr.takeIf { it.isNotEmpty() },
                            recurso = selectedRecursoCodigo,
                            clifor = selectedCliforCodigo,
                            isRecursoCartao = isRecursoCartao,
                            offset = offset,
                            pageSize = pageSize
                        )
                    } else {
                        when (selectedRecursoCodigo) {
                            "0401" -> dbHelper.getLancamentosByClifor("0022", offset, pageSize)
                            "0402" -> dbHelper.getLancamentosByClifor("0079", offset, pageSize)
                            null -> {
                                val lancamentosPendentes = dbHelper.getLancamentosPendentesWithOffset(
                                    sixtyDaysAgo,
                                    offset,
                                    pageSize
                                )
                                Pair(lancamentosPendentes, 0.0)
                            }
                            else -> if (isRecursoCartao) {
                                dbHelper.getLancamentosCartao(offset, pageSize)
                            } else {
                                dbHelper.getLancamentosByRecurso(
                                    selectedRecursoCodigo!!,
                                    startDate,
                                    offset,
                                    pageSize
                                )
                            }
                        }
                    }
                }
                saldoInicial = saldo
                lastLoadedCount = lancamentos.size
                offset += lancamentos.size
                Log.d(
                    "LancamentosActivity",
                    "Número de lançamentos carregados: ${lancamentos.size}, saldo inicial: $saldoInicial em ${System.currentTimeMillis() - startTime}ms"
                )
                lancamentos.forEach {
                    Log.d(
                        "LancamentosActivity",
                        "Lançamento: ID=${it.idMov}, Descr=${it.Descr}, Valor=${it.Valor}, dtVcto=${it.dtVcto}, dtApr=${it.dtApr}, statusMov=${it.statusMov}"
                    )
                }
                adapter.updateData(lancamentos, saldoInicial)
                binding.rvLancamentos.visibility = View.VISIBLE
                binding.scrollView.post {
                    binding.scrollView.scrollTo(0, binding.rvLancamentos.top)
                }
            } catch (e: Exception) {
                Log.e("LancamentosActivity", "Erro ao atualizar lançamentos: ${e.message}")
                Toast.makeText(
                    this@LancamentosActivity,
                    "Erro ao atualizar lançamentos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isLoading = false
            }
        }
    }

    private fun debugDatabase() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val count = withContext(Dispatchers.IO) {
                    val db = dbHelper.readableDatabase
                    db.rawQuery("SELECT COUNT(*) FROM tbmovimento", null).use {
                        it.moveToFirst()
                        it.getInt(0)
                    }
                }
                val sixtyDaysAgo = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                    Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -60) }.time
                )
                val pendentesCount = withContext(Dispatchers.IO) {
                    val db = dbHelper.readableDatabase
                    db.rawQuery(
                        "SELECT COUNT(*) FROM tbmovimento WHERE dtApr IS NULL AND DATE(dtVcto) >= ?",
                        arrayOf(sixtyDaysAgo)
                    ).use {
                        it.moveToFirst()
                        it.getInt(0)
                    }
                }
                val cartaoCount = withContext(Dispatchers.IO) {
                    val db = dbHelper.readableDatabase
                    db.rawQuery(
                        "SELECT COUNT(*) FROM tbmovimento WHERE dtApr IS NULL AND vrecurso = '2.001.003'",
                        null
                    ).use {
                        it.moveToFirst()
                        it.getInt(0)
                    }
                }
                val openCursors = withContext(Dispatchers.IO) { dbHelper.verifyOpenCursors() }
                Toast.makeText(
                    this@LancamentosActivity,
                    "Total de lançamentos: $count\nPendentes (dtApr IS NULL, dtVcto >= $sixtyDaysAgo): $pendentesCount\nCartão (vrecurso = '2.001.003'): $cartaoCount\nCursores abertos: $openCursors",
                    Toast.LENGTH_LONG
                ).show()
                Log.d(
                    "LancamentosActivity",
                    "Total de lançamentos: $count, Pendentes: $pendentesCount, Cartão: $cartaoCount, Cursores abertos: $openCursors"
                )
                if (openCursors > 0) {
                    Log.w("LancamentosActivity", "Aviso: $openCursors cursores abertos detectados. Verifique DatabaseHelper.")
                }
            } catch (e: Exception) {
                Log.e("LancamentosActivity", "Erro ao depurar banco: ${e.message}")
                Toast.makeText(
                    this@LancamentosActivity,
                    "Erro ao depurar banco: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun clearFields() {
        currentIdMov = 0
        binding.txtIdMov.text = "ID: "
        binding.txtIdMov.visibility = View.GONE
        binding.spinnerTipoOperacao.setSelection(0)
        binding.spinnerRecurso.setSelection(0)
        binding.spinnerClifor.setSelection(0)
        binding.edtDtLancto.text.clear()
        binding.edtDtEmi.text.clear()
        binding.edtDtVcto.text.clear()
        binding.edtDocumento.text.clear()
        binding.edtDescr.text.clear()
        binding.edtValor.text.clear()
        binding.edtDtApr.text.clear()
        binding.spinnerStatusMov.setSelection(0)
        binding.spinnerPrev.setSelection(0)
        binding.btnSalvar.contentDescription = getString(R.string.salvar)
    }

    private fun formatarValorMonetario(editText: EditText, valor: Double) {
        val decimalFormatSymbols = DecimalFormatSymbols(Locale("pt", "BR")).apply {
            currencySymbol = "R$ "
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val decimalFormat = DecimalFormat("R$ #,##0.00", decimalFormatSymbols)
        editText.setText(decimalFormat.format(valor))
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.rvLancamentos.adapter = null
        dbHelper.close()
    }
}