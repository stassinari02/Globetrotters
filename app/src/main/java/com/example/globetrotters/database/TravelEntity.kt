package com.example.globetrotters.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travels")
data class TravelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val dateRange: String,
    val photoUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)