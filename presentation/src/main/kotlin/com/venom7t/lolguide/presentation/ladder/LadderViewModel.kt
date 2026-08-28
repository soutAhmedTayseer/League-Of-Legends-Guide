package com.venom7t.lolguide.presentation.ladder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.ladder.usecase.GetChallengerLadderUseCase
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.presentation.common.toUiText
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
class LadderViewModel @Inject constructor(
    private val getChallengerLadder: GetChallengerLadderUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LadderState())
    val state: StateFlow<LadderState> = _state.asStateFlow()

    private val _effects = Channel<LadderEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var hasStarted = false

    fun onEvent(event: LadderEvent) {
        when (event) {
            LadderEvent.ScreenOpened -> start()
            LadderEvent.Retry -> load()
            is LadderEvent.RegionSelected -> {
                _state.update { it.copy(region = event.region) }
                load()
            }
            LadderEvent.BackClicked -> viewModelScope.launch {
                _effects.send(LadderEffect.NavigateBack)
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
            getChallengerLadder(_state.value.region)
                .onSuccess { entries -> _state.update { it.copy(isLoading = false, entries = entries) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.toUiText()) }
                }
        }
    }
}
