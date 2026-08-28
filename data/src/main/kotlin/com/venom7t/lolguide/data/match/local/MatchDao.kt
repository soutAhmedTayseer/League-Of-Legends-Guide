package com.venom7t.lolguide.data.match.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MatchDao {

    @Query("SELECT * FROM cached_matches WHERE matchId = :matchId LIMIT 1")
    suspend fun getById(matchId: String): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: MatchEntity)
}
