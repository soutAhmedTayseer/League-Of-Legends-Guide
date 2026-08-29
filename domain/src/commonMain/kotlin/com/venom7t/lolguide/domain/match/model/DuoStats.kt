package com.venom7t.lolguide.domain.match.model

/**
 * Win rate playing alongside one specific teammate, aggregated purely from
 * matches this app already has cached for the viewing summoner.
 *
 * Explicitly derived and explicitly partial, same discipline as
 * [DerivedChampionStats] (AGENTS.md §1): it only knows about matches this
 * app has fetched and cached, not a summoner's entire history, so
 * [sampleSize] must always be shown alongside the win rate.
 */
data class DuoStats(
    val teammatePuuid: String,
    val teammateRiotIdName: String,
    val teammateRiotIdTagline: String,
    val sampleSize: Int,
    val wins: Int,
) {
    val winRatePercent: Int get() = if (sampleSize == 0) 0 else (wins * 100) / sampleSize
}
