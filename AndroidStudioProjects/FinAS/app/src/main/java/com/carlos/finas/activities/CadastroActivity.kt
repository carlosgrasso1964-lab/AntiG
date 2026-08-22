package com.carlos.finas.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.carlos.finas.R
import com.carlos.finas.databinding.ActivityCadastroBinding

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
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
        binding.btnUsuarios.setOnClickListener {
            val intent = Intent(this, CadastroUsuariosActivity::class.java)
            startActivity(intent)
        }

        binding.btnPlanoContas.setOnClickListener {
            val intent = Intent(this, CadastroPlanoContasActivity::class.java)
            startActivity(intent)
        }

        binding.btnRecursos.setOnClickListener {
            val intent = Intent(this, CadastroRecursosActivity::class.java)
            startActivity(intent)
        }

        binding.btnFavorecidos.setOnClickListener {
            val intent = Intent(this, CadastroClientesActivity::class.java)
            startActivity(intent)
        }
    }
}
