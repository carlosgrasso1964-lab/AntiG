package com.carlos.finas.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.Tarefa
import com.carlos.finas.databinding.DialogTaskBinding
import com.carlos.finas.R

class TaskDialogFragment : DialogFragment() {

    private var _binding: DialogTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var dateKey: String
    private lateinit var storage: TaskStorage
    private lateinit var adapter: TaskListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_FinasApp_Dialog)
        dateKey = arguments?.getString(ARG_DATE) ?: ""
        storage = TaskStorage(com.carlos.finas.DatabaseHelper(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDialogTitle.text = getString(R.string.tarefas_do_dia) + " — $dateKey"

        adapter = TaskListAdapter(
            onToggle = { tarefa ->
                val novo = !tarefa.concluida
                storage.setCompleted(tarefa.id, novo)
                refresh()
                (requireActivity() as? CalendarioActivity)?.refreshCalendar()
            },
            onDelete = { tarefa ->
                storage.delete(tarefa.id)
                refresh()
                (requireActivity() as? CalendarioActivity)?.refreshCalendar()
            }
        )

        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter

        refresh()

        binding.btnAddTask.setOnClickListener {
            val txt = binding.etNewTask.text.toString().trim()
            if (txt.isEmpty()) {
                Toast.makeText(requireContext(), "Digite uma tarefa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            storage.saveTask(dateKey, txt)
            binding.etNewTask.text.clear()
            refresh()
            (requireActivity() as? CalendarioActivity)?.refreshCalendar()
        }

        binding.btnCancelTask.setOnClickListener { dismiss() }
    }

    private fun refresh() {
        val tarefas = storage.getTasks(dateKey)
        adapter.submit(tarefas)
        binding.rvTasks.visibility = if (tarefas.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_DATE = "arg_date"
        fun newInstance(dateKey: String): TaskDialogFragment =
            TaskDialogFragment().apply {
                arguments = Bundle().apply { putString(ARG_DATE, dateKey) }
            }
    }
}
