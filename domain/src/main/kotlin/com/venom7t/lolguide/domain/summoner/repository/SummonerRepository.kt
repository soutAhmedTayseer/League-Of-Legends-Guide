package com.venom7t.lolguide.domain.summoner.repository

import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.summoner.model.RankedEntry
import com.venom7t.lolguide.domain.summoner.model.Summoner

interface SummonerRepository {

    /**
     * Resolves a "Name#TAG" to a full [Summoner]. Two real Riot calls under
     * the hood (ACCOUNT-V1 then SUMMONER-V4), on different routing hosts
     * (Phase 4 plan §Region routing) -- exposed as one call because nothing
     * in this app ever wants the account without the profile.
     */
    suspend fun searchByRiotId(name: String, tagline: String, region: Region): Result<Summoner>

    suspend fun getRankedEntries(summoner: Summoner): Result<List<RankedEntry>>
}
