package com.venom7t.lolguide.presentation.champion.list

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionFilter
import com.venom7t.lolguide.domain.champion.model.ChampionTag
import com.venom7t.lolguide.domain.champion.model.DamageType
import com.venom7t.lolguide.domain.champion.model.Difficulty
import com.venom7t.lolguide.presentation.common.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Immutable
data class ChampionListState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val champions: ImmutableList<Champion> = persistentListOf(),
    val favouriteIds: ImmutableSet<String> = persistentSetOf(),
    val query: String = "",
    val filter: ChampionFilter = ChampionFilter(),
    val isFilterSheetOpen: Boolean = false,
    /**
     * The resource types present in the cached data, so the filter sheet
     * offers only options that can actually match something. Hardcoding
     * "Mana / Energy / None" would go stale the next time Riot ships a
     * champion with a bespoke resource.
     */
    val availableResources: ImmutableList<String> = persistentListOf(),
    val patchVersion: String? = null,
    val isPatchStale: Boolean = false,
    val error: UiText? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && champions.isEmpty() && query.isBlank() && !filter.isActive

    /** A search or filter excluded everything. Not the same as having no data. */
    val hasNoResults: Boolean
        get() = !isLoading && champions.isEmpty() && (query.isNotBlank() || filter.isActive)
}

sealed interface ChampionListEvent {
    data object ScreenOpened : ChampionListEvent
    data object Retry : ChampionListEvent
    data class QueryChanged(val query: String) : ChampionListEvent
    data object QueryCleared : ChampionListEvent
    data class ChampionClicked(val championId: String) : ChampionListEvent
    data class FavouriteToggled(val championId: String) : ChampionListEvent

    data object FilterSheetOpened : ChampionListEvent
    data object FilterSheetDismissed : ChampionListEvent
    data class RoleToggled(val role: ChampionTag) : ChampionListEvent
    data class ResourceToggled(val resource: String) : ChampionListEvent
    data class DifficultyToggled(val difficulty: Difficulty) : ChampionListEvent
    data class DamageTypeToggled(val damageType: DamageType) : ChampionListEvent
    data object FavouritesOnlyToggled : ChampionListEvent
    data object FiltersCleared : ChampionListEvent
}

sealed interface ChampionListEffect {
    data class NavigateToDetail(val championId: String) : ChampionListEffect
    data class ShowSnackbar(val message: UiText) : ChampionListEffect
}
