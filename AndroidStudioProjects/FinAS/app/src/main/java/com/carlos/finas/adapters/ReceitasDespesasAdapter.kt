package com.carlos.finas.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class ReceitaDespesa(
    val principal: String?,
    val grupo: String?,
    val conta: String?,
    val total: Double?,
    val acumulado: Double?,
    val percentual: Double?,
    val percentualHistorico: Double? // Novo campo para média histórica
)

class ReceitasDespesasAdapter private constructor(
    private var items: List<ReceitaDespesa>,
    private val onItemClick: (ReceitaDespesa) -> Unit
) : RecyclerView.Adapter<ReceitasDespesasAdapter.ViewHolder>() {

    private val decimalFormat = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
    private val percentFormat = DecimalFormat("0.0000%", DecimalFormatSymbols(Locale("pt", "BR")))

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPrincipal: TextView = itemView.findViewById(R.id.tv_principal)
        val tvGrupo: TextView = itemView.findViewById(R.id.tv_grupo)
        val tvConta: TextView = itemView.findViewById(R.id.tv_conta)
        val tvTotal: TextView = itemView.findViewById(R.id.tv_total)
        val tvAcumulado: TextView = itemView.findViewById(R.id.tv_acumulado)
        val tvPercentual: TextView = itemView.findViewById(R.id.tv_percentual)
        val tvPercentualHistorico: TextView = itemView.findViewById(R.id.tv_percentual_historico) // Novo TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_receitas_despesas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvPrincipal.text = item.principal ?: ""
        holder.tvGrupo.text = item.grupo ?: ""
        holder.tvConta.text = item.conta ?: ""
        holder.tvTotal.text = item.total?.let { decimalFormat.format(it) } ?: ""
        holder.tvAcumulado.text = item.acumulado?.let { decimalFormat.format(it) } ?: ""
        holder.tvPercentual.text = item.percentual?.let { percentFormat.format(it / 100) } ?: ""
        holder.tvPercentualHistorico.text = item.percentualHistorico?.let { percentFormat.format(it / 100) } ?: "" // Vincular percentual histórico
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun getItems(): List<ReceitaDespesa> = items

    fun updateItems(newItems: List<ReceitaDespesa>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                items[oldItemPosition].conta == newItems[newItemPosition].conta
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                items[oldItemPosition] == newItems[newItemPosition]
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    companion object {
        fun create(items: List<ReceitaDespesa>, onItemClick: (ReceitaDespesa) -> Unit): ReceitasDespesasAdapter {
            return ReceitasDespesasAdapter(items, onItemClick)
        }
    }
}