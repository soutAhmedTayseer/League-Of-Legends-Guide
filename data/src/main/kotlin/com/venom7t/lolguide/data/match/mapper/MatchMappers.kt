package com.venom7t.lolguide.data.match.mapper

import com.venom7t.lolguide.data.riot.remote.dto.MatchDto
import com.venom7t.lolguide.data.riot.remote.dto.MatchParticipantDto
import com.venom7t.lolguide.data.riot.remote.dto.MatchTimelineDto
import com.venom7t.lolguide.domain.match.model.MatchDetail
import com.venom7t.lolguide.domain.match.model.MatchParticipant
import com.venom7t.lolguide.domain.match.model.MatchSummary
import com.venom7t.lolguide.domain.match.model.MatchTimelineFrame

fun MatchDto.toDetail(): MatchDetail = MatchDetail(
    matchId = metadata.matchId,
    queueId = info.queueId,
    gameDurationSeconds = info.gameDuration.toInt(),
    gameEndEpochMillis = info.gameEndTimestamp,
    participants = info.participants.map { it.toParticipant() },
)

private fun MatchParticipantDto.toParticipant() = MatchParticipant(
    puuid = puuid,
    riotIdName = riotIdGameName,
    riotIdTagline = riotIdTagline,
    // Riot's champion id here is the numeric key (Champion.key), not the
    // Data Dragon string id -- resolving that to art/detail is the
    // presentation layer's job, same as the free-rotation numeric ids.
    championId = championId.toString(),
    teamId = teamId,
    win = win,
    kills = kills,
    deaths = deaths,
    assists = assists,
    items = items,
    summonerSpell1Id = summoner1Id,
    summonerSpell2Id = summoner2Id,
    totalDamageDealtToChampions = totalDamageDealtToChampions,
    goldEarned = goldEarned,
    csTotal = csTotal,
    visionScore = visionScore,
)

/**
 * @param viewingPuuid whose row of this match becomes the summary -- a
 *   history list is always "my games", so the summary is scoped to one
 *   participant, not all ten.
 */
fun MatchDto.toSummary(viewingPuuid: String, isFromCache: Boolean): MatchSummary? {
    val mine = info.participants.firstOrNull { it.puuid == viewingPuuid } ?: return null
    return MatchSummary(
        matchId = metadata.matchId,
        championId = mine.championId.toString(),
        kills = mine.kills,
        deaths = mine.deaths,
        assists = mine.assists,
        win = mine.win,
        queueId = info.queueId,
        gameDurationSeconds = info.gameDuration.toInt(),
        gameEndEpochMillis = info.gameEndTimestamp,
        items = mine.items,
        csTotal = mine.csTotal,
        isFromCache = isFromCache,
    )
}

fun MatchTimelineDto.toDomain(): List<MatchTimelineFrame> =
    info.frames.map { frame ->
        MatchTimelineFrame(
            timestampSeconds = (frame.timestamp / 1000).toInt(),
            totalGoldByParticipantId = frame.participantFrames
                .mapKeys { it.key.toIntOrNull() ?: it.value.participantId }
                .mapValues { it.value.totalGold },
            xpByParticipantId = frame.participantFrames
                .mapKeys { it.key.toIntOrNull() ?: it.value.participantId }
                .mapValues { it.value.xp },
        )
    }
