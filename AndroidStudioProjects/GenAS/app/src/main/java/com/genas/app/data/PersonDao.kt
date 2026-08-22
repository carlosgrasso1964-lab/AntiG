package com.genas.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM people WHERE familyId = :familyId ORDER BY id ASC")
    fun getPeopleByFamily(familyId: Long): Flow<List<Person>>

    @Query("SELECT * FROM people WHERE familyId = :familyId ORDER BY id ASC")
    suspend fun getPeopleByFamilyList(familyId: Long): List<Person>

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getPersonById(id: Long): Person?

    @Query("SELECT * FROM people WHERE familyId = :familyId AND parentId IS NULL ORDER BY id ASC LIMIT 1")
    suspend fun getRootPerson(familyId: Long): Person?

    @Query("SELECT * FROM people WHERE parentId = :parentId")
    suspend fun getChildren(parentId: Long): List<Person>

    @Insert
    suspend fun insert(person: Person): Long

    @Update
    suspend fun update(person: Person)

    @Delete
    suspend fun delete(person: Person)

    @Query("DELETE FROM people WHERE familyId = :familyId")
    suspend fun deleteByFamily(familyId: Long)

    @Query("UPDATE people SET parentId = NULL WHERE parentId = :personId")
    suspend fun clearParentId(personId: Long)
}
