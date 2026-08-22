package com.carlos.finas.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.carlos.finas.databinding.ActivityUtilitariosBinding
import com.carlos.finas.R

class UtilitariosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUtilitariosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUtilitariosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyBackgroundImage()

        // Botão para Abrir PDF
        binding.btnAbrirPdf.setOnClickListener {
            startActivity(Intent(this, AbrirPdfActivity::class.java))
        }

        // Botão para Calendário
        binding.btnCalendario.setOnClickListener {
            startActivity(Intent(this, CalendarioActivity::class.java))
        }

        // Botão para Agenda
        binding.btnAgenda.setOnClickListener {
            startActivity(Intent(this, AgendaActivity::class.java))
        }

        // Botão para Bloco de Notas
        binding.btnBlocoNotas.setOnClickListener {
            startActivity(Intent(this, BlocoNotasActivity::class.java))
        }

        // Botão para voltar ao Menu Principal
        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun applyBackgroundImage() {
        val sharedPrefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val defaultImageId = R.drawable.f11 // Imagem padrão
        val imageId = sharedPrefs.getInt("background_image_id", defaultImageId)
        binding.ivBackground.setImageResource(imageId)
    }
}