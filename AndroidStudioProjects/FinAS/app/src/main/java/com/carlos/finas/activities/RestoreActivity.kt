package com.carlos.finas.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.databinding.ActivityRestoreBinding

class RestoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRestoreBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        binding.btnRestore.setOnClickListener {
            startRestoreFilePicker()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun startRestoreFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/x-sqlite3" // Correção: use 'type' diretamente na instância
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/x-sqlite3", "application/octet-stream"))
        }
        restoreLauncher.launch(intent)
    }

    private val restoreLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    // Persistir permissão para o Uri
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        if (dbHelper.restoreDatabase(inputStream)) {
                            Toast.makeText(this, "Restauração concluída com sucesso", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Falha ao restaurar o backup", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Toast.makeText(this, "Erro ao abrir o arquivo de backup", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao restaurar: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("RestoreActivity", "Erro ao processar Uri: $uri, ${e.message}", e)
                }
            } ?: run {
                Toast.makeText(this, "Nenhum arquivo selecionado", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Restauração cancelada", Toast.LENGTH_SHORT).show()
        }
    }
}