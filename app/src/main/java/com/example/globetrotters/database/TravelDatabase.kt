package com.example.globetrotters.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TravelEntity::class,
        PhotoEntity::class,
        NoteEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class TravelDatabase : RoomDatabase() {

    abstract fun travelDao(): TravelDao
    abstract fun photoDao(): PhotoDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: TravelDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE travels ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE travels ADD COLUMN longitude REAL")
            }
        }

        // Nuova migrazione da 2 → 3 per creare la tabella photos
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Crea la nuova tabella photos con foreign key su travels(id)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS photos (
                      id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      travelId INTEGER NOT NULL,
                      uri TEXT NOT NULL,
                      FOREIGN KEY(travelId) REFERENCES travels(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                // Indice per migliorare le query per travelId
                db.execSQL("CREATE INDEX IF NOT EXISTS index_photos_travelId ON photos(travelId)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Creazione tabella notes SENZA foreign key e SENZA indice
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS notes (
                      id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      travelId INTEGER NOT NULL,
                      text TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): TravelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TravelDatabase::class.java,
                    "travel_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
