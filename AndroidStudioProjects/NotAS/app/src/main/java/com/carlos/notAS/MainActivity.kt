package com.carlos.notAS

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter
    private lateinit var notes: MutableList<Note>
    private lateinit var addEditNoteLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa o launcher antes de usá-lo
        addEditNoteLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                notes.clear()
                notes.addAll(dbHelper.getAllNotes())
                adapter.notifyDataSetChanged()
            }
        }

        dbHelper = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        notes = dbHelper.getAllNotes().toMutableList()
        adapter = NoteAdapter(notes, this::editNote, this::deleteNote)
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAddNote).setOnClickListener {
            val intent = Intent(this, AddEditNoteActivity::class.java)
            addEditNoteLauncher.launch(intent)
        }
    }

    private fun editNote(note: Note) {
        val intent = Intent(this, AddEditNoteActivity::class.java).apply {
            putExtra("note_id", note.id)
            putExtra("note_title", note.title)
            putExtra("note_content", note.content)
        }
        addEditNoteLauncher.launch(intent)
    }

    private fun deleteNote(note: Note) {
        dbHelper.deleteNote(note.id)
        notes.remove(note)
        adapter.notifyDataSetChanged()
    }
}
