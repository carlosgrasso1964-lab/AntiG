package com.carlos.finas.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.models.Usuario
import com.carlos.finas.databinding.ItemUsuarioBinding

class UsuarioAdapter(
    private var usuarios: List<Usuario>,
    private val onClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(
        private val binding: ItemUsuarioBinding,
        private val onClick: (Usuario) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(usuario: Usuario) {
            binding.tvId.text = usuario.id?.toString() ?: "N/A"
            binding.tvUsuario.text = usuario.usuario
            binding.root.setOnClickListener { onClick(usuario) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val binding = ItemUsuarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UsuarioViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        holder.bind(usuarios[position])
    }

    override fun getItemCount(): Int = usuarios.size

    fun updateData(newUsuarios: List<Usuario>) {
        usuarios = newUsuarios
        notifyDataSetChanged()
    }
}