package com.venom7t.lolguide.domain.livegame.usecase

import com.venom7t.lolguide.domain.livegame.model.LiveGame
import com.venom7t.lolguide.domain.livegame.repository.LiveGameRepository
import com.venom7t.lolguide.domain.onboarding.model.Region
import javax.inject.Inject

class GetLiveGameUseCase @Inject constructor(
    private val repository: LiveGameRepository,
) {
    suspend operator fun invoke(puuid: String, region: Region): Result<LiveGame?> =
        repository.getLiveGame(puuid, region)
}
