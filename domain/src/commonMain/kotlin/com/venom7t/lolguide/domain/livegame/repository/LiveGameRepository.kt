package com.venom7t.lolguide.domain.livegame.repository

import com.venom7t.lolguide.domain.livegame.model.LiveGame
import com.venom7t.lolguide.domain.onboarding.model.Region

interface LiveGameRepository {

    /**
     * Null (inside a successful [Result]) means "not currently in a game" --
     * a genuinely expected, common outcome, not a failure. A [Result.failure]
     * means the lookup itself broke (network, expired key, rate limit).
     */
    suspend fun getLiveGame(puuid: String, region: Region): Result<LiveGame?>
}
