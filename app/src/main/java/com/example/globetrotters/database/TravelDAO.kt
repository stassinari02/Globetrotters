package com.example.globetrotters.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TravelDao {

    @Query("SELECT * FROM travels")
    fun getAllTravels(): LiveData<List<TravelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTravel(travel: TravelEntity)

    @Delete
    suspend fun deleteTravel(travel: TravelEntity)

    @Query("DELETE FROM travels WHERE id IN (:ids)")
    suspend fun deleteTravelsByIds(ids: List<Int>)

    // Nuovo: recupera solo i viaggi con latitudine e longitudine non-nulle
    @Query("SELECT * FROM travels WHERE latitude IS NOT NULL AND longitude IS NOT NULL")
    suspend fun getTravelsWithCoordinates(): List<TravelEntity>
}