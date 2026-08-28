package com.venom7t.lolguide.data.riot.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Riot API wire types. Field names match Riot's JSON, including the
 * unfortunate historical mix of `puuid`/`summonerId`/`accountId` across
 * endpoints -- Riot's own inconsistency, not this codebase's.
 */

@Serializable
data class AccountDto(
    val puuid: String = "",
    val gameName: String = "",
    val tagLine: String = "",
)

@Serializable
data class SummonerDto(
    val puuid: String = "",
    /** The encrypted summoner id LEAGUE-V4's by-summoner path still requires. */
    val id: String = "",
    val profileIconId: Int = 0,
    val summonerLevel: Long = 0,
)

@Serializable
data class LeagueEntryDto(
    val queueType: String = "",
    val tier: String = "",
    val rank: String = "",
    val leaguePoints: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
)

@Serializable
data class MatchDto(
    val metadata: MatchMetadataDto = MatchMetadataDto(),
    val info: MatchInfoDto = MatchInfoDto(),
)

@Serializable
data class MatchMetadataDto(
    val matchId: String = "",
)

@Serializable
data class MatchInfoDto(
    val queueId: Int = 0,
    val gameDuration: Long = 0,
    val gameEndTimestamp: Long = 0,
    val participants: List<MatchParticipantDto> = emptyList(),
)

@Serializable
data class MatchParticipantDto(
    val puuid: String = "",
    val riotIdGameName: String = "",
    val riotIdTagline: String = "",
    val championId: Int = 0,
    val teamId: Int = 0,
    val win: Boolean = false,
    val kills: Int = 0,
    val deaths: Int = 0,
    val assists: Int = 0,
    val item0: Int = 0,
    val item1: Int = 0,
    val item2: Int = 0,
    val item3: Int = 0,
    val item4: Int = 0,
    val item5: Int = 0,
    val item6: Int = 0,
    val summoner1Id: Int = 0,
    val summoner2Id: Int = 0,
    val totalDamageDealtToChampions: Int = 0,
    val goldEarned: Int = 0,
    val totalMinionsKilled: Int = 0,
    val neutralMinionsKilled: Int = 0,
    val visionScore: Int = 0,
) {
    val items: List<Int> get() = listOf(item0, item1, item2, item3, item4, item5, item6)
    val csTotal: Int get() = totalMinionsKilled + neutralMinionsKilled
}

@Serializable
data class MatchTimelineDto(
    val info: MatchTimelineInfoDto = MatchTimelineInfoDto(),
)

@Serializable
data class MatchTimelineInfoDto(
    val frames: List<MatchTimelineFrameDto> = emptyList(),
)

@Serializable
data class MatchTimelineFrameDto(
    val timestamp: Long = 0,
    val participantFrames: Map<String, MatchTimelineParticipantFrameDto> = emptyMap(),
)

@Serializable
data class MatchTimelineParticipantFrameDto(
    val participantId: Int = 0,
    val totalGold: Int = 0,
    val xp: Int = 0,
)

@Serializable
data class SpectatorGameDto(
    val gameId: Long = 0,
    val gameStartTime: Long = 0,
    val gameQueueConfigId: Int = 0,
    val participants: List<SpectatorParticipantDto> = emptyList(),
)

@Serializable
data class SpectatorParticipantDto(
    val puuid: String = "",
    @SerialName("riotId") val riotId: String = "",
    val championId: Int = 0,
    val teamId: Int = 0,
    val spell1Id: Int = 0,
    val spell2Id: Int = 0,
) {
    /** SPECTATOR-V5 packs "Name#TAG" into one field rather than two. */
    val riotIdName: String get() = riotId.substringBeforeLast('#', riotId)
    val riotIdTagline: String get() = riotId.substringAfterLast('#', "")
}

@Serializable
data class ChampionMasteryDto(
    val championId: Int = 0,
    val championLevel: Int = 0,
    val championPoints: Int = 0,
    val lastPlayTime: Long = 0,
    val championPointsSinceLastLevel: Int = 0,
    val championPointsUntilNextLevel: Int = 0,
)

@Serializable
data class ChampionRotationDto(
    val freeChampionIds: List<Int> = emptyList(),
    val freeChampionIdsForNewPlayers: List<Int> = emptyList(),
    val maxNewPlayerLevel: Int = 0,
)

@Serializable
data class LeagueListDto(
    val entries: List<LeagueListEntryDto> = emptyList(),
)

@Serializable
data class LeagueListEntryDto(
    val puuid: String = "",
    val summonerName: String? = null,
    val leaguePoints: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
)

@Serializable
data class PlatformStatusDto(
    val name: String = "",
    val incidents: List<StatusIncidentDto> = emptyList(),
)

@Serializable
data class StatusIncidentDto(
    val id: Long = 0,
    val titles: List<StatusIncidentTitleDto> = emptyList(),
    @SerialName("incident_severity") val incidentSeverity: String? = null,
)

@Serializable
data class StatusIncidentTitleDto(
    val locale: String = "",
    val content: String = "",
)
