package com.venom7t.lolguide.presentation.champion.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionFilter
import com.venom7t.lolguide.domain.champion.usecase.FilterChampionsUseCase
import com.venom7t.lolguide.domain.champion.usecase.ObserveChampionsUseCase
import com.venom7t.lolguide.domain.champion.usecase.RefreshChampionsUseCase
import com.venom7t.lolguide.domain.champion.usecase.SearchChampionsUseCase
import com.venom7t.lolguide.domain.common.AppError
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.favourite.usecase.ObserveFavouriteIdsUseCase
import com.venom7t.lolguide.domain.favourite.usecase.ToggleFavouriteUseCase
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.presentation.common.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
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
    private val filterChampions: FilterChampionsUseCase,
    private val observeFavouriteIds: ObserveFavouriteIdsUseCase,
    private val toggleFavourite: ToggleFavouriteUseCase,
    private val resolvePatch: ResolvePatchUseCase,
    private val locale: AppLocale,
) : ViewModel() {

    private val _state = MutableStateFlow(ChampionListState())
    val state: StateFlow<ChampionListState> = _state.asStateFlow()

    private val _effects = Channel<ChampionListEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /** The unfiltered cache. Search and filters are applied over this. */
    private var allChampions: List<Champion> = emptyList()
    private var favouriteIds: Set<String> = emptySet()

    private var hasStarted = false

    fun onEvent(event: ChampionListEvent) {
        when (event) {
            ChampionListEvent.ScreenOpened -> start()
            ChampionListEvent.Retry -> load()
            is ChampionListEvent.QueryChanged -> updateQuery(event.query)
            ChampionListEvent.QueryCleared -> updateQuery("")

            is ChampionListEvent.ChampionClicked -> viewModelScope.launch {
                _effects.send(ChampionListEffect.NavigateToDetail(event.championId))
            }

            is ChampionListEvent.FavouriteToggled -> viewModelScope.launch {
                toggleFavourite(event.championId).onFailure { throwable ->
                    _effects.send(ChampionListEffect.ShowSnackbar(throwable.toUiText()))
                }
            }

            ChampionListEvent.FilterSheetOpened ->
                _state.update { it.copy(isFilterSheetOpen = true) }

            ChampionListEvent.FilterSheetDismissed ->
                _state.update { it.copy(isFilterSheetOpen = false) }

            is ChampionListEvent.RoleToggled -> updateFilter { current ->
                current.copy(roles = current.roles.toggle(event.role))
            }

            is ChampionListEvent.ResourceToggled -> updateFilter { current ->
                current.copy(resources = current.resources.toggle(event.resource))
            }

            is ChampionListEvent.DifficultyToggled -> updateFilter { current ->
                current.copy(difficulties = current.difficulties.toggle(event.difficulty))
            }

            is ChampionListEvent.DamageTypeToggled -> updateFilter { current ->
                current.copy(damageTypes = current.damageTypes.toggle(event.damageType))
            }

            ChampionListEvent.FavouritesOnlyToggled -> updateFilter { current ->
                current.copy(favouritesOnly = !current.favouritesOnly)
            }

            ChampionListEvent.FiltersCleared -> updateFilter { ChampionFilter() }
        }
    }

    private fun start() {
        if (hasStarted) return
        hasStarted = true
        observeCache()
        observeFavourites()
        load()
    }

    private fun observeCache() {
        observeChampions()
            .onEach { champions ->
                allChampions = champions
                _state.update { current ->
                    current.copy(
                        availableResources = champions
                            .map { it.partype }
                            .filter { it.isNotBlank() }
                            .distinct()
                            .sorted()
                            .toImmutableList(),
                        isLoading = current.isLoading && champions.isEmpty(),
                    )
                }
                recomputeVisible()
            }
            .launchIn(viewModelScope)
    }

    private fun observeFavourites() {
        observeFavouriteIds()
            .onEach { ids ->
                favouriteIds = ids
                _state.update { it.copy(favouriteIds = ids.toImmutableSet()) }
                // A favourite change alters the visible list only while the
                // favourites-only filter is on, but recomputing unconditionally
                // keeps one code path instead of two.
                recomputeVisible()
            }
            .launchIn(viewModelScope)
    }

    /**
     * Filter first, then search.
     *
     * Order matters: searching first would rank across champions the filter is
     * about to discard, so the top result could vanish as soon as it was
     * ranked. Filtering first means the ranking only ever orders things the
     * user can actually see.
     */
    private fun recomputeVisible() {
        _state.update { current ->
            val filtered = filterChampions(allChampions, current.filter, favouriteIds)
            val searched = searchChampions(filtered, current.query)
            current.copy(champions = searched.toImmutableList())
        }
    }

    private fun updateQuery(query: String) {
        _state.update { it.copy(query = query) }
        recomputeVisible()
    }

    private fun updateFilter(transform: (ChampionFilter) -> ChampionFilter) {
        _state.update { it.copy(filter = transform(it.filter)) }
        recomputeVisible()
    }

    private fun load() {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = allChampions.isEmpty(), isRefreshing = true, error = null)
            }

            val patch = resolvePatch().getOrElse { throwable ->
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
                            error = if (allChampions.isEmpty()) throwable.toUiText() else null,
                        )
                    }
                    if (allChampions.isNotEmpty()) {
                        _effects.send(ChampionListEffect.ShowSnackbar(throwable.toUiText()))
                    }
                }
        }
    }
}

/** Adds [value] if absent, removes it if present. */
private fun <T> Set<T>.toggle(value: T): Set<T> =
    if (value in this) this - value else this + value
