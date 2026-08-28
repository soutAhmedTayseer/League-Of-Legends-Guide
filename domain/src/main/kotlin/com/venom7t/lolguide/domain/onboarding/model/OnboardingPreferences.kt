package com.venom7t.lolguide.domain.onboarding.model

/**
 * Riot's platform routing values, restricted to the ones relevant to region
 * selection during onboarding. Kept here rather than a raw String so an
 * invalid region can never be stored.
 *
 * @param platformId used by SUMMONER-V4, MASTERY-V4, LEAGUE-V4,
 *   SPECTATOR-V5 and CHAMPION-V3 (Phase 4 plan).
 * @param regionalRoute used by ACCOUNT-V1 and MATCH-V5. **Not derivable from
 *   [platformId]** -- Riot groups several platforms under one regional host,
 *   so this is its own field rather than a computed one.
 */
enum class Region(val platformId: String, val regionalRoute: String) {
    NA("na1", "americas"),
    BR("br1", "americas"),
    LAN("la1", "americas"),
    LAS("la2", "americas"),
    OCE("oc1", "americas"),
    EUW("euw1", "europe"),
    EUNE("eun1", "europe"),
    TR("tr1", "europe"),
    RU("ru", "europe"),
    KR("kr", "asia"),
    JP("jp1", "asia"),
}

enum class PrimaryRole {
    TOP, JUNGLE, MID, BOTTOM, SUPPORT,
}

/**
 * One-time onboarding choices, used to personalise the home dashboard (e.g.
 * defaulting champion filters to the picked role). Neither field gates any
 * feature -- both are conveniences, not requirements -- so a user can skip
 * onboarding entirely and everything still works with neutral defaults.
 */
data class OnboardingPreferences(
    val region: Region?,
    val primaryRole: PrimaryRole?,
    val hasCompletedOnboarding: Boolean,
)
