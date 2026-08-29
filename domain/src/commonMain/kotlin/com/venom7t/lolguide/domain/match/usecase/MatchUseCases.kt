package com.venom7t.lolguide.domain.match.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.match.model.DerivedChampionStats
import com.venom7t.lolguide.domain.match.model.MatchDetail
import com.venom7t.lolguide.domain.match.model.MatchSummary
import com.venom7t.lolguide.domain.match.model.MatchTimelineFrame
import com.venom7t.lolguide.domain.match.repository.MatchRepository
import com.venom7t.lolguide.domain.onboarding.model.Region

@Factory
class GetMatchHistoryUseCase(
    private val repository: MatchRepository,
) {
    suspend operator fun invoke(
        puuid: String,
        region: Region,
        count: Int = DEFAULT_PAGE_SIZE,
        startIndex: Int = 0,
    ): Result<List<MatchSummary>> = repository.getMatchHistory(puuid, region, count, startIndex)

    private companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}

@Factory
class GetMatchDetailUseCase(
    private val repository: MatchRepository,
) {
    suspend operator fun invoke(matchId: String, region: Region): Result<MatchDetail> =
        repository.getMatchDetail(matchId, region)
}

@Factory
class GetMatchTimelineUseCase(
    private val repository: MatchRepository,
) {
    suspend operator fun invoke(matchId: String, region: Region): Result<List<MatchTimelineFrame>> =
        repository.getMatchTimeline(matchId, region)
}

/**
 * Aggregates already-fetched match history into per-champion win rate and
 * average KDA (feature #6).
 *
 * Pure and synchronous: it does not fetch anything itself, only summarises
 * matches the caller already has. Every result is explicitly a
 * [DerivedChampionStats], which carries its own sample size, per the domain
 * notice on that type -- this use case must never be tempted to present a
 * derived average as if Riot published it directly.
 */
@Factory
class ComputeDerivedChampionStatsUseCase() {

    operator fun invoke(matches: List<MatchSummary>, championId: String): DerivedChampionStats? {
        val onChampion = matches.filter { it.championId == championId }
        if (onChampion.isEmpty()) return null

        return DerivedChampionStats(
            championId = championId,
            sampleSize = onChampion.size,
            wins = onChampion.count { it.win },
            averageKills = onChampion.map { it.kills }.average(),
            averageDeaths = onChampion.map { it.deaths }.average(),
            averageAssists = onChampion.map { it.assists }.average(),
            averageCsPerMinute = onChampion.map { it.csPerMinute }.average(),
        )
    }
}
