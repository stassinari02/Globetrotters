package com.example.globetrotters.database

import androidx.room.*

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = TravelEntity::class,
            parentColumns = ["id"],
            childColumns = ["travelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("travelId")]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val travelId: Int,
    val uri: String
)
