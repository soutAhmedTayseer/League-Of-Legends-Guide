package com.example.lolguide

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChampionDao {
    @Query("SELECT * FROM champions")
    suspend fun getAllChampions(): List<Champion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(champions: List<Champion>)

    @Query("DELETE FROM champions")
    suspend fun clearAll()
}