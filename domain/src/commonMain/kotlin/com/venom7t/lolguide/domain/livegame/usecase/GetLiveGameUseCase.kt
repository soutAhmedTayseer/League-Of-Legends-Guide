package com.venom7t.lolguide.domain.livegame.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.livegame.model.LiveGame
import com.venom7t.lolguide.domain.livegame.repository.LiveGameRepository
import com.venom7t.lolguide.domain.onboarding.model.Region

@Factory
class GetLiveGameUseCase(
    private val repository: LiveGameRepository,
) {
    suspend operator fun invoke(puuid: String, region: Region): Result<LiveGame?> =
        repository.getLiveGame(puuid, region)
}
