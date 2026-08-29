package com.venom7t.lolguide.presentation.mastery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.venom7t.lolguide.domain.mastery.usecase.GetChampionMasteriesUseCase
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.presentation.common.toUiText
import com.venom7t.lolguide.presentation.navigation.MasteryRoute
import org.koin.android.annotation.KoinViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@KoinViewModel
class MasteryViewModel (
    savedStateHandle: SavedStateHandle,
    private val getChampionMasteries: GetChampionMasteriesUseCase,
    private val resolvePatch: ResolvePatchUseCase,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<MasteryRoute>()
    private val region = Region.valueOf(route.region)

    private val _state = MutableStateFlow(MasteryState())
    val state: StateFlow<MasteryState> = _state.asStateFlow()

    private val _effects = Channel<MasteryEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var hasStarted = false

    fun onEvent(event: MasteryEvent) {
        when (event) {
            MasteryEvent.ScreenOpened -> start()
            MasteryEvent.Retry -> load()
            MasteryEvent.BackClicked -> viewModelScope.launch {
                _effects.send(MasteryEffect.NavigateBack)
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
            val patch = resolvePatch().getOrNull()?.version
            getChampionMasteries(route.puuid, region)
                .onSuccess { masteries ->
                    _state.update {
                        it.copy(isLoading = false, patchVersion = patch, masteries = masteries)
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.toUiText()) }
                }
        }
    }
}
