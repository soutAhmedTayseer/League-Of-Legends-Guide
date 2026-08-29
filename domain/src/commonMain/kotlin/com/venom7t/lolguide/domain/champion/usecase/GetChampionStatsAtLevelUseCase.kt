package com.venom7t.lolguide.domain.champion.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.champion.model.ChampionStatCalculator
import com.venom7t.lolguide.domain.champion.model.ChampionStats
import com.venom7t.lolguide.domain.champion.model.ScaledStats

@Factory
class GetChampionStatsAtLevelUseCase() {
    operator fun invoke(stats: ChampionStats, level: Int): ScaledStats =
        ChampionStatCalculator.statsAtLevel(stats, level)
}
