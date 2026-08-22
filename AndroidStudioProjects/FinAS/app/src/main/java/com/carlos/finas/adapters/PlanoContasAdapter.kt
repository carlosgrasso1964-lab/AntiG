package com.carlos.finas.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.models.PlanoContas
import com.carlos.finas.R

class PlanoContasAdapter(
    private val planos: List<PlanoContas>,
    private val onItemClick: (PlanoContas) -> Unit
) : RecyclerView.Adapter<PlanoContasAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtCodGeral: TextView = itemView.findViewById(R.id.txtCodGeral)
        val txtNomeC: TextView = itemView.findViewById(R.id.txtNomeC)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_plano_contas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val plano = planos[position]
        holder.txtCodGeral.text = plano.cod_Geral
        holder.txtNomeC.text = plano.nome_C
        holder.itemView.setOnClickListener { onItemClick(plano) }
    }

    override fun getItemCount(): Int = planos.size
}