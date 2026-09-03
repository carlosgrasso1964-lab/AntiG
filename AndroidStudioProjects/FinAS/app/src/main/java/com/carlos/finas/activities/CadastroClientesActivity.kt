package com.carlos.finas.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlos.finas.adapters.ClienteAdapter
import com.carlos.finas.models.Clifor
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.R
import com.carlos.finas.databinding.ActivityCadastroClientesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CadastroClientesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCadastroClientesBinding
    private lateinit var dbHelper: DatabaseHelper
    private var editingCodCliFor: String? = null
    private var currentCodCliFor: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityCadastroClientesBinding>(this, R.layout.activity_cadastro_clientes)
        dbHelper = DatabaseHelper(this)

        // Carregar dados para edição, se necessário
        intent.getStringExtra("codCliFor")?.let { codCliFor ->
            currentCodCliFor = codCliFor
            loadClifor(codCliFor)
        }

        binding.recyclerViewClientes.layoutManager = LinearLayoutManager(this)
        setupSpinners()
        updateRecyclerView()

        binding.btnSalvar.setOnClickListener { saveClifor() }
        binding.btnCancelar.setOnClickListener {
            clearFields()
            editingCodCliFor = null
        }
        binding.btnDeletar.setOnClickListener {
            editingCodCliFor?.let { codCliFor ->
                lifecycleScope.launch(Dispatchers.Main) {
                    val result = withContext(Dispatchers.IO) { dbHelper.deleteClifor(codCliFor) }
                    if (result > 0) {
                        Toast.makeText(this@CadastroClientesActivity, "Favorecido deletado!", Toast.LENGTH_SHORT).show()
                        clearFields()
                        editingCodCliFor = null
                        updateRecyclerView()
                    } else {
                        Toast.makeText(this@CadastroClientesActivity, "Erro ao deletar!", Toast.LENGTH_SHORT).show()
                    }
                }
            } ?: Toast.makeText(this, "Selecione um favorecido!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinners() {
        lifecycleScope.launch(Dispatchers.Main) {
            // Tipo
            val tipoOptions = resources.getStringArray(R.array.tipo_options).toList()
            binding.spinnerTipo.adapter = ArrayAdapter(
                this@CadastroClientesActivity,
                android.R.layout.simple_spinner_item,
                tipoOptions
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            // Listener para sugerir código ao mudar o tipo
            binding.spinnerTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (editingCodCliFor == null) { // Só sugere se não está editando
                        lifecycleScope.launch(Dispatchers.Main) {
                            val suggestedCode = withContext(Dispatchers.IO) {
                                if (tipoOptions[position].startsWith("CLI")) {
                                    dbHelper.suggestClienteCode()
                                } else if (tipoOptions[position].startsWith("FOR")) {
                                    dbHelper.suggestFornecedorCode()
                                } else {
                                    "" // Para "Selecione" ou outro
                                }
                            }
                            binding.edtCodCliFor.setText(suggestedCode)
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                    binding.edtCodCliFor.text.clear()
                }
            }

            // Estado
            val estados = resources.getStringArray(R.array.estado_options).toList()
            binding.spinnerEstado.adapter = ArrayAdapter(
                this@CadastroClientesActivity,
                android.R.layout.simple_spinner_item,
                estados
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            // fkCliForGp
            val gpPrincipalList = withContext(Dispatchers.IO) { dbHelper.getAllPlanosContas() }
            val gpPrincipalNames = listOf("Selecione o vínculo") + gpPrincipalList.map { "${it.cod_Geral} - ${it.nome_C ?: ""}" }
            binding.spinnerFkCliForGp.adapter = ArrayAdapter(
                this@CadastroClientesActivity,
                android.R.layout.simple_spinner_item,
                gpPrincipalNames
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }
    }

    private fun saveClifor() {
        val codCliFor = binding.edtCodCliFor.text.toString().trim()
        val tipoPosition = binding.spinnerTipo.selectedItemPosition
        val tipo = if (tipoPosition > 0) resources.getStringArray(R.array.tipo_options)[tipoPosition].substring(0, 3) else null
        val nomeCliFor = binding.edtNomeCliFor.text.toString().trim()
        val apelidoCliFor = binding.edtApelidoCliFor.text.toString().trim().takeIf { it.isNotEmpty() }
        val email = binding.edtEmail.text.toString().trim().takeIf { it.isNotEmpty() }
        val celular = binding.edtCelular.text.toString().trim().takeIf { it.isNotEmpty() }
        val telefone = binding.edtTelefone.text.toString().trim().takeIf { it.isNotEmpty() }
        val endereco = binding.edtEndereco.text.toString().trim().takeIf { it.isNotEmpty() }
        val numeroText = binding.edtNumero.text.toString().trim()
        val complemento = binding.edtComplemento.text.toString().trim().takeIf { it.isNotEmpty() }
        val bairro = binding.edtBairro.text.toString().trim().takeIf { it.isNotEmpty() }
        val cidade = binding.edtCidade.text.toString().trim().takeIf { it.isNotEmpty() }
        val estadoPosition = binding.spinnerEstado.selectedItemPosition
        val estado = if (estadoPosition > 0) resources.getStringArray(R.array.estado_options)[estadoPosition] else null
        val cep = binding.edtCep.text.toString().trim().takeIf { it.isNotEmpty() }
        val rg = binding.edtRg.text.toString().trim().takeIf { it.isNotEmpty() }
        val cpf = binding.edtCpf.text.toString().trim().takeIf { it.isNotEmpty() }
        val contatoCliFor = binding.edtContatoCliFor.text.toString().trim().takeIf { it.isNotEmpty() }
        val obs = binding.edtObs.text.toString().trim().takeIf { it.isNotEmpty() }
        val fkCliForGpPosition = binding.spinnerFkCliForGp.selectedItemPosition

        lifecycleScope.launch(Dispatchers.Main) {
            val gpPrincipalList = withContext(Dispatchers.IO) { dbHelper.getAllPlanosContas() }
            val fkCliForGp = if (fkCliForGpPosition > 0) gpPrincipalList.getOrNull(fkCliForGpPosition - 1)?.cod_Geral else null

            // Validar campos obrigatórios
            if (codCliFor.isEmpty() || tipo.isNullOrEmpty() || nomeCliFor.isEmpty()) {
                Toast.makeText(this@CadastroClientesActivity, "Campos obrigatórios!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Validar formato do código
            val codeInt = codCliFor.toIntOrNull()
            if (codeInt == null || codCliFor.length > 4) {
                Toast.makeText(this@CadastroClientesActivity, "Código inválido, use até 4 dígitos", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Validar faixa do código (apenas na inclusão, não na edição)
            if (editingCodCliFor == null) {
                if (tipo == "FOR" && (codeInt < 0 || codeInt > 999)) {
                    Toast.makeText(this@CadastroClientesActivity, "Código de fornecedor deve estar entre 0000 e 0999", Toast.LENGTH_SHORT).show()
                    return@launch
                } else if (tipo == "CLI" && codeInt < 1000) {
                    Toast.makeText(this@CadastroClientesActivity, "Código de cliente deve ser maior ou igual a 1000", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }

            // Validar unicidade (exceto se for edição do mesmo código)
            if (editingCodCliFor != codCliFor && !withContext(Dispatchers.IO) { dbHelper.isCodeUnique(codCliFor) }) {
                Toast.makeText(this@CadastroClientesActivity, "Código já está em uso!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Converter numero para Int, com valor padrão 0
            val numero = numeroText.toIntOrNull() ?: 0
            if (numeroText.isNotEmpty() && numero == 0 && numeroText != "0") {
                Toast.makeText(this@CadastroClientesActivity, "Número inválido, use apenas dígitos", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val clifor = Clifor(
                codCliFor = codCliFor,
                Tipo = tipo,
                nomeCliFor = nomeCliFor,
                apelidoCliFor = apelidoCliFor,
                email = email,
                celular = celular,
                telefone = telefone,
                endereco = endereco,
                numero = numero,
                complemento = complemento,
                bairro = bairro,
                cidade = cidade,
                estado = estado,
                cep = cep,
                rg = rg,
                cpf = cpf,
                contatoCliFor = contatoCliFor,
                obs = obs,
                fkCliForGp = fkCliForGp
            )

            val success = withContext(Dispatchers.IO) {
                if (editingCodCliFor == null) {
                    dbHelper.insertClifor(clifor) != -1L
                } else {
                    dbHelper.updateClifor(editingCodCliFor!!, clifor) > 0
                }
            }

            if (success) {
                Toast.makeText(
                    this@CadastroClientesActivity,
                    if (editingCodCliFor == null) "Favorecido inserido!" else "Favorecido atualizado!",
                    Toast.LENGTH_SHORT
                ).show()
                clearFields()
                editingCodCliFor = null
                updateRecyclerView()
                binding.recyclerViewClientes.visibility = View.VISIBLE
                binding.scrollView.post {
                    binding.scrollView.smoothScrollTo(0, 0)
                }
            } else {
                Toast.makeText(this@CadastroClientesActivity, "Erro ao salvar!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateRecyclerView() {
        lifecycleScope.launch(Dispatchers.Main) {
            val favorecidos = withContext(Dispatchers.IO) { dbHelper.getAllClifors() }
            val gpPrincipalList = withContext(Dispatchers.IO) { dbHelper.getAllPlanosContas() }
            val gpPrincipalNames = listOf("Selecione o vínculo") + gpPrincipalList.map { "${it.cod_Geral} - ${it.nome_C ?: ""}" }

            Log.d("CadastroClientes", "Número de favorecidos carregados: ${favorecidos.size}")
            favorecidos.forEach { Log.d("CadastroClientes", "Favorecido: ${it.nomeCliFor}, Cod: ${it.codCliFor}") }

            binding.recyclerViewClientes.adapter = ClienteAdapter(favorecidos) { favorecido ->
                editingCodCliFor = favorecido.codCliFor
                binding.edtCodCliFor.setText(favorecido.codCliFor)
                binding.edtNomeCliFor.setText(favorecido.nomeCliFor)
                binding.edtApelidoCliFor.setText(favorecido.apelidoCliFor)
                binding.edtEmail.setText(favorecido.email)
                binding.edtCelular.setText(favorecido.celular)
                binding.edtTelefone.setText(favorecido.telefone)
                binding.edtEndereco.setText(favorecido.endereco)
                binding.edtNumero.setText(favorecido.numero.toString())
                binding.edtComplemento.setText(favorecido.complemento)
                binding.edtBairro.setText(favorecido.bairro)
                binding.edtCidade.setText(favorecido.cidade)
                binding.edtCep.setText(favorecido.cep)
                binding.edtRg.setText(favorecido.rg)
                binding.edtCpf.setText(favorecido.cpf)
                binding.edtContatoCliFor.setText(favorecido.contatoCliFor)
                binding.edtObs.setText(favorecido.obs)
                val tipoOptions = resources.getStringArray(R.array.tipo_options).toList()
                binding.spinnerTipo.setSelection(tipoOptions.indexOfFirst {
                    it.startsWith(
                        favorecido.Tipo ?: ""
                    )
                }.takeIf { it >= 0 } ?: 0)
                val estados = resources.getStringArray(R.array.estado_options).toList()
                binding.spinnerEstado.setSelection(
                    estados.indexOf(favorecido.estado ?: "").takeIf { it >= 0 } ?: 0)
                binding.spinnerFkCliForGp.setSelection(
                    gpPrincipalNames.indexOfFirst { it.startsWith("${favorecido.fkCliForGp ?: ""} - ") }
                        .takeIf { it >= 0 } ?: 0
                )
                binding.btnSalvar.text = getString(R.string.alterar)
                // Role para o topo (campos) ao selecionar um item
                binding.scrollView.post {
                    binding.scrollView.smoothScrollTo(0, 0)
                }
            }
        }
    }

    private fun clearFields() {
        binding.edtCodCliFor.text.clear()
        binding.edtNomeCliFor.text.clear()
        binding.edtApelidoCliFor.text.clear()
        binding.edtEmail.text.clear()
        binding.edtCelular.text.clear()
        binding.edtTelefone.text.clear()
        binding.edtEndereco.text.clear()
        binding.edtNumero.text.clear()
        binding.edtComplemento.text.clear()
        binding.edtBairro.text.clear()
        binding.edtCidade.text.clear()
        binding.edtCep.text.clear()
        binding.edtRg.text.clear()
        binding.edtCpf.text.clear()
        binding.edtContatoCliFor.text.clear()
        binding.edtObs.text.clear()
        binding.spinnerTipo.setSelection(0)
        binding.spinnerEstado.setSelection(0)
        binding.spinnerFkCliForGp.setSelection(0)
        binding.btnSalvar.text = getString(R.string.salvar)
        binding.scrollView.post {
            binding.scrollView.smoothScrollTo(0, 0)
        }
    }

    private fun loadClifor(codCliFor: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            val clifor = withContext(Dispatchers.IO) {
                dbHelper.getAllClifors().firstOrNull { it.codCliFor == codCliFor }
            }
            val gpPrincipalList = withContext(Dispatchers.IO) { dbHelper.getAllPlanosContas() }
            val gpPrincipalNames = listOf("Selecione o vínculo") + gpPrincipalList.map { "${it.cod_Geral} - ${it.nome_C ?: ""}" }
            clifor?.let {
                editingCodCliFor = it.codCliFor
                binding.edtCodCliFor.setText(it.codCliFor)
                binding.edtNomeCliFor.setText(it.nomeCliFor)
                binding.edtApelidoCliFor.setText(it.apelidoCliFor)
                binding.edtEmail.setText(it.email)
                binding.edtCelular.setText(it.celular)
                binding.edtTelefone.setText(it.telefone)
                binding.edtCep.setText(it.cep)
                binding.edtEndereco.setText(it.endereco)
                binding.edtNumero.setText(it.numero.toString())
                binding.edtComplemento.setText(it.complemento)
                binding.edtBairro.setText(it.bairro)
                binding.edtCidade.setText(it.cidade)
                binding.edtRg.setText(it.rg)
                binding.edtCpf.setText(it.cpf)
                binding.edtContatoCliFor.setText(it.contatoCliFor)
                binding.edtObs.setText(it.obs)
                val tipoOptions = resources.getStringArray(R.array.tipo_options).toList()
                binding.spinnerTipo.setSelection(tipoOptions.indexOfFirst { item -> item.startsWith(it.Tipo ?: "") }.takeIf { it >= 0 } ?: 0)
                val estados = resources.getStringArray(R.array.estado_options).toList()
                binding.spinnerEstado.setSelection(estados.indexOf(it.estado ?: "").takeIf { it >= 0 } ?: 0)
                binding.spinnerFkCliForGp.setSelection(
                    gpPrincipalNames.indexOfFirst { item -> it.fkCliForGp?.let { fk -> item.startsWith("$fk - ") } ?: false }.takeIf { it >= 0 } ?: 0
                )
                binding.btnSalvar.text = getString(R.string.alterar)
            }
        }
    }
}