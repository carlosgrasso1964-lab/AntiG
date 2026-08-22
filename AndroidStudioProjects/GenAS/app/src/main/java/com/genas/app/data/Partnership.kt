package com.genas.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "partnerships",
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
        Index("person1Id"),
        Index("person2Id")
    ]
)
data class Partnership(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val familyId: Long,
    val person1Id: Long,
    val person2Id: Long
)
