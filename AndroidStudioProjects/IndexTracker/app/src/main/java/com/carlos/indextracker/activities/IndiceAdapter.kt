package com.carlos.indextracker.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.carlos.indextracker.R
import com.carlos.indextracker.database.Indice

class IndiceAdapter(
    private val onEditClick: ((Indice) -> Unit)? = null,
    private val onDeleteClick: ((Long) -> Unit)? = null
) : ListAdapter<Indice, IndiceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_indice, parent, false)
        return ViewHolder(view, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View,
        private val onEditClick: ((Indice) -> Unit)? = null,
        private val onDeleteClick: ((Long) -> Unit)? = null
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvNome: TextView = itemView.findViewById(R.id.tvNome)
        private val tvAtivo: TextView = itemView.findViewById(R.id.tvAtivo)
        private val tvFonte: TextView = itemView.findViewById(R.id.tvFonte)
        private val ibDelete: ImageView = itemView.findViewById(R.id.ibDelete)

        fun bind(item: Indice) {
            tvNome.text = item.nome
            tvAtivo.text = "${item.ativo}"
            tvFonte.text = item.fonte
            ibDelete.setOnClickListener { onDeleteClick?.invoke(item.id) }
            itemView.setOnClickListener { onEditClick?.invoke(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Indice>() {
        override fun areItemsTheSame(oldItem: Indice, newItem: Indice): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Indice, newItem: Indice): Boolean =
            oldItem.nome == newItem.nome &&
                oldItem.ativo == newItem.ativo &&
                oldItem.fonte == newItem.fonte &&
                oldItem.sufixo == newItem.sufixo &&
                oldItem.cor == newItem.cor
    }
}