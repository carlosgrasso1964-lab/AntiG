package com.genas.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "people",
    foreignKeys = [
        ForeignKey(
            entity = Family::class,
            parentColumns = ["id"],
            childColumns = ["familyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("familyId"),
        Index("parentId")
    ]
)
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val familyId: Long,
    val name: String,
    val role: String = "Membro",
    val parentId: Long? = null
)
