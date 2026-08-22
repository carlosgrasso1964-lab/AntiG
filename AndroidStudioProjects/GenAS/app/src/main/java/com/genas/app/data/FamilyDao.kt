package com.genas.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {
    @Query("SELECT * FROM families ORDER BY createdAt DESC")
    fun getAllFamilies(): Flow<List<Family>>

    @Query("SELECT * FROM families LIMIT 1")
    suspend fun getFirstFamily(): Family?

    @Query("SELECT * FROM families WHERE id = :id")
    suspend fun getFamilyById(id: Long): Family?

    @Insert
    suspend fun insert(family: Family): Long

    @Update
    suspend fun update(family: Family)

    @Delete
    suspend fun delete(family: Family)

    @Query("DELETE FROM families")
    suspend fun deleteAll()
}
