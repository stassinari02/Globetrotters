// app/src/main/java/com/example/globetrotters/viewmodel/TravelViewModel.kt
package com.example.globetrotters.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.globetrotters.database.TravelDatabase
import com.example.globetrotters.database.TravelEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * TravelViewModel espone tutte le operazioni sui TravelEntity tramite LiveData ed è responsabile
 * di lanciare coroutine per le operazioni di inserimento ed eliminazione.
 */
class TravelViewModel(application: Application) : AndroidViewModel(application) {

    // 1) Riferimento al DAO
    private val travelDao = TravelDatabase.getDatabase(application).travelDao()

    // 2) LiveData per tutti i viaggi
    val allTravels: LiveData<List<TravelEntity>> = travelDao.getAllTravels()

    // 3) Inserimento di un nuovo viaggio (sospeso)
    fun insertTravel(travel: TravelEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            travelDao.insertTravel(travel)
        }
    }

    // 4) Eliminazione di un viaggio
    fun deleteTravel(travel: TravelEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            travelDao.deleteTravel(travel)
        }
    }

    // 5) Eliminazione multipla per ID
    fun deleteTravelsByIds(ids: List<Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            travelDao.deleteTravelsByIds(ids)
        }
    }

    // 6) Recupera (una volta, non LiveData) tutti i viaggi che hanno coordinate non-nulle
    //    Se ti serve in Activity/Fragment, potrai esporre un LiveData o una callback.
    fun getTravelsWithCoordinates(onResult: (List<TravelEntity>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = travelDao.getTravelsWithCoordinates()
            // Torna in MainThread per consegnare il risultato
            launch(Dispatchers.Main) {
                onResult(list)
            }
        }
    }
}
