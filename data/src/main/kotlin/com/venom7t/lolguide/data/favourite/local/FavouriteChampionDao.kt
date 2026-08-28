package com.venom7t.lolguide.data.favourite.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteChampionDao {

    @Query("SELECT championId FROM favourite_champions ORDER BY favouritedAtEpochMillis DESC")
    fun observeIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_champions WHERE championId = :championId)")
    suspend fun isFavourite(championId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(favourite: FavouriteChampionEntity)

    @Query("DELETE FROM favourite_champions WHERE championId = :championId")
    suspend fun remove(championId: String)
}
