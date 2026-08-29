package com.venom7t.lolguide.domain.game.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GameStats
import com.venom7t.lolguide.domain.game.repository.GameProgressRepository
import kotlinx.coroutines.flow.Flow

@Factory
class ObserveGameStatsUseCase(
    private val repository: GameProgressRepository,
) {
    operator fun invoke(mode: GameMode): Flow<GameStats> = repository.observeStats(mode)
}
