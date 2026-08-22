package com.carlos.listadecompras

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProdutoDao {
    @Query("SELECT * FROM produtos")
    fun getAll(): List<Produto>

    @Insert
    fun insert(produto: Produto)

    @Update
    fun update(produto: Produto)

    @Delete
    fun delete(produto: Produto)
}
