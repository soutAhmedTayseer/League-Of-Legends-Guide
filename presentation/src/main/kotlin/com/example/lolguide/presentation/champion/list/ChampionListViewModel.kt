package com.example.lolguide.presentation.champion.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lolguide.domain.champion.model.Champion
import com.example.lolguide.domain.champion.usecase.ObserveChampionsUseCase
import com.example.lolguide.domain.champion.usecase.RefreshChampionsUseCase
import com.example.lolguide.domain.champion.usecase.SearchChampionsUseCase
import com.example.lolguide.domain.common.AppError
import com.example.lolguide.domain.common.AppLocale
import com.example.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.example.lolguide.presentation.common.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChampionListViewModel @Inject constructor(
    private val observeChampions: ObserveChampionsUseCase,
    private val refreshChampions: RefreshChampionsUseCase,
    private val searchChampions: SearchChampionsUseCase,
    private val resolvePatch: ResolvePatchUseCase,
    private val locale: AppLocale,
) : ViewModel() {

    private val _state = MutableStateFlow(ChampionListState())
    val state: StateFlow<ChampionListState> = _state.asStateFlow()

    private val _effects = Channel<ChampionListEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /**
     * The unfiltered cache. Search filters this rather than the displayed list,
     * so deleting characters from the query restores results without a reload.
     */
    private var allChampions: List<Champion> = emptyList()

    private var hasStarted = false

    fun onEvent(event: ChampionListEvent) {
        when (event) {
            ChampionListEvent.ScreenOpened -> start()
            ChampionListEvent.Retry -> load()
            is ChampionListEvent.QueryChanged -> applyQuery(event.query)
            ChampionListEvent.QueryCleared -> applyQuery("")
            is ChampionListEvent.ChampionClicked -> viewModelScope.launch {
                _effects.send(ChampionListEffect.NavigateToDetail(event.championId))
            }
        }
    }

    /** Idempotent: the screen re-emits ScreenOpened on every recomposition-driven restart. */
    private fun start() {
        if (hasStarted) return
        hasStarted = true
        observeCache()
        load()
    }

    private fun observeCache() {
        observeChampions()
            .onEach { champions ->
                allChampions = champions
                _state.update { current ->
                    current.copy(
                        champions = searchChampions(champions, current.query).toImmutableList(),
                        // Cached content arriving means we are no longer blank,
                        // even if a refresh is still in flight.
                        isLoading = current.isLoading && champions.isEmpty(),
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = allChampions.isEmpty(), isRefreshing = true, error = null) }

            val patch = resolvePatch().getOrElse { throwable ->
                // No patch means no valid request can be built at all. If we
                // have cached champions the screen stays usable, so this is a
                // banner rather than a full error state.
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = if (allChampions.isEmpty()) {
                            AppError.NoCachedData.toUiText()
                        } else {
                            throwable.toUiText()
                        },
                    )
                }
                return@launch
            }

            _state.update {
                it.copy(patchVersion = patch.version, isPatchStale = patch.isStale)
            }

            // Only hit the network when we actually need to: a patch change,
            // or an empty cache. Re-downloading the full champion list on every
            // screen open would be pointless traffic (AGENTS.md §8.3).
            val needsRefresh = patch.didPatchChange || allChampions.isEmpty()
            if (!needsRefresh) {
                _state.update { it.copy(isLoading = false, isRefreshing = false) }
                return@launch
            }

            refreshChampions(patch.version, locale)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isRefreshing = false, error = null) }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = if (allChampions.isEmpty()) {
                                throwable.toUiText()
                            } else {
                                null
                            },
                        )
                    }
                    // With cached content on screen the failure is incidental,
                    // so it is a transient snackbar rather than a blocking state.
                    if (allChampions.isNotEmpty()) {
                        _effects.send(ChampionListEffect.ShowSnackbar(throwable.toUiText()))
                    }
                }
        }
    }

    private fun applyQuery(query: String) {
        _state.update { current ->
            current.copy(
                query = query,
                champions = searchChampions(allChampions, query).toImmutableList(),
            )
        }
    }
}
