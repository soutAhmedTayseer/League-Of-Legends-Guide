package com.example.lolguide.domain.champion.usecase

import com.example.lolguide.domain.champion.model.ChampionStatCalculator
import com.example.lolguide.domain.champion.model.ChampionStats
import com.example.lolguide.domain.champion.model.ScaledStats
import javax.inject.Inject

class GetChampionStatsAtLevelUseCase @Inject constructor() {
    operator fun invoke(stats: ChampionStats, level: Int): ScaledStats =
        ChampionStatCalculator.statsAtLevel(stats, level)
}
