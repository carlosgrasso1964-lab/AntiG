package com.genas.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PartnershipDao {
    @Query("SELECT * FROM partnerships WHERE familyId = :familyId")
    suspend fun getByFamily(familyId: Long): List<Partnership>

    @Query("SELECT * FROM partnerships WHERE person1Id = :personId OR person2Id = :personId")
    suspend fun getByPerson(personId: Long): List<Partnership>

    @Insert
    suspend fun insert(partnership: Partnership): Long

    @Delete
    suspend fun delete(partnership: Partnership)

    @Query("DELETE FROM partnerships WHERE person1Id = :personId OR person2Id = :personId")
    suspend fun deleteByPerson(personId: Long)

    @Query("DELETE FROM partnerships WHERE familyId = :familyId")
    suspend fun deleteByFamily(familyId: Long)
}
