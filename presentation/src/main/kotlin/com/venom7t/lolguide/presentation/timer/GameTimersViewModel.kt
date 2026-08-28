package com.venom7t.lolguide.presentation.timer

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.timer.model.GameTimer
import com.venom7t.lolguide.domain.timer.model.GameTimerPreset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class GameTimersState(
    val running: ImmutableList<GameTimer> = persistentListOf(),
    val nowEpochMillis: Long = System.currentTimeMillis(),
)

sealed interface GameTimersEvent {
    data class PresetStarted(val preset: GameTimerPreset) : GameTimersEvent
    data class TimerCancelled(val timerId: Long) : GameTimersEvent
}

/**
 * Runs entirely offline: no champion, item or patch dependency (Phase 3 plan
 * §"In-game timers"). The tick loop lives in the ViewModel rather than the
 * Composable so a configuration change (rotation) does not restart it.
 */
@HiltViewModel
class GameTimersViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(GameTimersState())
    val state: StateFlow<GameTimersState> = _state.asStateFlow()

    private var nextId = 0L

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(TICK_MILLIS)
                val now = System.currentTimeMillis()
                _state.update { current ->
                    current.copy(
                        nowEpochMillis = now,
                        // Expired timers drop off the list on their own tick
                        // rather than needing an explicit dismiss action.
                        running = current.running.filterNot { it.isExpired(now) }.toImmutableList(),
                    )
                }
            }
        }
    }

    fun onEvent(event: GameTimersEvent) {
        when (event) {
            is GameTimersEvent.PresetStarted -> _state.update { current ->
                val timer = GameTimer(
                    id = nextId++,
                    preset = event.preset,
                    startedAtEpochMillis = System.currentTimeMillis(),
                )
                current.copy(running = (current.running + timer).toImmutableList())
            }

            is GameTimersEvent.TimerCancelled -> _state.update { current ->
                current.copy(
                    running = current.running.filterNot { it.id == event.timerId }.toImmutableList(),
                )
            }
        }
    }

    private companion object {
        const val TICK_MILLIS = 500L
    }
}
