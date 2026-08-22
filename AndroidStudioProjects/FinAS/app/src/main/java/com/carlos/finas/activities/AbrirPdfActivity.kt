package com.carlos.finas.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.carlos.finas.databinding.ActivityAbrirPdfBinding

class AbrirPdfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAbrirPdfBinding

    private val openPdfLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.also { uri ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "Abrir PDF com"))
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao abrir PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAbrirPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelecionarPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            openPdfLauncher.launch(intent)
        }

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }
}