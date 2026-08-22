package com.carlos.listadecompras

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import java.text.NumberFormat
import java.util.Locale

class ProdutoAdapter(context: Context) : ArrayAdapter<Produto>(context, 0) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_view_item, parent, false)

        val item = getItem(position)
        val txtProduto = v.findViewById<TextView>(R.id.txt_item_produto)
        val txtQtd = v.findViewById<TextView>(R.id.txt_item_qtd)
        val txtValor = v.findViewById<TextView>(R.id.txt_item_valor)
        val imgProduto = v.findViewById<ImageView>(R.id.img_item_foto)

        if (item != null) {
            txtProduto.text = item.nome
            txtQtd.text = item.quantidade.toString()
            val formatter = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())
            txtValor.text = formatter.format(item.valor)
            if (item.foto != null) {
                imgProduto.setImageBitmap(item.foto)
            } else {
                imgProduto.setImageResource(android.R.drawable.ic_menu_camera)
            }
        }

        return v
    }

}