package com.carlos.finas.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.R
import com.carlos.finas.activities.Saldo
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class SaldosAdapter private constructor(
    private val saldos: List<Saldo>,
    private val onItemClick: (Saldo) -> Unit
) : RecyclerView.Adapter<SaldosAdapter.SaldoViewHolder>() {

    private val decimalFormat = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))

    class SaldoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFonte: TextView = itemView.findViewById(R.id.tvFonte)
        val tvSaldo: TextView = itemView.findViewById(R.id.tvSaldo)
        val tvAcumulado: TextView = itemView.findViewById(R.id.tvAcumulado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaldoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saldo, parent, false)
        return SaldoViewHolder(view)
    }

    override fun onBindViewHolder(holder: SaldoViewHolder, position: Int) {
        val saldo = saldos[position]
        holder.tvFonte.text = saldo.fonte ?: "-"
        holder.tvSaldo.text = decimalFormat.format(saldo.saldo)
        holder.tvAcumulado.text = saldo.acumulado?.let { decimalFormat.format(it) } ?: "-"
        holder.itemView.setOnClickListener { onItemClick(saldo) }
    }

    override fun getItemCount(): Int = saldos.size

    fun getSaldos(): List<Saldo> = saldos

    companion object {
        fun create(saldos: List<Saldo>, onItemClick: (Saldo) -> Unit): SaldosAdapter {
            return SaldosAdapter(saldos, onItemClick)
        }
    }
}