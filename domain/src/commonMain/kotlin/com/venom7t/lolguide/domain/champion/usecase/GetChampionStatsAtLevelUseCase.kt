package com.venom7t.lolguide.domain.champion.usecase

import com.venom7t.lolguide.domain.champion.model.ChampionStatCalculator
import com.venom7t.lolguide.domain.champion.model.ChampionStats
import com.venom7t.lolguide.domain.champion.model.ScaledStats
import javax.inject.Inject

class GetChampionStatsAtLevelUseCase @Inject constructor() {
    operator fun invoke(stats: ChampionStats, level: Int): ScaledStats =
        ChampionStatCalculator.statsAtLevel(stats, level)
}
