package com.carlos.finas.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.databinding.ActivityBackupBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBackupBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        binding.btnBackup.setOnClickListener {
            startBackupFilePicker()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun startBackupFilePicker() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/x-sqlite3" // Correção: use 'type' na instância
            putExtra(Intent.EXTRA_TITLE, "FinASDB_backup_$timestamp.db")
        }
        backupLauncher.launch(intent)
    }

    private val backupLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        if (dbHelper.backupDatabase(outputStream)) {
                            Toast.makeText(this, "Backup criado com sucesso: $uri", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Falha ao criar backup", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Toast.makeText(this, "Erro ao abrir o arquivo de backup", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao criar backup: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("BackupActivity", "Erro ao processar Uri: $uri, ${e.message}", e)
                }
            } ?: run {
                Toast.makeText(this, "Nenhum local selecionado", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Backup cancelado", Toast.LENGTH_SHORT).show()
        }
    }
}