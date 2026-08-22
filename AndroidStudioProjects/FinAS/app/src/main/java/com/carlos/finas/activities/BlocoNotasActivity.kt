package com.carlos.finas.activities

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.carlos.finas.databinding.ActivityBlocoNotasBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BlocoNotasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlocoNotasBinding
    private var currentFileUri: android.net.Uri? = null

    private val openFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.also { uri ->
                currentFileUri = uri
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        binding.etConteudo.setText(reader.readText())
                        reader.close()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao abrir arquivo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val saveFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        uri?.let {
            currentFileUri = it
            saveFile()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlocoNotasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNovo.setOnClickListener {
            binding.etConteudo.text.clear()
            currentFileUri = null
        }

        binding.btnAbrir.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
            }
            openFileLauncher.launch(intent)
        }

        binding.btnSalvar.setOnClickListener {
            if (currentFileUri == null) {
                openSaveDialog()
            } else {
                saveFile()
            }
        }

        binding.btnSalvarComo.setOnClickListener {
            openSaveDialog()
        }

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun openSaveDialog() {
        val fileName = "Nota_${System.currentTimeMillis()}.txt"
        saveFileLauncher.launch(fileName)
    }

    private fun saveFile() {
        currentFileUri?.let { uri ->
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(binding.etConteudo.text.toString())
                    }
                }
                Toast.makeText(this, "Arquivo salvo com sucesso!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao salvar arquivo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}