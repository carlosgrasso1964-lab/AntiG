package com.carlos.finas.adapters

import android.graphics.Color
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

data class FluxoCaixa(
    val idMov: String?,
    val recurso: String?,
    val clifor: String?,
    val dtEmi: String?,
    val dtVcto: String?,
    val diaSemana: String?,
    val documento: String?,
    val descr: String?,
    val valor: Double,
    val prev: String?,
    val saldo: Double?
)

class FluxoCaixaAdapter private constructor(
    private var fluxos: List<FluxoCaixa>,
    private val onItemClick: (FluxoCaixa) -> Unit
) : RecyclerView.Adapter<FluxoCaixaAdapter.FluxoCaixaViewHolder>() {

    private val decimalFormat = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))

    class FluxoCaixaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIdMov: TextView = itemView.findViewById(R.id.tvIdMov)
        val tvRecurso: TextView = itemView.findViewById(R.id.tvRecurso)
        val tvClifor: TextView = itemView.findViewById(R.id.tvClifor)
        val tvDtEmi: TextView = itemView.findViewById(R.id.tvDtEmi)
        val tvDtVcto: TextView = itemView.findViewById(R.id.tvDtVcto)
        val tvDiaSemana: TextView = itemView.findViewById(R.id.tvDiaSemana)
        val tvDocumento: TextView = itemView.findViewById(R.id.tvDocumento)
        val tvDescr: TextView = itemView.findViewById(R.id.tvDescr)
        val tvValor: TextView = itemView.findViewById(R.id.tvValor)
        val tvPrev: TextView = itemView.findViewById(R.id.tvPrev)
        val tvSaldo: TextView = itemView.findViewById(R.id.tvSaldo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FluxoCaixaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fluxo_caixa, parent, false)
        return FluxoCaixaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FluxoCaixaViewHolder, position: Int) {
        val fluxo = fluxos[position]
        holder.tvIdMov.text = fluxo.idMov ?: "-"
        holder.tvRecurso.text = fluxo.recurso ?: "-"
        holder.tvClifor.text = fluxo.clifor ?: "-"
        holder.tvDtEmi.text = fluxo.dtEmi ?: "-"
        holder.tvDtVcto.text = fluxo.dtVcto ?: "-"
        holder.tvDiaSemana.text = fluxo.diaSemana ?: "-"
        holder.tvDocumento.text = fluxo.documento ?: "-"
        holder.tvDescr.text = fluxo.descr ?: "-"
        holder.tvValor.text = decimalFormat.format(fluxo.valor)
        holder.tvPrev.text = fluxo.prev ?: "-"
        holder.tvSaldo.text = fluxo.saldo?.let { decimalFormat.format(it) } ?: ""

        // Colorir sábados e domingos em vermelho
        if (fluxo.diaSemana?.lowercase() in listOf("sáb", "dom")) {
            holder.tvDiaSemana.setTextColor(Color.RED)
        } else {
           //holder.tvDiaSemana.setTextColor(Color.BLACK)
            holder.tvDiaSemana.setTextColor(Color.YELLOW)
        }

        holder.itemView.setOnClickListener { onItemClick(fluxo) }
    }

    override fun getItemCount(): Int = fluxos.size

    fun getFluxos(): List<FluxoCaixa> = fluxos

    fun updateFluxos(newFluxos: List<FluxoCaixa>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = fluxos.size
            override fun getNewListSize() = newFluxos.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                fluxos[oldItemPosition].idMov == newFluxos[newItemPosition].idMov
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                fluxos[oldItemPosition] == newFluxos[newItemPosition]
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        fluxos = newFluxos
        diffResult.dispatchUpdatesTo(this)
    }

    companion object {
        fun create(fluxos: List<FluxoCaixa>, onItemClick: (FluxoCaixa) -> Unit): FluxoCaixaAdapter {
            return FluxoCaixaAdapter(fluxos, onItemClick)
        }
    }
}