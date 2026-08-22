package com.receitas.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Receita::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receitaDao(): ReceitaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "receitas_database"
                )
                    .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeAndReset() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}
