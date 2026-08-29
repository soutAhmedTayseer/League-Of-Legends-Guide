package com.venom7t.lolguide.presentation.game.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.usecase.ObserveGameStatsUseCase
import com.venom7t.lolguide.domain.game.usecase.PickDailyChampionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameHubViewModel (
    private val observeGameStats: ObserveGameStatsUseCase,
    private val pickDailyChampion: PickDailyChampionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(GameHubState())
    val state: StateFlow<GameHubState> = _state.asStateFlow()

    private val _effects = Channel<GameHubEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var hasStarted = false

    fun onEvent(event: GameHubEvent) {
        when (event) {
            GameHubEvent.ScreenOpened -> start()
            is GameHubEvent.ModeClicked -> viewModelScope.launch {
                _effects.send(GameHubEffect.NavigateToRound(event.mode))
            }
            GameHubEvent.BackClicked -> viewModelScope.launch {
                _effects.send(GameHubEffect.NavigateBack)
            }
        }
    }

    private fun start() {
        if (hasStarted) return
        hasStarted = true

        combine(GameMode.entries.map { mode -> observeGameStats(mode) }) { statsArray ->
            GameMode.entries.zip(statsArray).toMap()
        }
            .onEach { statsByMode -> _state.update { it.copy(stats = statsByMode) } }
            .launchIn(viewModelScope)

        // All three modes share one UTC-midnight reset, so one ticking
        // countdown on the hub covers them all rather than three identical
        // timers on each mode's own screen.
        viewModelScope.launch {
            while (isActive) {
                _state.update { it.copy(millisUntilReset = pickDailyChampion.millisUntilNextReset()) }
                delay(1_000L)
            }
        }
    }
}
