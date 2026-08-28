package com.example.lolguide.presentation.champion.list

import androidx.compose.runtime.Immutable
import com.example.lolguide.domain.champion.model.Champion
import com.example.lolguide.presentation.common.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * MVI contract for the champion list (AGENTS.md §4).
 */

@Immutable
data class ChampionListState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val champions: ImmutableList<Champion> = persistentListOf(),
    val query: String = "",
    val patchVersion: String? = null,
    val isPatchStale: Boolean = false,
    /**
     * Kept separate from [champions] so a refresh failure can be shown as a
     * banner over cached content instead of replacing it. Offline with cached
     * champions is a usable screen; blanking it would be a regression.
     */
    val error: UiText? = null,
) {
    /** True only when there is genuinely nothing to render. */
    val isEmpty: Boolean get() = !isLoading && champions.isEmpty() && query.isBlank()

    /** True when a search excluded everything, which is not the same as empty. */
    val hasNoSearchResults: Boolean
        get() = !isLoading && champions.isEmpty() && query.isNotBlank()
}

sealed interface ChampionListEvent {
    data object ScreenOpened : ChampionListEvent
    data object Retry : ChampionListEvent
    data class QueryChanged(val query: String) : ChampionListEvent
    data object QueryCleared : ChampionListEvent
    data class ChampionClicked(val championId: String) : ChampionListEvent
}

sealed interface ChampionListEffect {
    data class NavigateToDetail(val championId: String) : ChampionListEffect
    data class ShowSnackbar(val message: UiText) : ChampionListEffect
}
