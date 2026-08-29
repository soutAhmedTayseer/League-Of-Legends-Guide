package com.venom7t.lolguide.domain.followed.model

import com.venom7t.lolguide.domain.onboarding.model.Region

/**
 * A summoner the user has chosen to follow, for quick re-lookup.
 *
 * Local-only for this phase (owner decision, Phase 4 plan): stored on-device,
 * same pattern as Phase 1 favourited champions. No account or sync system
 * exists yet -- that is Phase 5 -- so this deliberately does not attempt to
 * be more than a personal on-device list.
 */
data class FollowedSummoner(
    val puuid: String,
    val riotIdName: String,
    val riotIdTagline: String,
    val region: Region,
    val followedAtEpochMillis: Long,
)
