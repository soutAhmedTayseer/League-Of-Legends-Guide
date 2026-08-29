package com.venom7t.lolguide.data.mastery.repository

import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.common.toAppError
import com.venom7t.lolguide.data.riot.remote.RiotApi
import com.venom7t.lolguide.data.riot.remote.RiotApiUrls
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.mastery.model.ChampionMastery
import com.venom7t.lolguide.domain.mastery.repository.MasteryRepository
import com.venom7t.lolguide.domain.onboarding.model.Region
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class MasteryRepositoryImpl constructor(
    private val api: RiotApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MasteryRepository {

    override suspend fun getChampionMasteries(
        puuid: String,
        region: Region,
    ): Result<List<ChampionMastery>> = withContext(ioDispatcher) {
        runCatchingCancellable {
            api.getChampionMasteries(RiotApiUrls.championMasteries(region.platformId, puuid)).map { dto ->
                ChampionMastery(
                    championId = dto.championId.toString(),
                    championLevel = dto.championLevel,
                    championPoints = dto.championPoints,
                    lastPlayTimeEpochMillis = dto.lastPlayTime,
                    pointsSinceLastLevel = dto.championPointsSinceLastLevel,
                    // Riot reports -1 at max level rather than omitting the
                    // field; normalise that into a real null so callers do
                    // not need to know the sentinel value.
                    pointsNeededForNextLevel = dto.championPointsUntilNextLevel.takeIf { it >= 0 },
                )
            }
        }.recoverCatching { throwable -> throw throwable.toAppError() }
    }
}
