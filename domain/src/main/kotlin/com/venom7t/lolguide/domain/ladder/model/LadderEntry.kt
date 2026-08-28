package com.venom7t.lolguide.domain.ladder.model

/**
 * One entry on the Challenger/Grandmaster/Master ladder.
 *
 * @param summonerName Riot deprecated returning this on the ladder endpoint
 *   for privacy reasons on some API versions -- it may be absent. [puuid] is
 *   the one field guaranteed present and is what a tap-through to a profile
 *   should use, not the name.
 */
data class LadderEntry(
    val rank: Int,
    val puuid: String,
    val summonerName: String?,
    val leaguePoints: Int,
    val wins: Int,
    val losses: Int,
) {
    val winRatePercent: Int
        get() {
            val total = wins + losses
            return if (total == 0) 0 else (wins * 100) / total
        }
}
