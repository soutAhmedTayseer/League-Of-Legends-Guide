package com.example.lolguide.presentation.champion.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.lolguide.domain.champion.usecase.GetChampionDetailUseCase
import com.example.lolguide.domain.common.AppLocale
import com.example.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.example.lolguide.presentation.common.toUiText
import com.example.lolguide.presentation.navigation.ChampionDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChampionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChampionDetail: GetChampionDetailUseCase,
    private val resolvePatch: ResolvePatchUseCase,
    private val locale: AppLocale,
) : ViewModel() {

    /**
     * Read from the type-safe route rather than a stringly-typed argument key,
     * so a rename of the route parameter is a compile error rather than a null
     * at runtime.
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
        }
    }

    private fun start() {
        if (hasStarted) return
        hasStarted = true
        load()
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
                    _state.update {
                        it.copy(
                            isLoading = false,
                            champion = result.champion,
                            detail = result.detail,
                            error = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.toUiText()) }
                }
        }
    }
}
