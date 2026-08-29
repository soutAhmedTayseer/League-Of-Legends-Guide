package com.venom7t.lolguide.presentation.livegame

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.venom7t.lolguide.domain.livegame.usecase.GetLiveGameUseCase
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.presentation.common.toUiText
import com.venom7t.lolguide.presentation.navigation.LiveGameRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LiveGameViewModel (
    savedStateHandle: SavedStateHandle,
    private val getLiveGame: GetLiveGameUseCase,
    private val resolvePatch: ResolvePatchUseCase,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<LiveGameRoute>()
    private val region = Region.valueOf(route.region)

    private val _state = MutableStateFlow(LiveGameState())
    val state: StateFlow<LiveGameState> = _state.asStateFlow()

    private val _effects = Channel<LiveGameEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var hasStarted = false

    fun onEvent(event: LiveGameEvent) {
        when (event) {
            LiveGameEvent.ScreenOpened -> start()
            LiveGameEvent.Retry -> load()
            LiveGameEvent.BackClicked -> viewModelScope.launch {
                _effects.send(LiveGameEffect.NavigateBack)
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
            _state.update { it.copy(isLoading = true, error = null, notInGame = false) }
            val patch = resolvePatch().getOrNull()?.version
            getLiveGame(route.puuid, region)
                .onSuccess { game ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            patchVersion = patch,
                            game = game,
                            notInGame = game == null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.toUiText()) }
                }
        }
    }
}
