package com.carlos.notAS

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "notes.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE notes (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS notes")
        onCreate(db)
    }

    fun insertNote(note: Note): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", note.title)
            put("content", note.content)
        }
        return db.insert("notes", null, values)
    }

    fun getAllNotes(): List<Note> {
        val list = mutableListOf<Note>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM notes ORDER BY id DESC", null)
        while (cursor.moveToNext()) {
            list.add(
                Note(
                    id = cursor.getInt(0),
                    title = cursor.getString(1),
                    content = cursor.getString(2)
                )
            )
        }
        cursor.close()
        return list
    }

    fun updateNote(note: Note): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", note.title)
            put("content", note.content)
        }
        return db.update("notes", values, "id=?", arrayOf(note.id.toString()))
    }

    fun deleteNote(id: Int): Int {
        val db = writableDatabase
        return db.delete("notes", "id=?", arrayOf(id.toString()))
    }
}