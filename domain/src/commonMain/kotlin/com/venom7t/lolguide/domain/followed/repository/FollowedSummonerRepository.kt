package com.venom7t.lolguide.domain.followed.repository

import com.venom7t.lolguide.domain.followed.model.FollowedSummoner
import com.venom7t.lolguide.domain.summoner.model.Summoner
import kotlinx.coroutines.flow.Flow

interface FollowedSummonerRepository {
    fun observeFollowed(): Flow<List<FollowedSummoner>>
    suspend fun isFollowed(puuid: String): Boolean
    suspend fun follow(summoner: Summoner)

    /**
     * Follows from an already-built [FollowedSummoner] rather than a full
     * [Summoner] lookup. Used by sync (Phase 5), which only has what the
     * remote side stored -- name, tagline, region, puuid -- not a fresh
     * SUMMONER-V4 profile to build a complete [Summoner] from.
     */
    suspend fun followRaw(summoner: FollowedSummoner)
    suspend fun unfollow(puuid: String)
}
