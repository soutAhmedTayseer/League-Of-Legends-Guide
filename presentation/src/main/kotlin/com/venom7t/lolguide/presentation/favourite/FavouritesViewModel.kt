package com.venom7t.lolguide.presentation.favourite

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.usecase.ObserveChampionsUseCase
import com.venom7t.lolguide.domain.champion.usecase.RefreshChampionsUseCase
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.favourite.usecase.ObserveFavouriteIdsUseCase
import com.venom7t.lolguide.domain.favourite.usecase.ToggleFavouriteUseCase
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import org.koin.android.annotation.KoinViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class FavouritesState(
    val isLoading: Boolean = true,
    val champions: ImmutableList<Champion> = persistentListOf(),
)

sealed interface FavouritesEvent {
    data object ScreenOpened : FavouritesEvent
    data class ChampionClicked(val championId: String) : FavouritesEvent
    data class FavouriteToggled(val championId: String) : FavouritesEvent
}

sealed interface FavouritesEffect {
    data class NavigateToDetail(val championId: String) : FavouritesEffect
}

@KoinViewModel
class FavouritesViewModel (
    private val observeChampions: ObserveChampionsUseCase,
    private val observeFavouriteIds: ObserveFavouriteIdsUseCase,
    private val toggleFavourite: ToggleFavouriteUseCase,
    private val refreshChampions: RefreshChampionsUseCase,
    private val resolvePatch: ResolvePatchUseCase,
    private val locale: AppLocale,
) : ViewModel() {

    private val _state = MutableStateFlow(FavouritesState())
    val state: StateFlow<FavouritesState> = _state.asStateFlow()

    private val _effects = Channel<FavouritesEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var hasStarted = false

    fun onEvent(event: FavouritesEvent) {
        when (event) {
            FavouritesEvent.ScreenOpened -> start()

            is FavouritesEvent.ChampionClicked -> viewModelScope.launch {
                _effects.send(FavouritesEffect.NavigateToDetail(event.championId))
            }

            is FavouritesEvent.FavouriteToggled -> viewModelScope.launch {
                toggleFavourite(event.championId)
            }
        }
    }

    private fun start() {
        if (hasStarted) return
        hasStarted = true

        // Favourites store only ids, so the champion data is joined in here.
        // Denormalising a copy of each champion into the favourites table would
        // go stale the moment the champion cache is replaced on a new patch.
        combine(observeChampions(), observeFavouriteIds()) { champions, favouriteIds ->
            champions.filter { it.id in favouriteIds }
        }
            .onEach { favourites ->
                _state.update {
                    it.copy(isLoading = false, champions = favourites.toImmutableList())
                }
            }
            .launchIn(viewModelScope)

        ensureChampionsCached()
    }

    /**
     * Champion data only ever reaches Room through [RefreshChampionsUseCase],
     * which normally runs from [com.venom7t.lolguide.presentation.champion.list.ChampionListViewModel].
     * Landing here first -- e.g. straight from Home on a fresh install, or
     * after the local cache was cleared -- would otherwise show an empty
     * favourites list forever, even with favourite ids already synced from
     * Firestore, until the user happened to open the Champions tab too.
     */
    private fun ensureChampionsCached() {
        viewModelScope.launch {
            if (observeChampions().first().isNotEmpty()) return@launch
            resolvePatch().onSuccess { patch -> refreshChampions(patch.version, locale) }
        }
    }
}
