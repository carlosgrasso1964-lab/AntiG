package com.receitas.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.receitas.app.data.Receita
import com.receitas.app.databinding.ItemReceitaBinding

class ReceitaAdapter(
    private val onDeleteClick: (Receita) -> Unit,
    private val onEditClick: (Receita) -> Unit,
    private val onItemClick: (Receita) -> Unit
) : ListAdapter<Receita, ReceitaAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReceitaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemReceitaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(receita: Receita) {
            binding.tvNome.text = receita.nome
            binding.tvIngredientes.text = receita.ingredientes
            binding.tvTempo.text = "\u23F1 ${receita.tempo}"

            if (receita.imagem != null) {
                binding.ivReceita.load(receita.imagem)
                binding.ivReceita.visibility = android.view.View.VISIBLE
            } else {
                binding.ivReceita.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener { onItemClick(receita) }
            binding.btnEditar.setOnClickListener { onEditClick(receita) }
            binding.btnExcluir.setOnClickListener { onDeleteClick(receita) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Receita>() {
        override fun areItemsTheSame(oldItem: Receita, newItem: Receita) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Receita, newItem: Receita) =
            oldItem.id == newItem.id &&
            oldItem.nome == newItem.nome &&
            oldItem.ingredientes == newItem.ingredientes &&
            oldItem.modo == newItem.modo &&
            oldItem.tempo == newItem.tempo &&
            oldItem.obs == newItem.obs
    }
}
