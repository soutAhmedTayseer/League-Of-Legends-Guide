package com.venom7t.lolguide.domain.followed.repository

import com.venom7t.lolguide.domain.followed.model.FollowedSummoner
import com.venom7t.lolguide.domain.summoner.model.Summoner
import kotlinx.coroutines.flow.Flow

interface FollowedSummonerRepository {
    fun observeFollowed(): Flow<List<FollowedSummoner>>
    suspend fun isFollowed(puuid: String): Boolean
    suspend fun follow(summoner: Summoner)
    suspend fun unfollow(puuid: String)
}
