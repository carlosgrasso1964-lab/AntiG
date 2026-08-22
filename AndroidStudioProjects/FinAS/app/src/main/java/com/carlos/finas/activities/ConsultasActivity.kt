package com.carlos.finas.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.carlos.finas.R
import com.carlos.finas.databinding.ActivityConsultasBinding

class ConsultasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConsultasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConsultasBinding.inflate(layoutInflater)

        setContentView(binding.root)
        applyBackgroundImage()

        // Configurar cliques dos botões
        binding.btnRecursos.setOnClickListener {
            startActivity(Intent(this, RecursosConsultaActivity::class.java))
        }

        binding.btnFavorecidos.setOnClickListener {
            startActivity(Intent(this, FavorecidosConsultaActivity::class.java))
        }

        binding.btnClassificacao.setOnClickListener {
            startActivity(Intent(this, ConsultaClassificacaoActivity::class.java))
        }

        binding.btnSaldos.setOnClickListener {
            startActivity(Intent(this, ConsultaSaldosActivity::class.java))
        }

        binding.btnFluxoDiario.setOnClickListener {
            startActivity(Intent(this, ConsultaFluxoCaixaActivity::class.java))
        }

        binding.btnReceitasDespesas.setOnClickListener {
            startActivity(Intent(this, ReceitasDespesasConsultaActivity::class.java))
        }

        binding.btnReferenciaCruzada.setOnClickListener {
            startActivity(Intent(this, ConsultaRCActivity::class.java))
        }

        binding.btnBalanco.setOnClickListener {
            startActivity(Intent(this, BalancoConsultaActivity::class.java))
        }

        binding.btnInflacao.setOnClickListener {
            startActivity(Intent(this, InflacaoConsultaActivity::class.java))
        }

        binding.btnPainelDiario.setOnClickListener {
            startActivity(Intent(this, PainelDiarioActivity::class.java))
        }
    }

    private fun applyBackgroundImage() {
        val sharedPrefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val defaultImageId = R.drawable.f11 // Imagem padrão
        val imageId = sharedPrefs.getInt("background_image_id", defaultImageId)
        binding.ivBackground.setImageResource(imageId)
    }
}