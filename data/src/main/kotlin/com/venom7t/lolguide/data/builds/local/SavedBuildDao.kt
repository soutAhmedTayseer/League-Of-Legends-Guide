package com.venom7t.lolguide.data.builds.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedBuildDao {

    @Query("SELECT * FROM saved_builds WHERE championId = :championId ORDER BY savedAtEpochMillis DESC")
    fun observeForChampion(championId: String): Flow<List<SavedBuildEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_builds WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Query("SELECT * FROM saved_builds WHERE id = :id")
    suspend fun getById(id: String): SavedBuildEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(build: SavedBuildEntity)

    @Query("DELETE FROM saved_builds WHERE id = :id")
    suspend fun delete(id: String)
}
