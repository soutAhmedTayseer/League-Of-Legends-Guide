package com.venom7t.lolguide.domain.summoner.model

import com.venom7t.lolguide.domain.onboarding.model.Region

/** One past summoner search, persisted so it survives closing the app. */
data class RecentSummonerSearch(
    val riotIdName: String,
    val riotIdTagline: String,
    val region: Region,
)
