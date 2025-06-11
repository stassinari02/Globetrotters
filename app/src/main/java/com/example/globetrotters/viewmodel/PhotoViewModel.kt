package com.example.globetrotters.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.globetrotters.database.PhotoEntity
import com.example.globetrotters.database.TravelDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * PhotoViewModel gestisce le foto di ciascun viaggio. Espone una LiveData<List<PhotoEntity>>
 * per un travelId specifico e i metodi per inserire/eliminare.
 */
class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    // 1) Riferimento al DAO per le foto
    private val photoDao = TravelDatabase.getDatabase(application).photoDao()

    // 2) LiveData: restituisce tutte le foto collegate a un determinato travelId
    fun getPhotosForTravel(travelId: Int): LiveData<List<PhotoEntity>> {
        return photoDao.getPhotosForTravel(travelId)
    }

    // 3) Inserimento di una nuova foto
    fun insertPhoto(photo: PhotoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            photoDao.insertPhoto(photo)
        }
    }

    // 4) Eliminazione di una foto
    fun deletePhoto(photo: PhotoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            photoDao.deletePhoto(photo)
        }
    }
}
