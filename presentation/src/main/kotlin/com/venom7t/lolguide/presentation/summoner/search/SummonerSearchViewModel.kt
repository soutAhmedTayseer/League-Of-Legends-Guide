package com.venom7t.lolguide.presentation.summoner.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.summoner.usecase.SearchSummonerUseCase
import com.venom7t.lolguide.presentation.common.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Resolves a "Name#TAG" search into a [com.venom7t.lolguide.domain.summoner.model.Summoner]
 * before navigating, so the profile screen never has to handle "not found"
 * itself -- it always opens on a summoner that is known to exist.
 */
@HiltViewModel
class SummonerSearchViewModel @Inject constructor(
    private val searchSummoner: SearchSummonerUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SummonerSearchState())
    val state: StateFlow<SummonerSearchState> = _state.asStateFlow()

    private val _effects = Channel<SummonerSearchEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: SummonerSearchEvent) {
        when (event) {
            is SummonerSearchEvent.QueryChanged ->
                _state.update { it.copy(query = event.query, error = null) }

            is SummonerSearchEvent.RegionSelected ->
                _state.update { it.copy(region = event.region) }

            SummonerSearchEvent.SearchClicked -> search(_state.value.query, _state.value.region)

            is SummonerSearchEvent.RecentSearchClicked ->
                search("${event.recent.riotIdName}#${event.recent.riotIdTagline}", event.recent.region)
        }
    }

    private fun search(query: String, region: com.venom7t.lolguide.domain.onboarding.model.Region) {
        if (_state.value.isSearching) return
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, error = null) }
            searchSummoner(query, region)
                .onSuccess { summoner ->
                    _state.update { current ->
                        current.copy(
                            isSearching = false,
                            recentSearches = (
                                listOf(
                                    RecentSearch(summoner.riotIdName, summoner.riotIdTagline, region),
                                ) + current.recentSearches.filterNot {
                                    it.riotIdName == summoner.riotIdName &&
                                        it.riotIdTagline == summoner.riotIdTagline &&
                                        it.region == region
                                }
                            ).take(5),
                        )
                    }
                    _effects.send(
                        SummonerSearchEffect.NavigateToProfile(
                            riotIdName = summoner.riotIdName,
                            riotIdTagline = summoner.riotIdTagline,
                            region = region,
                        ),
                    )
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isSearching = false, error = throwable.toUiText()) }
                }
        }
    }
}
