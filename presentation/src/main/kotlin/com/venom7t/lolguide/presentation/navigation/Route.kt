package com.venom7t.lolguide.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes (AGENTS.md §6). String routes are banned: they
 * turn a typo into a runtime crash and give the compiler nothing to check.
 *
 * The route *definitions* live in `:presentation` rather than `:app` so a
 * ViewModel can read its own arguments with `SavedStateHandle.toRoute()`. The
 * `NavHost` that wires them together still lives in `:app`, which is the only
 * module allowed to know about every screen at once.
 */

@Serializable
data object ChampionListRoute

@Serializable
data class ChampionDetailRoute(val championId: String)

@Serializable
data object FavouritesRoute

@Serializable
data object RouletteRoute

@Serializable
data object CompareRoute

// --- Phase 2: items, builds and references ---

@Serializable
data object ItemListRoute

@Serializable
data class ItemDetailRoute(val itemId: String)

@Serializable
data object BuildSimulatorRoute

@Serializable
data object RunesRoute

@Serializable
data object SummonerSpellsRoute

// --- Phase 3: onboarding, home, and offline extras ---

@Serializable
data object OnboardingRoute

@Serializable
data object HomeRoute

@Serializable
data object WhatsNewRoute

@Serializable
data object QuizRoute

@Serializable
data object GameTimersRoute

// --- Phase 4: player data ---

@Serializable
data object SummonerSearchRoute

@Serializable
data class SummonerProfileRoute(
    val riotIdName: String,
    val riotIdTagline: String,
    val region: String,
)

@Serializable
data class MatchDetailRoute(
    val matchId: String,
    val region: String,
    val viewingPuuid: String,
)

@Serializable
data class LiveGameRoute(val puuid: String, val region: String)

@Serializable
data class MasteryRoute(val puuid: String, val region: String)

@Serializable
data object LadderRoute

@Serializable
data object FollowedSummonersRoute
