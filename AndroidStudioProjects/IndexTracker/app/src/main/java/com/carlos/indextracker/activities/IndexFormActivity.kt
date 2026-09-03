package com.carlos.indextracker.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.carlos.indextracker.R
import com.carlos.indextracker.database.IndexTrackerDbHelper
import com.carlos.indextracker.database.Indice
import com.carlos.indextracker.databinding.ActivityIndexFormBinding
import com.google.gson.Gson

class IndexFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndexFormBinding
    private var indiceEditando: Indice? = null
    private lateinit var db: IndexTrackerDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndexFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = IndexTrackerDbHelper(this)

        val json = intent.getStringExtra("indice_json")
        if (json != null) {
            try {
                indiceEditando = Gson().fromJson(json, Indice::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        setupUI()
        loadData()
    }

    private fun setupUI() {
        binding.tvTitle.text = if (indiceEditando != null) "Editar índice" else "Novo índice"
        binding.btnSalvar.text = if (indiceEditando != null) "Atualizar" else "Salvar"

        binding.btnSalvar.setOnClickListener { salvarIndice() }
        binding.btnCancelar.setOnClickListener { finish() }
    }

    private fun loadData() {
        val i = indiceEditando ?: return
        binding.etNome.setText(i.nome)
        binding.etAtivo.setText(i.ativo)
        binding.etFonte.setText(i.fonte)
        binding.etSufixo.setText(i.sufixo)
        binding.etCor.setText(i.cor)
    }

    private fun salvarIndice() {
        val nome = binding.etNome.text.toString().trim()
        val ativo = binding.etAtivo.text.toString().trim()
        val fonte = binding.etFonte.text.toString().trim()
        val sufixo = binding.etSufixo.text.toString().trim()
        val corStr = binding.etCor.text.toString().trim()

        if (nome.isEmpty() || ativo.isEmpty() || fonte.isEmpty() || sufixo.isEmpty() || corStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val corHex = if (!corStr.startsWith("#")) "#$corStr" else corStr

        val novo = Indice(
            id = indiceEditando?.id ?: 0L,
            nome = nome,
            ativo = ativo,
            fonte = fonte,
            sufixo = sufixo,
            cor = corHex
        )

        if (indiceEditando != null) {
            db.updateIndice(novo)
        } else {
            db.insertIndice(novo)
        }

        Toast.makeText(this, "${novo.nome} salvo", Toast.LENGTH_SHORT).show()
        setResult(1)
        finish()
    }

    override fun onBackPressed() {
        finish()
    }
}