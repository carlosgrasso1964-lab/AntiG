package com.antig.agendAS

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TaskStorage(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getTasks(dateKey: String): MutableList<Task> {
        val json = prefs.getString(dateKey, null) ?: return mutableListOf()
        val type = object : TypeToken<List<Task>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun saveTasks(dateKey: String, tasks: List<Task>) {
        prefs.edit().putString(dateKey, gson.toJson(tasks)).apply()
    }

    fun hasTasks(dateKey: String): Boolean {
        return getTasks(dateKey).isNotEmpty()
    }

    fun getAllDateKeys(): Set<String> {
        return prefs.all.keys
    }

    companion object {
        private const val PREFS_NAME = "agenda_tasks"
    }
}
