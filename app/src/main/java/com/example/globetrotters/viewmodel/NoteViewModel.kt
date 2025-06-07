// app/src/main/java/com/example/globetrotters/viewmodel/NoteViewModel.kt
package com.example.globetrotters.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.globetrotters.database.NoteEntity
import com.example.globetrotters.database.TravelDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * NoteViewModel gestisce le note di ciascun viaggio. Espone LiveData per leggere/scrivere
 * e funzionalità di ricerca.
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    // 1) Riferimento al DAO per le note
    private val noteDao = TravelDatabase.getDatabase(application).noteDao()

    // 2) Inserimento di una nota
    fun insertNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.insertNote(note)
        }
    }

    // 3) Recupera tutte le note per un determinato travelId
    fun getNotesForTravel(travelId: Int): LiveData<List<NoteEntity>> {
        return noteDao.getNotesForTravel(travelId)
    }

    // 4) Ricerca di note per travelId e testo contenuto
    fun searchNotes(travelId: Int, query: String): LiveData<List<NoteEntity>> {
        return noteDao.searchNotes(travelId, query)
    }

    // 5) Eliminazione di una singola nota
    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.deleteNote(note)
        }
    }

    // 6) Eliminazione multipla di note per lista di ID
    fun deleteNotesByIds(ids: List<Int>) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.deleteNotesByIds(ids)
        }
    }
}
