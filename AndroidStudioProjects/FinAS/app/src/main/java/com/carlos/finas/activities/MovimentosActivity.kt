package com.carlos.finas.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.carlos.finas.R
import com.carlos.finas.databinding.ActivityMovimentosBinding

class MovimentosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovimentosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startTime = System.currentTimeMillis()
        binding = ActivityMovimentosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("MovimentosActivity", "Layout inflado em ${System.currentTimeMillis() - startTime}ms")
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
        binding.btnLancamentos.setOnClickListener {
            val intent = Intent(this, LancamentosActivity::class.java)
            startActivity(intent)
        }

        binding.btnTransferencias.setOnClickListener {
            val intent = Intent(this, TransferenciasActivity::class.java)
            startActivity(intent)
        }

        binding.btnParcelamentos.setOnClickListener {
            //Toast.makeText(this, R.string.parcelamentos_em_desenvolvimento, Toast.LENGTH_SHORT).show()
            Toast.makeText(this, "Parcelamentos", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ParcelamentosActivity::class.java)
            startActivity(intent)
        }

        binding.btnParcelCartoes.setOnClickListener {
            //Toast.makeText(this, R.string.parcel_cartoes_em_desenvolvimento, Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ParcelamentosCartoesActivity::class.java)
            startActivity(intent)
        }
    }
}