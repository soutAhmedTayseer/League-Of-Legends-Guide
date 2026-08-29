package com.venom7t.lolguide.domain.lptracker.model

import com.venom7t.lolguide.domain.summoner.model.RankedQueue

/**
 * One point-in-time reading of a tracked summoner's ranked standing.
 *
 * This is a **locally recorded observation**, not something Riot's API
 * exposes as history (AGENTS.md §1) -- LEAGUE-V4 only ever answers "what is
 * this summoner's rank right now." The LP tracker worker calls that
 * repeatedly and stores each answer as one of these, so any "gained/lost N
 * LP" figure the UI shows is explicitly derived from *this app's own
 * polling history*, and is empty until tracking has run at least twice.
 */
data class LpSnapshot(
    val puuid: String,
    val queueType: RankedQueue,
    val tier: String,
    val rank: String,
    val leaguePoints: Int,
    val capturedAtEpochMillis: Long,
)
