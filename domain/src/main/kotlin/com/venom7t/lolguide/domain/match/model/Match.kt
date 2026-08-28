package com.venom7t.lolguide.domain.match.model

/**
 * One match, as it appears in a history list.
 *
 * A subset of [MatchDetail] scoped to the viewing summoner's own participant
 * row -- a history list renders many of these at once, so it must not carry
 * all 10 participants' full data per row.
 */
data class MatchSummary(
    val matchId: String,
    val championId: String,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val win: Boolean,
    val queueId: Int,
    val gameDurationSeconds: Int,
    val gameEndEpochMillis: Long,
    val items: List<Int>,
    val csTotal: Int,
    /** True when the fetch is a cache hit -- match data is immutable once played, per AGENTS.md §8.3. */
    val isFromCache: Boolean,
) {
    val kda: Double get() = if (deaths == 0) (kills + assists).toDouble() else (kills + assists).toDouble() / deaths
    val csPerMinute: Double
        get() = if (gameDurationSeconds == 0) 0.0 else csTotal / (gameDurationSeconds / 60.0)
}

/** Full match detail: every participant, for the match detail screen. */
data class MatchDetail(
    val matchId: String,
    val queueId: Int,
    val gameDurationSeconds: Int,
    val gameEndEpochMillis: Long,
    val participants: List<MatchParticipant>,
) {
    val blueTeam: List<MatchParticipant> get() = participants.filter { it.teamId == 100 }
    val redTeam: List<MatchParticipant> get() = participants.filter { it.teamId == 200 }
}

data class MatchParticipant(
    val puuid: String,
    val riotIdName: String,
    val riotIdTagline: String,
    val championId: String,
    val teamId: Int,
    val win: Boolean,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val items: List<Int>,
    val summonerSpell1Id: Int,
    val summonerSpell2Id: Int,
    val totalDamageDealtToChampions: Int,
    val goldEarned: Int,
    val csTotal: Int,
    val visionScore: Int,
) {
    val kda: Double get() = if (deaths == 0) (kills + assists).toDouble() else (kills + assists).toDouble() / deaths
}

/**
 * A single graphable point in a match's gold/XP progression, from
 * MATCH-V5's timeline endpoint.
 */
data class MatchTimelineFrame(
    val timestampSeconds: Int,
    val totalGoldByParticipantId: Map<Int, Int>,
    val xpByParticipantId: Map<Int, Int>,
)
