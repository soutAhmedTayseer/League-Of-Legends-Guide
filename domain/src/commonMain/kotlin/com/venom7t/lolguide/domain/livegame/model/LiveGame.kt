package com.venom7t.lolguide.domain.livegame.model

/**
 * A currently in-progress game, from SPECTATOR-V5. Only exists while the game
 * is live -- there is no "recent live game" concept, so a lookup miss means
 * the summoner is simply not in a game right now, not an error.
 */
data class LiveGame(
    val gameId: Long,
    val gameStartEpochMillis: Long,
    val queueId: Int,
    val participants: List<LiveGameParticipant>,
) {
    val blueTeam: List<LiveGameParticipant> get() = participants.filter { it.teamId == 100 }
    val redTeam: List<LiveGameParticipant> get() = participants.filter { it.teamId == 200 }
}

data class LiveGameParticipant(
    val puuid: String,
    val riotIdName: String,
    val riotIdTagline: String,
    val championId: String,
    val teamId: Int,
    val summonerSpell1Id: Int,
    val summonerSpell2Id: Int,
)
