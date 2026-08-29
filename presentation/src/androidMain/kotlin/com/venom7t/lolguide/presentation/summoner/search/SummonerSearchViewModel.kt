package com.venom7t.lolguide.presentation.summoner.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.onboarding.repository.OnboardingRepository
import com.venom7t.lolguide.domain.summoner.model.RecentSummonerSearch
import com.venom7t.lolguide.domain.summoner.repository.RecentSearchRepository
import com.venom7t.lolguide.domain.summoner.usecase.SearchSummonerUseCase
import com.venom7t.lolguide.presentation.common.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Resolves a "Name#TAG" search into a [com.venom7t.lolguide.domain.summoner.model.Summoner]
 * before navigating, so the profile screen never has to handle "not found"
 * itself -- it always opens on a summoner that is known to exist.
 */
class SummonerSearchViewModel (
    private val searchSummoner: SearchSummonerUseCase,
    private val recentSearchRepository: RecentSearchRepository,
    private val onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SummonerSearchState())
    val state: StateFlow<SummonerSearchState> = _state.asStateFlow()

    private val _effects = Channel<SummonerSearchEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var hasStarted = false

    fun onEvent(event: SummonerSearchEvent) {
        when (event) {
            SummonerSearchEvent.ScreenOpened -> start()

            is SummonerSearchEvent.QueryChanged ->
                _state.update { it.copy(query = event.query, error = null) }

            is SummonerSearchEvent.RegionSelected ->
                _state.update { it.copy(region = event.region) }

            SummonerSearchEvent.SearchClicked -> search(_state.value.query, _state.value.region)

            is SummonerSearchEvent.RecentSearchClicked ->
                search("${event.recent.riotIdName}#${event.recent.riotIdTagline}", event.recent.region)
        }
    }

    private fun start() {
        if (hasStarted) return
        hasStarted = true

        // Pre-fills the region the user picked during onboarding, so
        // "which region am I on" isn't a question this screen re-asks --
        // still changeable per search via RegionSelected.
        viewModelScope.launch {
            val onboardingRegion = onboardingRepository.observePreferences().first().region
            if (onboardingRegion != null) {
                _state.update { it.copy(region = onboardingRegion) }
            }
        }

        // Persisted (DataStore-backed) so closing and reopening the app
        // doesn't lose recent searches -- they were previously in-memory
        // only and vanished with the ViewModel.
        recentSearchRepository.observeRecentSearches()
            .onEach { recent ->
                _state.update {
                    it.copy(
                        recentSearches = recent.map { search ->
                            RecentSearch(search.riotIdName, search.riotIdTagline, search.region)
                        },
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun search(query: String, region: com.venom7t.lolguide.domain.onboarding.model.Region) {
        if (_state.value.isSearching) return
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true, error = null) }
            searchSummoner(query, region)
                .onSuccess { summoner ->
                    _state.update { it.copy(isSearching = false) }
                    recentSearchRepository.addRecentSearch(
                        RecentSummonerSearch(summoner.riotIdName, summoner.riotIdTagline, region),
                    )
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
