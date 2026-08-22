package com.carlos.finas.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.carlos.finas.R
import com.carlos.finas.databinding.ActivityManutencaoBinding

class ManutencaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManutencaoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManutencaoBinding.inflate(layoutInflater)
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
        binding.btnBackup.setOnClickListener {
            val intent = Intent(this, BackupActivity::class.java)
            startActivity(intent)
        }

        binding.btnRestore.setOnClickListener {
            val intent = Intent(this, RestoreActivity::class.java)
            startActivity(intent)
        }

        binding.btnConfigurarTela.setOnClickListener {
            val intent = Intent(this, ConfigurarTelaActivity::class.java)
            startActivity(intent)
        }
    }
}