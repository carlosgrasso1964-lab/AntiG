package com.carlos.finas.activities

import android.R
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.databinding.ActivityParcelamentosCartoesBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class ParcelamentosCartoesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityParcelamentosCartoesBinding
    private lateinit var dbHelper: DatabaseHelper
    private var codClifor: String = ""
    private var fkCliForGp: String = ""
    private var codClassif: String = ""
    private var codRecurso: String = ""
    private var fkRecursoGp: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParcelamentosCartoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        setupSpinners()
        setupDatePickers()
        setupFieldListeners()
        setupButtonListeners()
    }

    private fun setupSpinners() {
        // Configurar spinner de tipo de operação
        val tipoOperacao = arrayOf("Saída") // Apenas Saída para cartões
        val tipoOperacaoAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, tipoOperacao)
        tipoOperacaoAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerTipoOperacao.adapter = tipoOperacaoAdapter

        // Configurar spinner de recursos (apenas cartões)
        val recursos = dbHelper.carregarRecursosCartoes()
        val recursosAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, recursos.map { it.first })
        recursosAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerRecurso.adapter = recursosAdapter
        binding.spinnerRecurso.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = recursos[position].second
                codRecurso = selected.first
                fkRecursoGp = selected.second
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                codRecurso = ""
                fkRecursoGp = ""
            }
        })

        // Configurar spinner de clifor
        val cliforList = dbHelper.carregarClifor()
        val cliforAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, cliforList.map { it.first })
        cliforAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerClifor.adapter = cliforAdapter
        binding.spinnerClifor.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = cliforList[position].second
                codClifor = selected.first
                fkCliForGp = selected.second
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                codClifor = ""
                fkCliForGp = ""
            }
        })

        // Configurar spinner de classificação
        val classifList = dbHelper.carregarClassificacao()
        val classifAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, classifList.map { it.first })
        classifAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerClassif.adapter = classifAdapter
        binding.spinnerClassif.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                codClassif = classifList[position].second
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                codClassif = ""
            }
        })

        // Configurar spinner de número de parcelas
        val parcelas = (1..12).map { it.toString() }.toTypedArray()
        val parcelasAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, parcelas)
        parcelasAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerNumParcelas.adapter = parcelasAdapter
        binding.spinnerNumParcelas.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                calcularValorParcela()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        })

        // Configurar spinner de periodicidade
        val periodicidades = resources.getStringArray(com.carlos.finas.R.array.periodicidade_array)
        val periodicidadeAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, periodicidades)
        periodicidadeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerPeriodicidade.adapter = periodicidadeAdapter

        // Configurar spinner de status
        val statusMov = arrayOf("", "PG", "RC")
        val statusAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, statusMov)
        statusAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = statusAdapter

        // Configurar spinner de previsão
        val prevOptions = arrayOf("V", "F")
        val prevAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, prevOptions)
        prevAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerPrev.adapter = prevAdapter
    }

    private fun setupDatePickers() {
        val dateFormatDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateFormatStorage = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        binding.edtDtEmi.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                binding.edtDtEmi.setText(dateFormatDisplay.format(calendar.time))
                binding.edtDtEmi.tag = dateFormatStorage.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.edtDtVcto.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                binding.edtDtVcto.setText(dateFormatDisplay.format(calendar.time))
                binding.edtDtVcto.tag = dateFormatStorage.format(calendar.time)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupFieldListeners() {
        binding.edtValorTotal.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val valorText = binding.edtValorTotal.text.toString().replace("R\\$\\s?".toRegex(), "").replace("-", "")
                if (valorText.isNotEmpty()) {
                    val parsedValue = parseValor(valorText)
                    if (parsedValue == null) {
                        Toast.makeText(this, "Por favor, insira um valor válido (ex.: 1234,56 ou 1.234,56)", Toast.LENGTH_SHORT).show()
                        binding.edtValorTotal.text.clear()
                        binding.edtValorParcela.text.clear()
                        return@setOnFocusChangeListener
                    }
                    val tipo = binding.spinnerTipoOperacao.selectedItem.toString()
                    val valorFormatado = if (tipo == "Saída") -Math.abs(parsedValue) else Math.abs(parsedValue)
                    formatarValorMonetario(binding.edtValorTotal, valorFormatado)
                    calcularValorParcela()
                }
            }
        }

        binding.edtValorTotal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularValorParcela()
            }
        })
    }

    private fun parseValor(valorText: String): Double? {
        val cleanValorText = valorText.replace(".", "").replace(",", ".")
        return try {
            val parsedValue = cleanValorText.toDoubleOrNull()
            if (parsedValue == null || parsedValue <= 0) return null
            val decimalPart = cleanValorText.substringAfter(".", "").trim()
            if (decimalPart.isNotEmpty() && decimalPart.length > 2) return null
            parsedValue
        } catch (e: Exception) {
            null
        }
    }

    private fun formatarValorMonetario(editText: EditText, valor: Double) {
        val decimalFormatSymbols = DecimalFormatSymbols(Locale("pt", "BR")).apply {
            currencySymbol = "R$ "
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val decimalFormat = DecimalFormat("R$ #,##0.00", decimalFormatSymbols)
        editText.setText(decimalFormat.format(Math.abs(valor)))
    }

    private fun calcularValorParcela() {
        val valorText = binding.edtValorTotal.text.toString().replace("R\\$\\s?".toRegex(), "").replace("-", "")
        val valorTotal = parseValor(valorText)
        val numParcelas = binding.spinnerNumParcelas.selectedItem?.toString()?.toIntOrNull() ?: 1
        if (valorTotal != null && numParcelas > 0) {
            val valorParcela = valorTotal / numParcelas
            formatarValorMonetario(binding.edtValorParcela, valorParcela)
        } else {
            binding.edtValorParcela.text.clear()
        }
    }

    private fun setupButtonListeners() {
        binding.btnGerar.setOnClickListener {
            val recurso = codRecurso
            val vrecurso = fkRecursoGp
            val clifor = codClifor
            val vCliFor = fkCliForGp
            val dtEmi = binding.edtDtEmi.tag?.toString() ?: ""
            val dtVcto = binding.edtDtVcto.tag?.toString() ?: ""
            val documento = binding.edtDocumento.text.toString()
            val classif = codClassif
            val descr = binding.edtDescricao.text.toString()
            val valorText = binding.edtValorTotal.text.toString().replace("R\\$\\s?".toRegex(), "")
            val valorParcelaText = binding.edtValorParcela.text.toString().replace("R\\$\\s?".toRegex(), "")
            val valorTotal = parseValor(valorText)
            val valorParcela = parseValor(valorParcelaText)
            val numParcelas = binding.spinnerNumParcelas.selectedItem.toString().toInt()
            val periodicidade = binding.spinnerPeriodicidade.selectedItem.toString()
            val statusMov = binding.spinnerStatus.selectedItem.toString()
            val prev = binding.spinnerPrev.selectedItem.toString()
            val cartaoRecurso = codRecurso
            val cartaoVrecurso = fkRecursoGp

            if (dtEmi.isEmpty()) {
                Toast.makeText(this, "Preencha a Data de Emissão!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (dtVcto.isEmpty()) {
                Toast.makeText(this, "Preencha a Data de Vencimento!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (descr.isEmpty()) {
                Toast.makeText(this, "Preencha a Descrição!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (clifor.isEmpty()) {
                Toast.makeText(this, "Selecione um Cliente/Fornecedor!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (classif.isEmpty()) {
                Toast.makeText(this, "Selecione uma Classificação!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (recurso.isEmpty()) {
                Toast.makeText(this, "Selecione um Recurso!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (cartaoRecurso.isEmpty()) {
                Toast.makeText(this, "Selecione um Cartão!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (valorTotal == null) {
                Toast.makeText(this, "Por favor, insira um valor total válido (ex.: 1234,56 ou 1.234,56)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (valorParcela == null) {
                Toast.makeText(this, "Valor da Parcela inválido! Verifique o Valor Total.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Garantir que valorTotalSigned seja positivo para cálculos
            val valorTotalSigned = Math.abs(valorTotal)
            val valorParcelaSigned = valorParcela

            // Calcular diferença com maior precisão
            val totalCalculado = valorParcelaSigned * numParcelas
            val diff = Math.abs(valorTotalSigned - totalCalculado)

            if (diff > 0.001) { // Tolerância para erros de arredondamento
                val input = EditText(this).apply {
                    inputType = InputType.TYPE_CLASS_NUMBER
                    hint = "Digite o número da parcela (1 a $numParcelas)"
                }
                AlertDialog.Builder(this)
                    .setTitle("Ajustar Diferença")
                    .setMessage("A divisão resulta em uma diferença de R$ ${String.format(Locale("pt", "BR"), "%.2f", diff)}. Digite o número da parcela para ajustar (1 a $numParcelas):")
                    .setView(input)
                    .setPositiveButton("Confirmar") { _, _ ->
                        val parcelaAjustadaStr = input.text.toString()
                        val parcelaAjustada = parcelaAjustadaStr.toIntOrNull()
                        if (parcelaAjustada == null || parcelaAjustada < 1 || parcelaAjustada > numParcelas) {
                            Toast.makeText(this, "Por favor, digite uma parcela válida (1 a $numParcelas)", Toast.LENGTH_SHORT).show()
                        } else {
                            AlertDialog.Builder(this)
                                .setTitle("Corrige Emissão")
                                .setMessage("Corrige Data da Emissão?")
                                .setPositiveButton("Sim") { _, _ ->
                                    salvarParcelamento(
                                        recurso, vrecurso, clifor, vCliFor, dtEmi, dtVcto,
                                        documento, classif, descr, valorTotalSigned, valorParcelaSigned,
                                        numParcelas, periodicidade, true, statusMov, prev, parcelaAjustada, diff,
                                        cartaoRecurso, cartaoVrecurso
                                    )
                                }
                                .setNegativeButton("Não") { _, _ ->
                                    salvarParcelamento(
                                        recurso, vrecurso, clifor, vCliFor, dtEmi, dtVcto,
                                        documento, classif, descr, valorTotalSigned, valorParcelaSigned,
                                        numParcelas, periodicidade, false, statusMov, prev, parcelaAjustada, diff,
                                        cartaoRecurso, cartaoVrecurso
                                    )
                                }
                                .show()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Corrige Emissão")
                    .setMessage("Corrige Data da Emissão?")
                    .setPositiveButton("Sim") { _, _ ->
                        salvarParcelamento(
                            recurso, vrecurso, clifor, vCliFor, dtEmi, dtVcto,
                            documento, classif, descr, valorTotalSigned, valorParcelaSigned,
                            numParcelas, periodicidade, true, statusMov, prev, 1, 0.0,
                            cartaoRecurso, cartaoVrecurso
                        )
                    }
                    .setNegativeButton("Não") { _, _ ->
                        salvarParcelamento(
                            recurso, vrecurso, clifor, vCliFor, dtEmi, dtVcto,
                            documento, classif, descr, valorTotalSigned, valorParcelaSigned,
                            numParcelas, periodicidade, false, statusMov, prev, 1, 0.0,
                            cartaoRecurso, cartaoVrecurso
                        )
                    }
                    .show()
            }
        }

        binding.btnLimpar.setOnClickListener {
            limparCampos()
        }

        binding.btnFechar.setOnClickListener {
            finish()
        }
    }

    private fun salvarParcelamento(
        recurso: String, vrecurso: String, clifor: String, vCliFor: String,
        dtEmi: String, dtVcto: String, documento: String, classif: String, descr: String,
        valorTotal: Double, valorParcela: Double, numParcelas: Int, periodicidade: String,
        corrigeEmissao: Boolean, statusMov: String, prev: String, parcelaAjustada: Int, diff: Double,
        cartaoRecurso: String, cartaoVrecurso: String
    ) {
        val sucesso = dbHelper.inserirLancamentoParceladoCartao(
            recurso, vrecurso, clifor, vCliFor, dtEmi, dtVcto,
            documento, classif, descr, valorTotal, valorParcela,
            numParcelas, periodicidade, corrigeEmissao, prev, parcelaAjustada, diff,
            cartaoRecurso, cartaoVrecurso
        )
        if (sucesso) {
            Toast.makeText(this, "Parcelamento de cartão gerado com sucesso!", Toast.LENGTH_SHORT).show()
            limparCampos()
        } else {
            Toast.makeText(this, "Erro ao gerar parcelamento! Verifique os dados.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun limparCampos() {
        codClifor = ""
        fkCliForGp = ""
        codClassif = ""
        codRecurso = ""
        fkRecursoGp = ""
        binding.edtDocumento.text.clear()
        binding.edtDescricao.text.clear()
        binding.edtValorTotal.text.clear()
        binding.edtValorParcela.text.clear()
        binding.edtDtEmi.text.clear()
        binding.edtDtEmi.tag = null
        binding.edtDtVcto.text.clear()
        binding.edtDtVcto.tag = null
        binding.spinnerTipoOperacao.setSelection(0)
        binding.spinnerRecurso.setSelection(0)
        binding.spinnerClifor.setSelection(0)
        binding.spinnerClassif.setSelection(0)
        binding.spinnerNumParcelas.setSelection(0)
        binding.spinnerPeriodicidade.setSelection(0)
        binding.spinnerStatus.setSelection(0)
        binding.spinnerPrev.setSelection(0)
    }
}