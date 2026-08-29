package com.venom7t.lolguide.presentation.match.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.venom7t.lolguide.domain.champion.usecase.ObserveChampionsUseCase
import com.venom7t.lolguide.domain.match.usecase.GetMatchDetailUseCase
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.presentation.common.toUiText
import com.venom7t.lolguide.presentation.navigation.MatchDetailRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MatchDetailViewModel (
    savedStateHandle: SavedStateHandle,
    private val getMatchDetail: GetMatchDetailUseCase,
    private val resolvePatch: ResolvePatchUseCase,
    private val observeChampions: ObserveChampionsUseCase,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<MatchDetailRoute>()
    private val region = Region.valueOf(route.region)

    private val _state = MutableStateFlow(MatchDetailState(viewingPuuid = route.viewingPuuid))
    val state: StateFlow<MatchDetailState> = _state.asStateFlow()

    private val _effects = Channel<MatchDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var hasStarted = false

    fun onEvent(event: MatchDetailEvent) {
        when (event) {
            MatchDetailEvent.ScreenOpened -> start()
            MatchDetailEvent.Retry -> load()
            MatchDetailEvent.BackClicked -> viewModelScope.launch {
                _effects.send(MatchDetailEffect.NavigateBack)
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
            val championsByKey = observeChampions().first().associateBy { it.key }
            getMatchDetail(route.matchId, region)
                .onSuccess { detail ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            detail = detail,
                            patchVersion = patch,
                            championsByKey = championsByKey,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.toUiText()) }
                }
        }
    }
}
