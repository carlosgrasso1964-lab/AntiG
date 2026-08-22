package com.carlos.notAS

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var edtTitle: EditText
    private lateinit var edtContent: EditText
    private var noteId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)

        edtTitle = findViewById(R.id.edtTitle)
        edtContent = findViewById(R.id.edtContent)
        dbHelper = DatabaseHelper(this)

        noteId = intent.getIntExtra("note_id", -1).takeIf { it != -1 }

        if (noteId != null) {
            edtTitle.setText(intent.getStringExtra("note_title"))
            edtContent.setText(intent.getStringExtra("note_content"))
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val title = edtTitle.text.toString()
            val content = edtContent.text.toString()
            if (noteId != null) {
                dbHelper.updateNote(Note(noteId!!, title, content))
            } else {
                dbHelper.insertNote(Note(title = title, content = content))
            }
            setResult(RESULT_OK)
            finish()
        }
    }
}