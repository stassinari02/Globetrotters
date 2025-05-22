package com.example.globetrotters.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE travelId = :tid")
    fun getNotesForTravel(tid: Int): LiveData<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE travelId = :tid AND text LIKE '%' || :query || '%'")
    fun searchNotes(tid: Int, query: String): LiveData<List<NoteEntity>>

    // Cancella una singola nota
    @Delete
    suspend fun deleteNote(note: NoteEntity)

    // Cancella più note passando la lista di ID
    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun deleteNotesByIds(ids: List<Int>)
}

