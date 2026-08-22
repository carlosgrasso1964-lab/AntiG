package com.antig.agendAS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onToggle: (Int) -> Unit,
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.checkbox.isChecked = task.completed
        holder.textView.text = task.text
        holder.textView.paint.isStrikeThruText = task.completed
        holder.textView.alpha = if (task.completed) 0.5f else 1f

        holder.checkbox.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                onToggle(currentPos)
            }
        }
        holder.btnEdit.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                onEdit(currentPos)
            }
        }
        holder.btnDelete.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                onDelete(currentPos)
            }
        }
    }

    override fun getItemCount(): Int = tasks.size

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: CheckBox = view.findViewById(R.id.taskCheckbox)
        val textView: TextView = view.findViewById(R.id.taskText)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditTask)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteTask)
    }
}
