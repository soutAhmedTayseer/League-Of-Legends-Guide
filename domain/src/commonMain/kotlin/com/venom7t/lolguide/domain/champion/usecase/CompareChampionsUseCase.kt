package com.venom7t.lolguide.domain.champion.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionStatCalculator
import com.venom7t.lolguide.domain.champion.model.ScaledStats

/**
 * Builds a side-by-side comparison at a chosen level.
 *
 * Comparing base stats alone is misleading: champions with high growth look
 * weak at level 1 and dominant at 18. Comparing at a level the user picks is
 * the only honest way to do it.
 */
@Factory
class CompareChampionsUseCase() {

    operator fun invoke(left: Champion, right: Champion, level: Int): ChampionComparison =
        ChampionComparison(
            level = level,
            left = left,
            right = right,
            leftStats = ChampionStatCalculator.statsAtLevel(left.stats, level),
            rightStats = ChampionStatCalculator.statsAtLevel(right.stats, level),
        )
}

/**
 * All stats here are **derived** by [ChampionStatCalculator], not shipped by
 * Riot, and must be labelled as such in the UI (AGENTS.md section 1).
 */
data class ChampionComparison(
    val level: Int,
    val left: Champion,
    val right: Champion,
    val leftStats: ScaledStats,
    val rightStats: ScaledStats,
)
