package com.venom7t.lolguide.data.champion.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ChampionDao {

    /**
     * Emits again whenever the table changes, which is what lets a refresh
     * update an already-visible list without the screen re-requesting it.
     */
    @Query("SELECT * FROM champions")
    fun observeAll(): Flow<List<ChampionEntity>>

    @Query("SELECT * FROM champions WHERE id = :championId LIMIT 1")
    suspend fun getById(championId: String): ChampionEntity?

    @Query("SELECT COUNT(*) FROM champions")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(champions: List<ChampionEntity>)

    @Query("DELETE FROM champions")
    suspend fun clear()

    /**
     * Replaces the whole table in one transaction.
     *
     * The cache holds exactly one patch at a time. Merging instead of
     * replacing would leave champions removed or renamed in a later patch
     * sitting alongside current ones with no way to tell them apart
     * (AGENTS.md §1).
     */
    @Transaction
    suspend fun replaceAll(champions: List<ChampionEntity>) {
        clear()
        insertAll(champions)
    }
}
