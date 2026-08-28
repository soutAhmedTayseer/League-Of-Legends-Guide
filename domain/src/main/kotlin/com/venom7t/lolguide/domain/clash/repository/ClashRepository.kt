package com.venom7t.lolguide.domain.clash.repository

import com.venom7t.lolguide.domain.clash.model.ClashTeam
import com.venom7t.lolguide.domain.onboarding.model.Region

interface ClashRepository {

    /**
     * The summoner's currently-registered Clash team, if any. Null (inside
     * success) means "not registered for a Clash team right now," a normal
     * outcome, distinct from a real fetch failure -- same shape as
     * [com.venom7t.lolguide.domain.livegame.repository.LiveGameRepository].
     */
    suspend fun getTeamForSummoner(summonerId: String, region: Region): Result<ClashTeam?>
}
