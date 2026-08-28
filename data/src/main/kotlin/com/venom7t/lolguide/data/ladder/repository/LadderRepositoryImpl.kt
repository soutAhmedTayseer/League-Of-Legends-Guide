package com.venom7t.lolguide.data.ladder.repository

import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.common.toAppError
import com.venom7t.lolguide.data.riot.remote.RiotApi
import com.venom7t.lolguide.data.riot.remote.RiotApiUrls
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.ladder.model.LadderEntry
import com.venom7t.lolguide.domain.ladder.repository.LadderRepository
import com.venom7t.lolguide.domain.onboarding.model.Region
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LadderRepositoryImpl @Inject constructor(
    private val api: RiotApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LadderRepository {

    override suspend fun getChallengerLadder(region: Region, queue: String): Result<List<LadderEntry>> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                api.getChallengerLeague(RiotApiUrls.challengerLeague(region.platformId, queue))
                    .entries
                    // Points descending is the only order a ladder is ever
                    // shown in; Riot does not guarantee response order.
                    .sortedByDescending { it.leaguePoints }
                    .mapIndexed { index, dto ->
                        LadderEntry(
                            rank = index + 1,
                            puuid = dto.puuid,
                            summonerName = dto.summonerName,
                            leaguePoints = dto.leaguePoints,
                            wins = dto.wins,
                            losses = dto.losses,
                        )
                    }
            }.recoverCatching { throwable -> throw throwable.toAppError() }
        }
}
