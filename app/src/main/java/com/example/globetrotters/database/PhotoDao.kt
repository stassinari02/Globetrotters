package com.example.globetrotters.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE travelId = :tid")
    fun getPhotosForTravel(tid: Int): LiveData<List<PhotoEntity>>

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
}
