package com.venom7t.lolguide.presentation.roulette

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.usecase.ObserveChampionsUseCase
import com.venom7t.lolguide.domain.champion.usecase.RandomChampionUseCase
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

@Immutable
data class RouletteState(
    val isLoading: Boolean = true,
    val result: Champion? = null,
    val poolSize: Int = 0,
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

@HiltViewModel
class RouletteViewModel @Inject constructor(
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
                _state.update { it.copy(result = next) }
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
}
