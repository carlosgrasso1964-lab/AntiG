package com.carlos.finas.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.models.Recurso
import com.carlos.finas.databinding.ItemRecursoBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class RecursoAdapter(
    private var recursos: MutableList<Recurso>,
    private val onClick: (Recurso) -> Unit
) : RecyclerView.Adapter<RecursoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRecursoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recurso: Recurso, onClick: (Recurso) -> Unit) {
            binding.tvCodigo.text = "Código: ${recurso.codigo}"
            binding.tvNome.text = "Nome: ${recurso.nomebco ?: "Sem nome"}"
            val decimalFormat = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
            binding.tvNome.append(" | Limite: ${recurso.limite?.let { decimalFormat.format(it) } ?: "R$ 0,00"}")
            binding.root.setOnClickListener { onClick(recurso) }
        }
    }

    fun updateData(newData: List<Recurso>) {
        recursos.clear()
        recursos.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecursoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(recursos[position], onClick)
    }

    override fun getItemCount(): Int = recursos.size
}