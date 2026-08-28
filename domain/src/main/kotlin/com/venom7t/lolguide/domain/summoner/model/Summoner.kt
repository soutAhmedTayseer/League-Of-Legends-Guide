package com.venom7t.lolguide.domain.summoner.model

import com.venom7t.lolguide.domain.onboarding.model.Region

/**
 * A resolved Riot account, joining ACCOUNT-V1 (identity) and SUMMONER-V4
 * (profile) into the one thing every other Phase 4 screen keys off.
 *
 * [puuid] is the identifier every other endpoint in this phase wants --
 * match history, mastery, live game -- so it is resolved once here rather
 * than re-derived per screen.
 */
data class Summoner(
    val puuid: String,
    val riotIdName: String,
    val riotIdTagline: String,
    val summonerLevel: Long,
    val profileIconId: Int,
    val region: Region,
) {
    /** The "Name#TAG" form players actually type and recognise. */
    val riotId: String get() = "$riotIdName#$riotIdTagline"
}

/**
 * One queue's ranked standing. A summoner can hold several -- solo/duo and
 * flex are reported as separate entries by LEAGUE-V4 -- so this models one
 * queue, not "the" rank.
 */
data class RankedEntry(
    val queueType: RankedQueue,
    val tier: String,
    val rank: String,
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

enum class RankedQueue(val riotQueueType: String) {
    SOLO_DUO("RANKED_SOLO_5x5"),
    FLEX("RANKED_FLEX_SR"),
    ;

    companion object {
        fun fromRiotQueueType(value: String): RankedQueue? =
            entries.firstOrNull { it.riotQueueType == value }
    }
}
