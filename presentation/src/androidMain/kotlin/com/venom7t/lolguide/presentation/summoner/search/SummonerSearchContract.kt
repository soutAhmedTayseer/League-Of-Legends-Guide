package com.venom7t.lolguide.presentation.summoner.search

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.presentation.common.UiText

@Immutable
data class SummonerSearchState(
    val query: String = "",
    val region: Region = Region.EUNE,
    val isSearching: Boolean = false,
    val error: UiText? = null,
    val recentSearches: List<RecentSearch> = emptyList(),
) {
    val canSearch: Boolean get() = query.contains('#') && !isSearching
}

@Immutable
data class RecentSearch(
    val riotIdName: String,
    val riotIdTagline: String,
    val region: Region,
)

sealed interface SummonerSearchEvent {
    data object ScreenOpened : SummonerSearchEvent
    data class QueryChanged(val query: String) : SummonerSearchEvent
    data class RegionSelected(val region: Region) : SummonerSearchEvent
    data object SearchClicked : SummonerSearchEvent
    data class RecentSearchClicked(val recent: RecentSearch) : SummonerSearchEvent
}

sealed interface SummonerSearchEffect {
    data class NavigateToProfile(
        val riotIdName: String,
        val riotIdTagline: String,
        val region: Region,
    ) : SummonerSearchEffect
    data class ShowSnackbar(val message: UiText) : SummonerSearchEffect
}
