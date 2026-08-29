package com.carlos.finas.activities

import com.carlos.finas.DatabaseHelper
import com.carlos.finas.Tarefa

/**
 * Interface de storage para tarefas do calendário (padrão AgendAS).
 * Implementação atual usa SQLite (DatabaseHelper). Mantida como wrapper
 * para isolar a activity de mudanças futuras no backend.
 */
class TaskStorage(private val dbHelper: DatabaseHelper) {
    fun getTasks(dateKey: String): List<Tarefa> = dbHelper.getTarefasByData(dateKey)
    fun saveTask(dateKey: String, description: String): Long = dbHelper.addTarefa(dateKey, description)
    fun hasTasks(dateKey: String): Boolean = dbHelper.getTarefasByData(dateKey).any { !it.concluida }
    fun getAllDateKeys(): Set<String> = dbHelper.getAllDateKeysWithTasks()
    fun setCompleted(id: Int, done: Boolean): Boolean = dbHelper.updateTarefaConcluida(id, done)
    fun delete(id: Int): Boolean = dbHelper.deleteTarefa(id)
}
