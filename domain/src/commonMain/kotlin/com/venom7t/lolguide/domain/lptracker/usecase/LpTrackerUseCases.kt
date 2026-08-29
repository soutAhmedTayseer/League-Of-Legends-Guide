package com.venom7t.lolguide.domain.lptracker.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.followed.usecase.ObserveFollowedSummonersUseCase
import com.venom7t.lolguide.domain.lptracker.model.LpSnapshot
import com.venom7t.lolguide.domain.lptracker.repository.LpTrackerRepository
import com.venom7t.lolguide.domain.summoner.model.RankedQueue
import com.venom7t.lolguide.domain.summoner.usecase.GetRankedEntriesUseCase
import com.venom7t.lolguide.domain.summoner.usecase.SearchSummonerUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Factory
class ObserveLpHistoryUseCase(
    private val repository: LpTrackerRepository,
) {
    operator fun invoke(puuid: String, queueType: RankedQueue): Flow<List<LpSnapshot>> =
        repository.observeHistory(puuid, queueType)
}

/**
 * Result of one polling pass over every followed summoner: what changed
 * since the previous poll, per queue, so the caller (the background worker)
 * can decide whether a notification is warranted.
 */
data class LpChange(
    val puuid: String,
    val riotIdName: String,
    val riotIdTagline: String,
    val queueType: RankedQueue,
    val leaguePointsDelta: Int,
    val newTier: String,
    val newRank: String,
    val newLeaguePoints: Int,
)

/**
 * Polls ranked entries for every followed summoner, records a snapshot for
 * each queue, and returns what changed since the last poll -- this is the
 * single unit of work the LP-tracker WorkManager job runs each interval
 * (Phase 5 plan).
 */
@Factory
class PollFollowedSummonersLpUseCase(
    private val observeFollowedSummoners: ObserveFollowedSummonersUseCase,
    private val searchSummoner: SearchSummonerUseCase,
    private val getRankedEntries: GetRankedEntriesUseCase,
    private val repository: LpTrackerRepository,
) {
    suspend operator fun invoke(): List<LpChange> {
        val now = System.currentTimeMillis()
        val followed = observeFollowedSummoners().first()
        val changes = mutableListOf<LpChange>()

        for (followedSummoner in followed) {
            // The followed record only carries puuid/name/tagline/region,
            // not the encrypted summonerId LEAGUE-V4's by-summoner path
            // requires (see SummonerRepositoryImpl's comment on that quirk).
            // Re-resolving via ACCOUNT-V1+SUMMONER-V4 here is the same two
            // calls a profile-screen open already costs; there is no cache
            // that would make skipping this cheaper.
            val riotId = "${followedSummoner.riotIdName}#${followedSummoner.riotIdTagline}"
            val summoner = searchSummoner(riotId, followedSummoner.region).getOrNull() ?: continue
            val entries = getRankedEntries(summoner).getOrNull() ?: continue

            val snapshots = entries.mapNotNull { entry ->
                val queue = entry.queueType
                snapshotFor(followedSummoner.puuid, queue, entry.tier, entry.rank, entry.leaguePoints, now)
            }
            if (snapshots.isEmpty()) continue

            for (snapshot in snapshots) {
                val previous = repository.getLatestBefore(followedSummoner.puuid, snapshot.queueType, now)
                repository.recordSnapshots(followedSummoner.puuid, listOf(snapshot))
                if (previous != null && previous.leaguePoints != snapshot.leaguePoints) {
                    changes += LpChange(
                        puuid = followedSummoner.puuid,
                        riotIdName = followedSummoner.riotIdName,
                        riotIdTagline = followedSummoner.riotIdTagline,
                        queueType = snapshot.queueType,
                        leaguePointsDelta = snapshot.leaguePoints - previous.leaguePoints,
                        newTier = snapshot.tier,
                        newRank = snapshot.rank,
                        newLeaguePoints = snapshot.leaguePoints,
                    )
                }
            }
        }
        return changes
    }

    private fun snapshotFor(
        puuid: String,
        queueType: RankedQueue,
        tier: String,
        rank: String,
        leaguePoints: Int,
        now: Long,
    ) = LpSnapshot(
        puuid = puuid,
        queueType = queueType,
        tier = tier,
        rank = rank,
        leaguePoints = leaguePoints,
        capturedAtEpochMillis = now,
    )
}
