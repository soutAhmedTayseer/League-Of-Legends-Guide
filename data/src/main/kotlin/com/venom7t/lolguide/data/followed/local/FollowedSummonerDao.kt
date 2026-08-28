package com.venom7t.lolguide.data.followed.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowedSummonerDao {

    @Query("SELECT * FROM followed_summoners ORDER BY followedAtEpochMillis DESC")
    fun observeAll(): Flow<List<FollowedSummonerEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM followed_summoners WHERE puuid = :puuid)")
    suspend fun isFollowed(puuid: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FollowedSummonerEntity)

    @Query("DELETE FROM followed_summoners WHERE puuid = :puuid")
    suspend fun delete(puuid: String)
}
