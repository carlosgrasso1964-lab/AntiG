package com.antig.agendAS

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TaskDialogFragment : DialogFragment() {

    private var dateKey: String = ""
    private lateinit var storage: TaskStorage
    private var editIndex: Int? = null

    private lateinit var dialogDate: TextView
    private lateinit var taskInput: EditText
    private lateinit var btnSave: Button
    private lateinit var btnClose: Button
    private lateinit var taskList: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private var tasks = mutableListOf<Task>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_task, null)
        builder.setView(view)

        dateKey = arguments?.getString(ARG_DATE_KEY) ?: ""
        editIndex = arguments?.getInt(ARG_EDIT_INDEX, -1)?.let { if (it < 0) null else it }
        storage = TaskStorage(requireContext())

        dialogDate = view.findViewById(R.id.dialogDate)
        taskInput = view.findViewById(R.id.taskInput)
        btnSave = view.findViewById(R.id.btnSaveTask)
        btnClose = view.findViewById(R.id.btnCloseDialog)
        taskList = view.findViewById(R.id.taskList)

        dialogDate.text = dateKey
        tasks = storage.getTasks(dateKey).toMutableList()

        taskAdapter = TaskAdapter(
            tasks = tasks,
            onToggle = { pos ->
                tasks[pos] = tasks[pos].copy(completed = !tasks[pos].completed)
                storage.saveTasks(dateKey, tasks)
                taskAdapter.notifyItemChanged(pos)
            },
            onEdit = { pos ->
                taskInput.setText(tasks[pos].text)
                editIndex = pos
                btnSave.text = "Atualizar"
            },
            onDelete = { pos ->
                tasks.removeAt(pos)
                storage.saveTasks(dateKey, tasks)
                taskAdapter.notifyItemRemoved(pos)
                taskAdapter.notifyItemRangeChanged(pos, tasks.size - pos)
                if (tasks.isEmpty()) {
                    dismiss()
                    (requireActivity() as MainActivity).refreshCalendar()
                }
            }
        )

        taskList.layoutManager = LinearLayoutManager(requireContext())
        taskList.adapter = taskAdapter

        if (editIndex != null && editIndex!! < tasks.size) {
            taskInput.setText(tasks[editIndex!!].text)
            btnSave.text = "Atualizar"
        }

        btnSave.setOnClickListener {
            val text = taskInput.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            if (editIndex != null && editIndex!! < tasks.size) {
                tasks[editIndex!!] = tasks[editIndex!!].copy(text = text)
                editIndex = null
                btnSave.text = "Salvar"
            } else {
                tasks.add(Task(text))
            }

            storage.saveTasks(dateKey, tasks)
            taskInput.setText("")
            taskAdapter.notifyDataSetChanged()
        }

        btnClose.setOnClickListener {
            dismiss()
            (requireActivity() as MainActivity).refreshCalendar()
        }

        return builder.create()
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        if (activity is MainActivity) {
            (activity as MainActivity).refreshCalendar()
        }
    }

    companion object {
        private const val ARG_DATE_KEY = "date_key"
        private const val ARG_EDIT_INDEX = "edit_index"

        fun newInstance(dateKey: String, editIndex: Int? = null): TaskDialogFragment {
            val fragment = TaskDialogFragment()
            val args = Bundle()
            args.putString(ARG_DATE_KEY, dateKey)
            if (editIndex != null) args.putInt(ARG_EDIT_INDEX, editIndex)
            fragment.arguments = args
            return fragment
        }
    }
}
