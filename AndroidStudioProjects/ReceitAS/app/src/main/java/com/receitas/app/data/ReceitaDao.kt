package com.receitas.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceitaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(receita: Receita): Long

    @Update
    suspend fun update(receita: Receita)

    @Delete
    suspend fun delete(receita: Receita)

    @Query("SELECT * FROM receitas ORDER BY nome COLLATE NOCASE ASC")
    fun getAllReceitas(): Flow<List<Receita>>
}
