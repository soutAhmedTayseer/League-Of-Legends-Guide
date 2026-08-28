package com.venom7t.lolguide.data.followed.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "followed_summoners")
data class FollowedSummonerEntity(
    @PrimaryKey val puuid: String,
    val riotIdName: String,
    val riotIdTagline: String,
    val regionName: String,
    val followedAtEpochMillis: Long,
)
