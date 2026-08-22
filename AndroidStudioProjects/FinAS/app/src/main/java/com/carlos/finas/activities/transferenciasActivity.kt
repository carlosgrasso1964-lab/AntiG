package com.carlos.finas.activities

import android.R
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.models.Recurso
import com.carlos.finas.databinding.ActivityTransferenciasBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class TransferenciasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransferenciasBinding
    private lateinit var dbHelper: DatabaseHelper
    private var selectedOrigem: Recurso? = null
    private var selectedDestino: Recurso? = null
    private val recursos = mutableListOf<Recurso>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferenciasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        setupDataMask()
        loadRecursos()
        setupListeners()
    }

    private fun setupDataMask() {
        binding.edtData.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true
                val clean = s.toString().replace("[^0-9]".toRegex(), "")
                val masked = when {
                    clean.length <= 2 -> clean
                    clean.length <= 4 -> "${clean.substring(0, 2)}/${clean.substring(2)}"
                    clean.length <= 8 -> "${clean.substring(0, 2)}/${clean.substring(2, 4)}/${clean.substring(4)}"
                    else -> "${clean.substring(0, 2)}/${clean.substring(2, 4)}/${clean.substring(4, 8)}"
                }
                binding.edtData.setText(masked)
                binding.edtData.setSelection(masked.length)
                isUpdating = false
            }
        })
    }

    private fun loadRecursos() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val recursosList = withContext(Dispatchers.IO) {
                    dbHelper.getRecursos()
                }
                recursos.clear()
                recursos.addAll(recursosList)
                val adapter = ArrayAdapter(
                    this@TransferenciasActivity,
                    R.layout.simple_spinner_item,
                    recursos.map { "${it.nomebco ?: "Sem Nome"} - ${it.codigo}-${it.fk_gpprinc ?: "Sem Conta"}" }
                )
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                binding.spinnerOrigem.adapter = adapter
                binding.spinnerDestino.adapter = adapter
            } catch (e: Exception) {
                Log.e("TransferenciasActivity", "Erro ao carregar recursos: ${e.message}", e)
                Toast.makeText(
                    this@TransferenciasActivity,
                    "Erro ao carregar recursos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupListeners() {
        binding.spinnerOrigem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedOrigem = recursos.getOrNull(position)
                binding.edtOrigemCodigo.setText(selectedOrigem?.codigo ?: "")
                binding.edtOrigemClassif.setText(selectedOrigem?.fk_gpprinc ?: "")
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedOrigem = null
                binding.edtOrigemCodigo.setText("")
                binding.edtOrigemClassif.setText("")
            }
        }

        binding.spinnerDestino.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDestino = recursos.getOrNull(position)
                binding.edtDestinoCodigo.setText(selectedDestino?.codigo ?: "")
                binding.edtDestinoClassif.setText(selectedDestino?.fk_gpprinc ?: "")
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedDestino = null
                binding.edtDestinoCodigo.setText("")
                binding.edtDestinoClassif.setText("")
            }
        }

        binding.edtValor.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true
                val clean = s.toString().replace("[^0-9]".toRegex(), "")
                if (clean.isNotEmpty()) {
                    val value = clean.toDouble() / 100
                    val decimalFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale("pt", "BR")).apply {
                        groupingSeparator = '.'
                        decimalSeparator = ','
                    })
                    binding.edtValor.setText(decimalFormat.format(value))
                    binding.edtValor.setSelection(binding.edtValor.text.length)
                }
                isUpdating = false
            }
        })

        binding.btnTransferir.setOnClickListener {
            saveTransferencia()
        }

        binding.btnFechar.setOnClickListener {
            finish()
        }
    }

    private fun saveTransferencia() {
        val data = binding.edtData.text.toString()
        val documento = binding.edtDocumento.text.toString().takeIf { it.isNotEmpty() } ?: "TRANSF"
        val valorText = binding.edtValor.text.toString().replace("R\\$\\s?".toRegex(), "").replace(".", "").replace(",", ".")

        if (data.isEmpty()) {
            Toast.makeText(this, "Data é obrigatória", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedOrigem == null) {
            Toast.makeText(this, "Selecione o recurso de origem", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDestino == null) {
            Toast.makeText(this, "Selecione o recurso de destino", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedOrigem == selectedDestino) {
            Toast.makeText(this, "Origem e destino não podem ser iguais", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedOrigem!!.fk_gpprinc.isNullOrEmpty()) {
            Toast.makeText(this, "Recurso de origem não possui conta contábil (fk_gpprinc)", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDestino!!.fk_gpprinc.isNullOrEmpty()) {
            Toast.makeText(this, "Recurso de destino não possui conta contábil (fk_gpprinc)", Toast.LENGTH_SHORT).show()
            return
        }
        val valor = try {
            valorText.toDoubleOrNull() ?: run {
                Toast.makeText(this, "Insira um valor válido (ex.: 123,45)", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Valor inválido: ${e.message}", Toast.LENGTH_SHORT).show()
            return
        }

        val dataFormatted = formatDate(data)
        if (dataFormatted.isEmpty()) {
            Toast.makeText(this, "Data inválida. Use o formato dd/MM/yyyy", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val success = withContext(Dispatchers.IO) {
                    dbHelper.registrarTransferencia(
                        origemRecurso = selectedOrigem!!.codigo,
                        origemVrecurso = selectedOrigem!!.fk_gpprinc!!, // Força não nulo após validação
                        destinoRecurso = selectedDestino!!.codigo,
                        destinoVrecurso = selectedDestino!!.fk_gpprinc!!, // Força não nulo após validação
                        documento = documento,
                        data = dataFormatted,
                        valor = valor
                    )
                }
                if (success) {
                    Toast.makeText(
                        this@TransferenciasActivity,
                        "Transferência efetuada com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
                    clearFields()
                } else {
                    Toast.makeText(
                        this@TransferenciasActivity,
                        "Erro ao realizar a transferência",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("TransferenciasActivity", "Erro ao salvar transferência: ${e.message}", e)
                Toast.makeText(
                    this@TransferenciasActivity,
                    "Erro ao salvar transferência: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun clearFields() {
        binding.edtData.setText("")
        binding.edtDocumento.setText("")
        binding.spinnerOrigem.setSelection(0)
        binding.spinnerDestino.setSelection(0)
        binding.edtValor.setText("")
        binding.edtOrigemCodigo.setText("")
        binding.edtOrigemClassif.setText("")
        binding.edtDestinoCodigo.setText("")
        binding.edtDestinoClassif.setText("")
    }

    private fun formatDate(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val parsedDate = inputFormat.parse(date)
            parsedDate?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            Log.e("TransferenciasActivity", "Erro ao formatar data: $date, ${e.message}", e)
            ""
        }
    }
}