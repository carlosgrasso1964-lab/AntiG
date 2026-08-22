package com.carlos.finas.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.carlos.finas.databinding.ActivityDashboardBinding
import com.carlos.finas.R

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyBackgroundImage()
        setupButtonListeners()
    }

    private fun applyBackgroundImage() {
        val sharedPrefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val defaultImageId = R.drawable.f11 // Imagem padrão
        val imageId = sharedPrefs.getInt("background_image_id", defaultImageId)
        binding.ivBackground.setImageResource(imageId)
    }

    private fun setupButtonListeners() {
        binding.btnCadastros.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        binding.btnMovimentacao.setOnClickListener {
            val intent = Intent(this, MovimentosActivity::class.java)
            startActivity(intent)
        }

        binding.btnConsultas.setOnClickListener {
            val intent = Intent(this, ConsultasActivity::class.java)
            startActivity(intent)
        }

        binding.btnManutencao.setOnClickListener {
            val intent = Intent(this, ManutencaoActivity::class.java)
            startActivity(intent)
        }

        binding.btnUtilitarios.setOnClickListener {
            val intent = Intent(this, UtilitariosActivity::class.java)
            startActivity(intent)
        }

        binding.btnSair.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sair")
                .setMessage("Deseja realmente sair do aplicativo?")
                .setPositiveButton("Sim") { _, _ ->
                    finishAffinity()
                }
                .setNegativeButton("Não", null)
                .show()
        }
    }
}