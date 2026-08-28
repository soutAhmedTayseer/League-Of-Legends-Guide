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
