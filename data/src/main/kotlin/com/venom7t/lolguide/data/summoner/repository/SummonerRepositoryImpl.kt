package com.venom7t.lolguide.data.summoner.repository

import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.common.toAppError
import com.venom7t.lolguide.data.riot.remote.RiotApi
import com.venom7t.lolguide.data.riot.remote.RiotApiUrls
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.summoner.model.RankedEntry
import com.venom7t.lolguide.domain.summoner.model.RankedQueue
import com.venom7t.lolguide.domain.summoner.model.Summoner
import com.venom7t.lolguide.domain.summoner.repository.SummonerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummonerRepositoryImpl @Inject constructor(
    private val api: RiotApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SummonerRepository {

    override suspend fun searchByRiotId(
        name: String,
        tagline: String,
        region: Region,
    ): Result<Summoner> = withContext(ioDispatcher) {
        runCatchingCancellable {
            // Two real requests on two different routing hosts (Phase 4
            // plan §Region routing) -- ACCOUNT-V1 resolves the identity,
            // SUMMONER-V4 resolves the profile that identity maps to.
            val account = api.getAccountByRiotId(
                RiotApiUrls.accountByRiotId(region.regionalRoute, name, tagline)
            )
            val summoner = api.getSummonerByPuuid(
                RiotApiUrls.summonerByPuuid(region.platformId, account.puuid)
            )

            Summoner(
                puuid = account.puuid,
                summonerId = summoner.id,
                riotIdName = account.gameName,
                riotIdTagline = account.tagLine,
                summonerLevel = summoner.summonerLevel,
                profileIconId = summoner.profileIconId,
                region = region,
            )
        }.recoverCatching { throwable -> throw throwable.toAppError() }
    }

    override suspend fun getRankedEntries(summoner: Summoner): Result<List<RankedEntry>> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                // LEAGUE-V4's by-summoner path is keyed by the encrypted
                // summonerId (SUMMONER-V4's `id` field), not puuid, unlike
                // most of the rest of this API -- it just has not been
                // migrated to puuid routing the way match/mastery/spectator
                // have.
                api.getLeagueEntriesBySummoner(
                    RiotApiUrls.leagueEntriesBySummoner(
                        summoner.region.platformId,
                        summoner.summonerId,
                    )
                ).mapNotNull { dto ->
                    val queue = RankedQueue.fromRiotQueueType(dto.queueType) ?: return@mapNotNull null
                    RankedEntry(
                        queueType = queue,
                        tier = dto.tier,
                        rank = dto.rank,
                        leaguePoints = dto.leaguePoints,
                        wins = dto.wins,
                        losses = dto.losses,
                    )
                }
            }.recoverCatching { throwable -> throw throwable.toAppError() }
        }
}
