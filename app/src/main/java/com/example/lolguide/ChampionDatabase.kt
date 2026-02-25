package com.example.lolguide

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Champion::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ChampionDatabase : RoomDatabase() {

    abstract fun championDao(): ChampionDao

    companion object {
        @Volatile
        private var INSTANCE: ChampionDatabase? = null

        fun getInstance(context: Context): ChampionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChampionDatabase::class.java,
                    "champion_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}