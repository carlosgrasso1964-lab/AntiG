package com.carlos.indextracker.activities

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.carlos.indextracker.database.Indice
import com.carlos.indextracker.databinding.ItemGerenciarBinding
import com.carlos.indextracker.ui.Formato

class IndiceAdapter(
    private val onEditar: (Indice) -> Unit,
    private val onExcluir: (Indice) -> Unit
) : ListAdapter<Indice, IndiceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGerenciarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemGerenciarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(indice: Indice) {
            binding.tvNome.text = indice.nome
            binding.dotCor.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Formato.parseCor(indice.cor))
            }

            val tipo = when {
                indice.manual -> "✍️ Manual"
                indice.fonte == Indice.FONTE_MOEDA -> "🌐 API · ${indice.codigo}"
                else -> "🔗 API · URL"
            }
            binding.tvDetalhe.text = "$tipo · padrão ${Formato.valor(indice.valorPadrao, indice.casasDecimais)}${indice.sufixo}"

            binding.btnEditar.setOnClickListener { onEditar(indice) }
            binding.btnExcluir.setOnClickListener { onExcluir(indice) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Indice>() {
        override fun areItemsTheSame(oldItem: Indice, newItem: Indice) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Indice, newItem: Indice) = oldItem == newItem
    }
}
