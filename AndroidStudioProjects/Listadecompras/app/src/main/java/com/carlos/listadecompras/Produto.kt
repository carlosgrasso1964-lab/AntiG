package com.carlos.listadecompras

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produtos")
data class Produto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val quantidade: Int,
    val valor: Double,
    val foto: Bitmap? = null
)
