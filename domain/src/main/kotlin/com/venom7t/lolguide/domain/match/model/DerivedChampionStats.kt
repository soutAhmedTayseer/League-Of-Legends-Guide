package com.venom7t.lolguide.domain.match.model

/**
 * A summoner's win rate and average KDA on one champion, computed from their
 * own recent match history.
 *
 * **This is entirely derived, never source data.** Riot publishes no
 * "your stats on this champion" endpoint; it is computed by aggregating
 * [MatchSummary] rows already fetched for the match history screen. Per
 * `AGENTS.md` §1, this type is deliberately distinct from anything Riot
 * returns directly, and [sampleSize] is carried on the type itself so the UI
 * can and must show how many games the numbers are based on -- five games is
 * not the same claim as five hundred.
 */
data class DerivedChampionStats(
    val championId: String,
    val sampleSize: Int,
    val wins: Int,
    val averageKills: Double,
    val averageDeaths: Double,
    val averageAssists: Double,
    val averageCsPerMinute: Double,
) {
    val winRatePercent: Int get() = if (sampleSize == 0) 0 else (wins * 100) / sampleSize
    val averageKda: Double
        get() = if (averageDeaths == 0.0) {
            averageKills + averageAssists
        } else {
            (averageKills + averageAssists) / averageDeaths
        }
}
