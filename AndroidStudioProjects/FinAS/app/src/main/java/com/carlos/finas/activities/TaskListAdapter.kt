package com.carlos.finas.activities

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.Tarefa
import com.carlos.finas.databinding.ItemTaskBinding

class TaskListAdapter(
    private val onToggle: (Tarefa) -> Unit,
    private val onDelete: (Tarefa) -> Unit
) : RecyclerView.Adapter<TaskListAdapter.VH>() {

    private val items = mutableListOf<Tarefa>()

    fun submit(list: List<Tarefa>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        with(holder.binding) {
            tvTaskDescription.text = t.descricao
            cbStatus.isChecked = t.concluida

            if (t.concluida) {
                tvTaskDescription.paintFlags =
                    tvTaskDescription.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvTaskDescription.setTextColor(
                    root.context.getColor(com.carlos.finas.R.color.completed_text)
                )
            } else {
                tvTaskDescription.paintFlags = 0
                tvTaskDescription.setTextColor(
                    root.context.getColor(com.carlos.finas.R.color.day_text)
                )
            }

            cbStatus.setOnClickListener { onToggle(t) }
            btnDeleteTask.setOnClickListener { onDelete(t) }
        }
    }
}
