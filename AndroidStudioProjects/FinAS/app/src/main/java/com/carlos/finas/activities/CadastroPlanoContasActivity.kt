package com.carlos.finas.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.models.PlanoContas
import com.carlos.finas.adapters.PlanoContasAdapter
import com.carlos.finas.databinding.ActivityCadastroPlanoContasBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CadastroPlanoContasActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCadastroPlanoContasBinding
    private lateinit var dbHelper: DatabaseHelper
    private var currentCodGeral: String? = null
    private lateinit var adapter: PlanoContasAdapter
    private var planos: MutableList<PlanoContas> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroPlanoContasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        // Configurar RecyclerView
        setupRecyclerView()

        // Configurar botão de salvar
        binding.btnSalvar.setOnClickListener {
            savePlanoContas()
        }

        // Configurar botão de excluir
        binding.btnExcluir.setOnClickListener {
            currentCodGeral?.let { deletePlanoContas(it) }
        }

        // Carregar dados para edição, se necessário
        intent.getStringExtra("codGeral")?.let { codGeral ->
            currentCodGeral = codGeral
            loadPlanoContas(codGeral)
            binding.btnExcluir.visibility = View.VISIBLE
        }

        // Carregar lista inicial de planos
        loadPlanos()
    }

    private fun setupRecyclerView() {
        adapter = PlanoContasAdapter(planos) { plano ->
            // Ao clicar em um item, carregar para edição
            currentCodGeral = plano.cod_Geral
            loadPlanoContas(plano.cod_Geral)
            binding.btnExcluir.visibility = View.VISIBLE
        }
        binding.recyclerViewPlanos.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewPlanos.adapter = adapter
    }

    private fun loadPlanos() {
        lifecycleScope.launch(Dispatchers.Main) {
            planos.clear()
            val novosPlanos = withContext(Dispatchers.IO) {
                dbHelper.getAllPlanosContas()
            }
            planos.addAll(novosPlanos)
            adapter.notifyDataSetChanged()
            Log.d("CadastroPlanoContas", "Carregados ${planos.size} planos")
        }
    }

    private fun savePlanoContas() {
        val codGeral = binding.editCodGeral.text.toString().trim()
        val nomeP = binding.edtNomeP.text.toString().trim().takeIf { it.isNotEmpty() }
        val nomeS = binding.edtNomeS.text.toString().trim().takeIf { it.isNotEmpty() }
        val nomeC = binding.edtNomeC.text.toString().trim().takeIf { it.isNotEmpty() }

        // Validar campos obrigatórios
        if (codGeral.isEmpty()) {
            Toast.makeText(this, "Código Geral é obrigatório", Toast.LENGTH_SHORT).show()
            return
        }

        val plano = PlanoContas(
            cod_Geral = codGeral,
            nome_P = nomeP,
            nome_S = nomeS,
            nome_C = nomeC
        )

        lifecycleScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                if (currentCodGeral == null) {
                    dbHelper.insertPlanoContas(plano)
                } else {
                    dbHelper.updatePlanoContas(currentCodGeral!!, plano)
                }
            }

            if (result != -1L) {
                Toast.makeText(
                    this@CadastroPlanoContasActivity,
                    if (currentCodGeral == null) "Plano de Contas salvo" else "Plano de Contas atualizado",
                    Toast.LENGTH_SHORT
                ).show()
                clearFields()
                loadPlanos() // Recarregar a lista após salvar
            } else {
                Toast.makeText(this@CadastroPlanoContasActivity, "Erro ao salvar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deletePlanoContas(codGeral: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                dbHelper.deletePlanoContas(codGeral)
            }
            if (result > 0) {
                Toast.makeText(this@CadastroPlanoContasActivity, "Plano de Contas excluído", Toast.LENGTH_SHORT).show()
                clearFields()
                loadPlanos() // Recarregar a lista após excluir
            } else {
                Toast.makeText(this@CadastroPlanoContasActivity, "Erro ao excluir", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPlanoContas(codGeral: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            val plano = withContext(Dispatchers.IO) {
                dbHelper.getAllPlanosContas().firstOrNull { it.cod_Geral == codGeral }
            }
            plano?.let {
                binding.editCodGeral.setText(it.cod_Geral)
                binding.edtNomeP.setText(it.nome_P)
                binding.edtNomeS.setText(it.nome_S)
                binding.edtNomeC.setText(it.nome_C)
                Log.d("CadastroPlanoContas", "Plano carregado: cod_Geral=${it.cod_Geral}, nome_C=${it.nome_C}")
            } ?: run {
                Toast.makeText(this@CadastroPlanoContasActivity, "Plano não encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearFields() {
        currentCodGeral = null
        binding.editCodGeral.text.clear()
        binding.edtNomeP.text.clear()
        binding.edtNomeS.text.clear()
        binding.edtNomeC.text.clear()
        binding.btnExcluir.visibility = View.GONE
    }
}