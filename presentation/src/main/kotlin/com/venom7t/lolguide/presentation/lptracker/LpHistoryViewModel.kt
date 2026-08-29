package com.venom7t.lolguide.presentation.lptracker

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.venom7t.lolguide.domain.lptracker.usecase.ObserveLpHistoryUseCase
import com.venom7t.lolguide.presentation.navigation.LpHistoryRoute
import org.koin.android.annotation.KoinViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@KoinViewModel
class LpHistoryViewModel (
    savedStateHandle: SavedStateHandle,
    private val observeLpHistory: ObserveLpHistoryUseCase,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<LpHistoryRoute>()

    private val _state = MutableStateFlow(LpHistoryState())
    val state: StateFlow<LpHistoryState> = _state.asStateFlow()

    private val _effects = Channel<LpHistoryEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var hasStarted = false

    fun onEvent(event: LpHistoryEvent) {
        when (event) {
            LpHistoryEvent.ScreenOpened -> start()
            is LpHistoryEvent.QueueSelected -> _state.update { it.copy(queueType = event.queueType) }
            LpHistoryEvent.BackClicked -> viewModelScope.launch {
                _effects.send(LpHistoryEffect.NavigateBack)
            }
        }
    }

    private fun start() {
        if (hasStarted) return
        hasStarted = true
        observeQueue()
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private fun observeQueue() {
        _state.flatMapLatest { current -> observeLpHistory(route.puuid, current.queueType) }
            .onEach { snapshots -> _state.update { it.copy(snapshots = snapshots) } }
            .launchIn(viewModelScope)
    }
}
