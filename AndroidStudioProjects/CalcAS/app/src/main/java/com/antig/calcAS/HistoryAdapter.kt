package com.antig.calcAS

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private val entries: MutableList<HistoryEntry>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        private const val TYPE_EMPTY = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (entries.isEmpty()) TYPE_EMPTY else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_EMPTY) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history_empty, parent, false)
            return EmptyViewHolder(view)
        }
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_entry, parent, false)
        return EntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EntryViewHolder && entries.isNotEmpty()) {
            val entry = entries[position]
            holder.calcText.text = "${entry.calculation} ="
            holder.resultText.text = entry.result
        }
    }

    override fun getItemCount(): Int = if (entries.isEmpty()) 1 else entries.size

    class EntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val calcText: TextView = view.findViewById(R.id.historyCalc)
        val resultText: TextView = view.findViewById(R.id.historyResult)
    }

    class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
