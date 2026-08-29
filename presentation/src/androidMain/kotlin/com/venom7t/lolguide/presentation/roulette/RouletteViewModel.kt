package com.venom7t.lolguide.presentation.roulette

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.usecase.ObserveChampionsUseCase
import com.venom7t.lolguide.domain.champion.usecase.RandomChampionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class RouletteState(
    val isLoading: Boolean = true,
    val result: Champion? = null,
    val poolSize: Int = 0,
    /**
     * The frames a roll spins through before landing on [result] (which is
     * always the last entry) -- a slot-machine cycle reads as "rolling",
     * where jumping straight to the answer reads as nothing happening.
     * Empty before the first roll.
     */
    val spinSequence: List<Champion> = emptyList(),
)

sealed interface RouletteEvent {
    data object ScreenOpened : RouletteEvent
    data object Rolled : RouletteEvent
    data object ViewChampionClicked : RouletteEvent
    data object BackClicked : RouletteEvent
}

sealed interface RouletteEffect {
    data class NavigateToDetail(val championId: String) : RouletteEffect
    data object NavigateBack : RouletteEffect
}

class RouletteViewModel (
    private val observeChampions: ObserveChampionsUseCase,
    private val randomChampion: RandomChampionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RouletteState())
    val state: StateFlow<RouletteState> = _state.asStateFlow()

    private val _effects = Channel<RouletteEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var pool: List<Champion> = emptyList()
    private var hasStarted = false

    fun onEvent(event: RouletteEvent) {
        when (event) {
            RouletteEvent.ScreenOpened -> start()

            RouletteEvent.Rolled -> {
                // Excluding the previous result means a reroll always visibly
                // changes something, which is the whole point of pressing it.
                val next = randomChampion(pool = pool, excludeId = _state.value.result?.id)
                _state.update {
                    it.copy(result = next, spinSequence = buildSpinSequence(next))
                }
            }

            RouletteEvent.ViewChampionClicked -> {
                val current = _state.value.result ?: return
                viewModelScope.launch {
                    _effects.send(RouletteEffect.NavigateToDetail(current.id))
                }
            }

            RouletteEvent.BackClicked -> viewModelScope.launch {
                _effects.send(RouletteEffect.NavigateBack)
            }
        }
    }

    /**
     * A short run of random champions from the pool, ending in [landing] --
     * the visual "spin" before the wheel stops. Frames can repeat; nothing
     * about which one is shown mid-spin is meaningful, only the landing spot.
     */
    private fun buildSpinSequence(landing: Champion?): List<Champion> {
        if (landing == null || pool.isEmpty()) return emptyList()
        val frames = (1 until SPIN_FRAME_COUNT).map { pool.random() }
        return frames + landing
    }

    private fun start() {
        if (hasStarted) return
        hasStarted = true

        observeChampions()
            .onEach { champions ->
                pool = champions
                _state.update { it.copy(isLoading = false, poolSize = champions.size) }
            }
            .launchIn(viewModelScope)
    }

    private companion object {
        const val SPIN_FRAME_COUNT = 14
    }
}
