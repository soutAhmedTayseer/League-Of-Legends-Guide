package com.venom7t.lolguide.data.rotation.repository

import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.common.toAppError
import com.venom7t.lolguide.data.riot.remote.RiotApi
import com.venom7t.lolguide.data.riot.remote.RiotApiUrls
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.rotation.model.ChampionRotation
import com.venom7t.lolguide.domain.rotation.repository.RotationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RotationRepositoryImpl @Inject constructor(
    private val api: RiotApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : RotationRepository {

    override suspend fun getCurrentRotation(region: Region): Result<ChampionRotation> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                val dto = api.getChampionRotation(RiotApiUrls.championRotation(region.platformId))
                ChampionRotation(
                    championIds = dto.freeChampionIds,
                    newPlayerChampionIds = dto.freeChampionIdsForNewPlayers,
                    maxNewPlayerLevel = dto.maxNewPlayerLevel,
                )
            }.recoverCatching { throwable -> throw throwable.toAppError() }
        }
}
