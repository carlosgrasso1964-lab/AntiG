package com.receitas.app.ui

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.sqlite.db.SimpleSQLiteQuery
import com.receitas.app.ReceitASApp
import com.receitas.app.data.AppDatabase
import com.receitas.app.data.Receita
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val receitaDao = (application as ReceitASApp).database.receitaDao()
    val allReceitas: Flow<List<Receita>> = receitaDao.getAllReceitas()

    private val _restoreOk = MutableStateFlow(false)
    val restoreOk: StateFlow<Boolean> = _restoreOk.asStateFlow()

    fun updateReceita(receita: Receita) {
        viewModelScope.launch {
            receitaDao.update(receita)
        }
    }

    fun addReceita(
        nome: String,
        ingredientes: String,
        modo: String,
        tempo: String,
        obs: String,
        imagem: ByteArray?
    ) {
        viewModelScope.launch {
            receitaDao.insert(
                Receita(
                    nome = nome,
                    ingredientes = ingredientes,
                    modo = modo,
                    tempo = tempo,
                    obs = obs,
                    imagem = imagem
                )
            )
        }
    }

    fun deleteReceita(receita: Receita) {
        viewModelScope.launch {
            receitaDao.delete(receita)
        }
    }

    fun backupDatabase(outputStream: OutputStream) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val appCtx = getApplication<ReceitASApp>()
                    val db = AppDatabase.getInstance(appCtx)
                    val sqldb = db.openHelper.writableDatabase
                    sqldb.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)")).close()
                    try {
                        sqldb.execSQL("VACUUM")
                    } catch (_: Exception) { }

                    val dbFile = appCtx.getDatabasePath("receitas_database")
                    dbFile.inputStream().use { inp ->
                        outputStream.use { out ->
                            inp.copyTo(out)
                            out.flush()
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Backup conclu\u00eddo!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun restoreDatabase(inputUri: Uri) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val appCtx = getApplication<ReceitASApp>()
                    AppDatabase.closeAndReset()

                    val dbFile = appCtx.getDatabasePath("receitas_database")
                    File(dbFile.absolutePath + "-wal").delete()
                    File(dbFile.absolutePath + "-shm").delete()

                    val stream = appCtx.contentResolver.openInputStream(inputUri)
                    if (stream == null) {
                        throw IllegalStateException("Não foi possível ler o backup")
                    }
                    stream.use { inp ->
                        dbFile.outputStream().use { out ->
                            inp.copyTo(out)
                        }
                    }
                    if (!dbFile.exists() || dbFile.length() == 0L) {
                        throw IllegalStateException("Arquivo restaurado inválido")
                    }
                }
                _restoreOk.value = true
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun clearRestoreOk() {
        _restoreOk.value = false
    }
}
