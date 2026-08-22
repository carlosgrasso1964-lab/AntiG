package com.carlos.finas.adapters

import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.models.Clifor

class ClienteAdapter(
    private val clientes: List<Clifor>,
    private val onClick: (Clifor) -> Unit
) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textNome: TextView = itemView.findViewById(R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.simple_list_item_1, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]
        holder.textNome.text = "${cliente.codCliFor} - ${cliente.nomeCliFor}"
        holder.itemView.setOnClickListener { onClick(cliente) }
    }

    override fun getItemCount(): Int = clientes.size
}