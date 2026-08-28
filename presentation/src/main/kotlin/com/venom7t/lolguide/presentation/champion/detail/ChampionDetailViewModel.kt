package com.venom7t.lolguide.presentation.champion.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.venom7t.lolguide.domain.champion.model.ChampionStatCalculator
import com.venom7t.lolguide.domain.champion.usecase.GetChampionDetailUseCase
import com.venom7t.lolguide.domain.champion.usecase.GetChampionStatsAtLevelUseCase
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.favourite.usecase.ObserveFavouriteIdsUseCase
import com.venom7t.lolguide.domain.favourite.usecase.ToggleFavouriteUseCase
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.presentation.common.toUiText
import com.venom7t.lolguide.presentation.navigation.ChampionDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
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
class ChampionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChampionDetail: GetChampionDetailUseCase,
    private val getStatsAtLevel: GetChampionStatsAtLevelUseCase,
    private val observeFavouriteIds: ObserveFavouriteIdsUseCase,
    private val toggleFavourite: ToggleFavouriteUseCase,
    private val resolvePatch: ResolvePatchUseCase,
    private val locale: AppLocale,
) : ViewModel() {

    /**
     * Read from the type-safe route rather than a stringly-typed argument key,
     * so renaming the route parameter is a compile error, not a runtime null.
     */
    private val championId: String = savedStateHandle.toRoute<ChampionDetailRoute>().championId

    private val _state = MutableStateFlow(ChampionDetailState())
    val state: StateFlow<ChampionDetailState> = _state.asStateFlow()

    private val _effects = Channel<ChampionDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var hasStarted = false

    fun onEvent(event: ChampionDetailEvent) {
        when (event) {
            ChampionDetailEvent.ScreenOpened -> start()
            ChampionDetailEvent.Retry -> load()

            ChampionDetailEvent.BackClicked -> viewModelScope.launch {
                _effects.send(ChampionDetailEffect.NavigateBack)
            }

            is ChampionDetailEvent.LevelChanged -> setLevel(event.level)

            is ChampionDetailEvent.SkinSelected -> _state.update {
                it.copy(selectedSkinIndex = event.index)
            }

            // Adding is harmless and immediate. Removing destroys something the
            // user made, so it is confirmed first (AGENTS.md §13).
            ChampionDetailEvent.FavouriteClicked -> {
                if (_state.value.isFavourite) {
                    _state.update { it.copy(pendingFavouriteRemoval = true) }
                } else {
                    commitFavouriteToggle()
                }
            }

            ChampionDetailEvent.FavouriteRemovalConfirmed -> {
                _state.update { it.copy(pendingFavouriteRemoval = false) }
                commitFavouriteToggle()
            }

            ChampionDetailEvent.FavouriteRemovalCancelled ->
                _state.update { it.copy(pendingFavouriteRemoval = false) }
        }
    }

    private fun start() {
        if (hasStarted) return
        hasStarted = true
        observeFavourite()
        load()
    }

    private fun observeFavourite() {
        observeFavouriteIds()
            .onEach { ids -> _state.update { it.copy(isFavourite = championId in ids) } }
            .launchIn(viewModelScope)
    }

    private fun commitFavouriteToggle() {
        viewModelScope.launch {
            toggleFavourite(championId).onFailure { throwable ->
                _effects.send(ChampionDetailEffect.ShowSnackbar(throwable.toUiText()))
            }
        }
    }

    private fun setLevel(level: Int) {
        val clamped = level.coerceIn(
            ChampionStatCalculator.MIN_LEVEL,
            ChampionStatCalculator.MAX_LEVEL,
        )
        _state.update { current ->
            current.copy(
                level = clamped,
                scaledStats = current.champion?.let { getStatsAtLevel(it.stats, clamped) },
            )
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val patch = resolvePatch().getOrElse { throwable ->
                _state.update { it.copy(isLoading = false, error = throwable.toUiText()) }
                return@launch
            }

            getChampionDetail(championId = championId, version = patch.version, locale = locale)
                .onSuccess { result ->
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            champion = result.champion,
                            detail = result.detail,
                            error = null,
                            selectedSkinIndex = 0,
                            scaledStats = getStatsAtLevel(result.champion.stats, current.level),
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.toUiText()) }
                }
        }
    }
}
