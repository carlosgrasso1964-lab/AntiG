package com.carlos.finas.activities

import androidx.appcompat.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.R
import com.carlos.finas.models.Recurso
import com.carlos.finas.adapters.RecursoAdapter
import com.carlos.finas.databinding.ActivityCadastroRecursosBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CadastroRecursosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCadastroRecursosBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: RecursoAdapter
    private var selectedRecurso: Recurso? = null
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var limiteTextWatcher: TextWatcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroRecursosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        adapter = RecursoAdapter(mutableListOf()) { recurso ->
            selectedRecurso = recurso
            populateFields()
        }
        binding.rvRecursos.layoutManager = LinearLayoutManager(this)
        binding.rvRecursos.adapter = adapter

        setupSpinners()
        setupDateFields()
        setupCurrencyFormat()
        loadRecursos()

        binding.btnAddRecurso.setOnClickListener {
            if (selectedRecurso == null) addRecurso() else updateRecurso()
        }

        binding.btnDeleteRecurso.setOnClickListener {
            selectedRecurso?.let { recurso ->
                AlertDialog.Builder(this, R.style.CustomAlertDialog)
                    .setTitle("Excluir Recurso")
                    .setMessage("Deseja excluir o recurso ${recurso.nomebco}?")
                    .setPositiveButton("Sim") { _, _ -> deleteRecurso(recurso.codigo) }
                    .setNegativeButton("Não", null)
                    .show()
            } ?: Toast.makeText(this, "Selecione um recurso!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinners() {
        lifecycleScope.launch(Dispatchers.Main) {
            // Fluxo
            val fluxoOptions = resources.getStringArray(R.array.fluxo_options_binary).toList()
            binding.spinnerFluxo.adapter = ArrayAdapter(this@CadastroRecursosActivity, android.R.layout.simple_spinner_item, fluxoOptions)

            // Status
            val statusOptions = resources.getStringArray(R.array.status_options).toList()
            binding.spinnerStatus.adapter = ArrayAdapter(this@CadastroRecursosActivity, android.R.layout.simple_spinner_item, statusOptions)

            // Conta Vinculada
            val gpPrincOptions = withContext(Dispatchers.IO) { dbHelper.getAllPlanosContas() }
                .map { "${it.cod_Geral} - ${it.nome_C ?: ""}" }
            binding.spinnerGpPrinc.adapter = ArrayAdapter(this@CadastroRecursosActivity, android.R.layout.simple_spinner_item, gpPrincOptions)
        }
    }

    private fun setupDateFields() {
        fun setupDatePicker(editText: EditText) {
            editText.setOnClickListener {
                val calendar = Calendar.getInstance()
                DatePickerDialog(this, { _, year, month, day ->
                    calendar.set(year, month, day)
                    editText.setText(displayDateFormat.format(calendar.time))
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }
        }
        setupDatePicker(binding.edtAbertura)
        setupDatePicker(binding.edtEncerramento)
    }

    private fun setupCurrencyFormat() {
        val decimalFormatSymbols = DecimalFormatSymbols(Locale("pt", "BR")).apply {
            currencySymbol = "R$ "
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val decimalFormat = DecimalFormat("R$ #,##0.00", decimalFormatSymbols)

        limiteTextWatcher = object : TextWatcher {
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
                if (input.isEmpty()) {
                    binding.edtLimite.setText("")
                    isUpdating = false
                    return
                }

                // Remove tudo exceto números, vírgula e ponto
                var cleanString = input.replace("[^0-9,.]".toRegex(), "")
                if (!cleanString.matches("[0-9,.]*".toRegex()) || cleanString.count { it == ',' } > 1 || cleanString.count { it == '.' } > 1) {
                    Toast.makeText(this@CadastroRecursosActivity, "Use apenas números e uma vírgula", Toast.LENGTH_SHORT).show()
                    binding.edtLimite.setText(lastValidValue)
                    binding.edtLimite.setSelection(lastValidValue.length)
                    isUpdating = false
                    return
                }

                // Substitui ponto por vírgula para consistência
                cleanString = cleanString.replace(".", ",")
                val isPartialInput = cleanString.isEmpty() || cleanString == "," || cleanString.endsWith(",") || cleanString.count { it == ',' } == 0 || cleanString.substringAfter(",").length < 2

                try {
                    if (!isPartialInput) {
                        val parsedValue = cleanString.replace(",", ".").toDoubleOrNull()
                        if (parsedValue != null && cleanString.substringAfter(",").length <= 2) {
                            val formattedValue = decimalFormat.format(parsedValue)
                            binding.edtLimite.setText(formattedValue)
                            binding.edtLimite.setSelection(formattedValue.length)
                        } else {
                            binding.edtLimite.setText(cleanString)
                            binding.edtLimite.setSelection(cleanString.length)
                        }
                    } else {
                        binding.edtLimite.setText(cleanString)
                        binding.edtLimite.setSelection(cleanString.length)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@CadastroRecursosActivity, "Formato inválido", Toast.LENGTH_SHORT).show()
                    binding.edtLimite.setText(lastValidValue)
                    binding.edtLimite.setSelection(lastValidValue.length)
                }

                isUpdating = false
            }
        }

        binding.edtLimite.addTextChangedListener(limiteTextWatcher)

        binding.edtLimite.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.edtLimite.text.isEmpty()) {
                binding.edtLimite.setText("R$ ")
                binding.edtLimite.setSelection(binding.edtLimite.text.length)
            }
        }
    }

    private fun addRecurso() {
        lifecycleScope.launch(Dispatchers.Main) {
            val codigo = binding.edtCodigo.text.toString().trim()
            val nomebco = binding.edtNome.text.toString().trim()
            val agencia = binding.edtAgencia.text.toString().trim().takeIf { it.isNotEmpty() }
            val fluxo = resources.getStringArray(R.array.fluxo_values_binary)[binding.spinnerFluxo.selectedItemPosition]
            val limiteText = binding.edtLimite.text.toString().replace("R\\$\\s?".toRegex(), "").replace(".", "").replace(",", ".")
            val limite = try {
                limiteText.toDoubleOrNull() ?: 0.0
            } catch (e: Exception) {
                Toast.makeText(this@CadastroRecursosActivity, "Valor de limite inválido", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val abertura = try {
                displayDateFormat.parse(binding.edtAbertura.text.toString())?.let { dbDateFormat.format(it) }
            } catch (e: Exception) { null }
            val encerramento = try {
                displayDateFormat.parse(binding.edtEncerramento.text.toString())?.let { dbDateFormat.format(it) } ?: "2099-12-31"
            } catch (e: Exception) { "2099-12-31" }
            val status = resources.getStringArray(R.array.status_values)[binding.spinnerStatus.selectedItemPosition]
            val fkGpPrinc = binding.spinnerGpPrinc.selectedItem?.toString()?.substringBefore(" - ")?.takeIf { it.isNotEmpty() }

            if (codigo.isEmpty() || nomebco.isEmpty() || abertura.isNullOrEmpty()) {
                Toast.makeText(this@CadastroRecursosActivity, "Campos obrigatórios: Código, Nome, Abertura!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val recurso = Recurso(
                codigo = codigo,
                nomebco = nomebco,
                agencia = agencia,
                fluxo = fluxo,
                limite = limite,
                abertura = abertura,
                encerramento = encerramento,
                status = status,
                fk_gpprinc = fkGpPrinc
            )

            val result = withContext(Dispatchers.IO) { dbHelper.insertRecurso(recurso) }
            if (result > 0) {
                Toast.makeText(this@CadastroRecursosActivity, "Recurso adicionado!", Toast.LENGTH_SHORT).show()
                clearFields()
                loadRecursos()
            } else {
                Toast.makeText(this@CadastroRecursosActivity, "Erro ao adicionar!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateRecurso() {
        lifecycleScope.launch(Dispatchers.Main) {
            selectedRecurso?.let {
                val codigo = binding.edtCodigo.text.toString().trim()
                val nomebco = binding.edtNome.text.toString().trim()
                val agencia = binding.edtAgencia.text.toString().trim().takeIf { it.isNotEmpty() }
                val fluxo = resources.getStringArray(R.array.fluxo_values_binary)[binding.spinnerFluxo.selectedItemPosition]
                val limiteText = binding.edtLimite.text.toString().replace("R\\$\\s?".toRegex(), "").replace(".", "").replace(",", ".")
                val limite = try {
                    limiteText.toDoubleOrNull() ?: 0.0
                } catch (e: Exception) {
                    Toast.makeText(this@CadastroRecursosActivity, "Valor de limite inválido", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val abertura = try {
                    displayDateFormat.parse(binding.edtAbertura.text.toString())?.let { dbDateFormat.format(it) }
                } catch (e: Exception) { null }
                val encerramento = try {
                    displayDateFormat.parse(binding.edtEncerramento.text.toString())?.let { dbDateFormat.format(it) } ?: "2099-12-31"
                } catch (e: Exception) { "2099-12-31" }
                val status = resources.getStringArray(R.array.status_values)[binding.spinnerStatus.selectedItemPosition]
                val fkGpPrinc = binding.spinnerGpPrinc.selectedItem?.toString()?.substringBefore(" - ")?.takeIf { it.isNotEmpty() }

                if (codigo.isEmpty() || nomebco.isEmpty() || abertura.isNullOrEmpty()) {
                    Toast.makeText(this@CadastroRecursosActivity, "Campos obrigatórios: Código, Nome, Abertura!", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val updatedRecurso = Recurso(
                    codigo = codigo,
                    nomebco = nomebco,
                    agencia = agencia,
                    fluxo = fluxo,
                    limite = limite,
                    abertura = abertura,
                    encerramento = encerramento,
                    status = status,
                    fk_gpprinc = fkGpPrinc
                )

                val result = withContext(Dispatchers.IO) { dbHelper.updateRecurso(codigo, updatedRecurso) }
                if (result > 0) {
                    Toast.makeText(this@CadastroRecursosActivity, "Recurso atualizado!", Toast.LENGTH_SHORT).show()
                    clearFields()
                    loadRecursos()
                } else {
                    Toast.makeText(this@CadastroRecursosActivity, "Erro ao atualizar!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteRecurso(codigo: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) { dbHelper.deleteRecurso(codigo) }
            if (result > 0) {
                Toast.makeText(this@CadastroRecursosActivity, "Recurso excluído!", Toast.LENGTH_SHORT).show()
                clearFields()
                loadRecursos()
            } else {
                Toast.makeText(this@CadastroRecursosActivity, "Erro ao excluir!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateFields() {
        selectedRecurso?.let { recurso ->
            binding.edtLimite.removeTextChangedListener(limiteTextWatcher)
            binding.edtCodigo.setText(recurso.codigo)
            binding.edtNome.setText(recurso.nomebco)
            binding.edtAgencia.setText(recurso.agencia)
            binding.spinnerFluxo.setSelection(resources.getStringArray(R.array.fluxo_values_binary).indexOf(recurso.fluxo ?: "N"))
            val decimalFormat = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
            binding.edtLimite.setText(recurso.limite?.let { decimalFormat.format(it) } ?: "")
            binding.edtAbertura.setText(recurso.abertura?.let {
                try {
                    dbDateFormat.parse(it)?.let { displayDateFormat.format(it) }
                } catch (e: Exception) { it }
            })
            binding.edtEncerramento.setText(recurso.encerramento?.let {
                try {
                    dbDateFormat.parse(it)?.let { displayDateFormat.format(it) }
                } catch (e: Exception) { it }
            })
            binding.spinnerStatus.setSelection(resources.getStringArray(R.array.status_values).indexOf(recurso.status ?: "A"))
            val fkGpPrinc = recurso.fk_gpprinc
            lifecycleScope.launch(Dispatchers.Main) {
                val gpPrincOptions = withContext(Dispatchers.IO) { dbHelper.getAllPlanosContas() }
                    .map { "${it.cod_Geral} - ${it.nome_C ?: ""}" }
                binding.spinnerGpPrinc.setSelection(gpPrincOptions.indexOfFirst { it.startsWith(fkGpPrinc ?: "") }.takeIf { it >= 0 } ?: 0)
            }
            binding.btnAddRecurso.text = getString(R.string.alterar)
            binding.btnDeleteRecurso.visibility = View.VISIBLE
            binding.edtLimite.addTextChangedListener(limiteTextWatcher)
        }
    }

    private fun clearFields() {
        binding.edtCodigo.text.clear()
        binding.edtNome.text.clear()
        binding.edtAgencia.text.clear()
        binding.spinnerFluxo.setSelection(0)
        binding.edtLimite.text.clear()
        binding.edtAbertura.text.clear()
        binding.edtEncerramento.text.clear()
        binding.spinnerStatus.setSelection(0)
        binding.spinnerGpPrinc.setSelection(0)
        binding.btnAddRecurso.text = getString(R.string.salvar)
        binding.btnDeleteRecurso.visibility = View.GONE
        selectedRecurso = null
    }

    private fun loadRecursos() {
        lifecycleScope.launch(Dispatchers.Main) {
            val recursos = withContext(Dispatchers.IO) { dbHelper.getAllRecursos() }
            adapter.updateData(recursos)
        }
    }
}