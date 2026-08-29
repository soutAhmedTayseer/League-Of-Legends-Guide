package com.venom7t.lolguide.domain.match.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.match.model.DuoStats
import com.venom7t.lolguide.domain.match.repository.MatchRepository
import com.venom7t.lolguide.domain.onboarding.model.Region

/**
 * Aggregates duo win rates from the summoner's already-fetched match
 * history: for each cached match, every other participant on the winning
 * team is one data point.
 *
 * Deliberately reads only the match cache -- it calls [MatchRepository]'s
 * detail fetch per match id from history, which is a cache hit for anything
 * already looked at (match data is immutable once played, AGENTS.md §8.3),
 * so this does not fan out fresh network calls per match under normal use.
 */
@Factory
class ComputeDuoStatsUseCase(
    private val matchRepository: MatchRepository,
) {
    suspend operator fun invoke(
        viewingPuuid: String,
        matchIds: List<String>,
        region: Region,
        minSampleSize: Int = 2,
    ): List<DuoStats> {
        val byTeammate = mutableMapOf<String, MutableList<Boolean>>()
        val namesByTeammate = mutableMapOf<String, Pair<String, String>>()

        for (matchId in matchIds) {
            val detail = matchRepository.getMatchDetail(matchId, region).getOrNull() ?: continue
            val self = detail.participants.firstOrNull { it.puuid == viewingPuuid } ?: continue
            val teammates = detail.participants.filter {
                it.teamId == self.teamId && it.puuid != viewingPuuid
            }
            for (teammate in teammates) {
                byTeammate.getOrPut(teammate.puuid) { mutableListOf() }.add(self.win)
                namesByTeammate[teammate.puuid] = teammate.riotIdName to teammate.riotIdTagline
            }
        }

        return byTeammate.mapNotNull { (puuid, results) ->
            if (results.size < minSampleSize) return@mapNotNull null
            val (name, tagline) = namesByTeammate.getValue(puuid)
            DuoStats(
                teammatePuuid = puuid,
                teammateRiotIdName = name,
                teammateRiotIdTagline = tagline,
                sampleSize = results.size,
                wins = results.count { it },
            )
        }.sortedByDescending { it.sampleSize }
    }
}
