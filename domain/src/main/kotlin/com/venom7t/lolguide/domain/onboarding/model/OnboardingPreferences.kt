package com.venom7t.lolguide.domain.onboarding.model

/**
 * Riot's platform routing values, restricted to the ones relevant to region
 * selection during onboarding. Kept here rather than a raw String so an
 * invalid region can never be stored.
 */
enum class Region(val platformId: String) {
    NA("na1"),
    EUW("euw1"),
    EUNE("eun1"),
    KR("kr"),
    JP("jp1"),
    OCE("oc1"),
    BR("br1"),
    LAN("la1"),
    LAS("la2"),
    TR("tr1"),
    RU("ru"),
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
