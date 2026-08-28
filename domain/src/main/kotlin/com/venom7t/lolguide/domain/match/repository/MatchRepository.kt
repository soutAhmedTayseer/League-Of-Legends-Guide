package com.venom7t.lolguide.domain.match.repository

import com.venom7t.lolguide.domain.match.model.MatchDetail
import com.venom7t.lolguide.domain.match.model.MatchSummary
import com.venom7t.lolguide.domain.match.model.MatchTimelineFrame
import com.venom7t.lolguide.domain.onboarding.model.Region

interface MatchRepository {

    /**
     * @param puuid the requesting summoner's own puuid -- needed to know
     *   which participant row of each match is "this player" when building
     *   [MatchSummary].
     */
    suspend fun getMatchHistory(
        puuid: String,
        region: Region,
        count: Int,
        startIndex: Int = 0,
    ): Result<List<MatchSummary>>

    /**
     * A finished match never changes, so this reads from the permanent cache
     * first and only calls the network on a genuine miss (AGENTS.md §8.3) --
     * unlike the champion/item caches, nothing here is ever wholesale-replaced.
     */
    suspend fun getMatchDetail(matchId: String, region: Region): Result<MatchDetail>

    suspend fun getMatchTimeline(matchId: String, region: Region): Result<List<MatchTimelineFrame>>
}
