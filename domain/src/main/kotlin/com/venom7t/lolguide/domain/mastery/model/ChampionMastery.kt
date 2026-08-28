package com.venom7t.lolguide.domain.mastery.model

data class ChampionMastery(
    val championId: String,
    val championLevel: Int,
    val championPoints: Int,
    val lastPlayTimeEpochMillis: Long,
    /** Points earned toward the next level, out of [pointsNeededForNextLevel]. Null at max level. */
    val pointsSinceLastLevel: Int,
    val pointsNeededForNextLevel: Int?,
)
