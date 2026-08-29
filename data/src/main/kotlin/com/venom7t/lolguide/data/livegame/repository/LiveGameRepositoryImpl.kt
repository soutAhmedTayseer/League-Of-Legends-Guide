package com.venom7t.lolguide.data.livegame.repository

import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.common.toAppError
import com.venom7t.lolguide.data.riot.remote.RiotApi
import com.venom7t.lolguide.data.riot.remote.RiotApiUrls
import com.venom7t.lolguide.domain.common.AppError
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.livegame.model.LiveGame
import com.venom7t.lolguide.domain.livegame.model.LiveGameParticipant
import com.venom7t.lolguide.domain.livegame.repository.LiveGameRepository
import com.venom7t.lolguide.domain.onboarding.model.Region
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class LiveGameRepositoryImpl constructor(
    private val api: RiotApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LiveGameRepository {

    override suspend fun getLiveGame(puuid: String, region: Region): Result<LiveGame?> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                val dto = api.getActiveGameByPuuid(RiotApiUrls.activeGameByPuuid(region.platformId, puuid))
                LiveGame(
                    gameId = dto.gameId,
                    gameStartEpochMillis = dto.gameStartTime,
                    queueId = dto.gameQueueConfigId,
                    participants = dto.participants.map { p ->
                        LiveGameParticipant(
                            puuid = p.puuid,
                            riotIdName = p.riotIdName,
                            riotIdTagline = p.riotIdTagline,
                            championId = p.championId.toString(),
                            teamId = p.teamId,
                            summonerSpell1Id = p.spell1Id,
                            summonerSpell2Id = p.spell2Id,
                        )
                    },
                )
            }.recoverCatching { throwable ->
                val appError = throwable.toAppError()
                // Riot answers "not in a game" with a 404, a normal, expected
                // outcome here -- not a lookup failure. Every other failure
                // still propagates.
                if (appError is AppError.NotFound) return@recoverCatching null
                throw appError
            }
        }
}
