package com.example.globetrotters.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TravelEntity::class], version = 2, exportSchema = false)
abstract class TravelDatabase : RoomDatabase() {

    abstract fun travelDao(): TravelDao

    companion object {
        @Volatile
        private var INSTANCE: TravelDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Aggiunge le due nuove colonne
                db.execSQL("ALTER TABLE travels ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE travels ADD COLUMN longitude REAL")
            }
        }

        fun getDatabase(context: Context): TravelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TravelDatabase::class.java,
                    "travel_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}