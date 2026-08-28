package com.venom7t.lolguide.domain.game.usecase

import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GameStats
import com.venom7t.lolguide.domain.game.repository.GameProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGameStatsUseCase @Inject constructor(
    private val repository: GameProgressRepository,
) {
    operator fun invoke(mode: GameMode): Flow<GameStats> = repository.observeStats(mode)
}
