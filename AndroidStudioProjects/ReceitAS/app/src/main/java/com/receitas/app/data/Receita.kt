package com.receitas.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receitas")
data class Receita(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val ingredientes: String,
    val modo: String,
    val tempo: String,
    val obs: String,
    val imagem: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Receita
        if (id != other.id) return false
        if (nome != other.nome) return false
        if (ingredientes != other.ingredientes) return false
        if (modo != other.modo) return false
        if (tempo != other.tempo) return false
        if (obs != other.obs) return false
        if (imagem != null) {
            if (other.imagem == null) return false
            if (!imagem.contentEquals(other.imagem)) return false
        } else if (other.imagem != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + nome.hashCode()
        result = 31 * result + ingredientes.hashCode()
        result = 31 * result + modo.hashCode()
        result = 31 * result + tempo.hashCode()
        result = 31 * result + obs.hashCode()
        result = 31 * result + (imagem?.contentHashCode() ?: 0)
        return result
    }
}
