package com.venom7t.lolguide.presentation.ladder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.ladder.usecase.GetChallengerLadderUseCase
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.domain.summoner.usecase.GetSummonerByPuuidUseCase
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
    private val getSummonerByPuuid: GetSummonerByPuuidUseCase,
    private val resolvePatch: ResolvePatchUseCase,
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
            val alreadyShowing = _state.value.entries.isNotEmpty()
            _state.update {
                it.copy(
                    isLoading = !alreadyShowing,
                    isRefreshing = alreadyShowing,
                    resolvedProfiles = emptyMap(),
                    error = null,
                )
            }

            val region = _state.value.region
            resolvePatch().onSuccess { patch -> _state.update { it.copy(patchVersion = patch.version) } }

            getChallengerLadder(region)
                .onSuccess { entries ->
                    _state.update { it.copy(isLoading = false, isRefreshing = false, entries = entries) }
                    resolveTopEntries(entries, region)
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(isLoading = false, isRefreshing = false, error = throwable.toUiText())
                    }
                }
        }
    }

    /**
     * LEAGUE-V4's ladder payload never carries a name or icon (Riot dropped
     * that for privacy) -- only [RESOLVE_COUNT] rows are worth spending a
     * puuid lookup on. The ladder can run to a few hundred entries; resolving
     * all of them would be a few hundred extra keyed requests just to open
     * this screen, which risks the dev key's rate limit far more than it's
     * worth. The rest keep the "#N —" placeholder, a real degraded state
     * rather than a bug (AGENTS.md §8.2).
     */
    private fun resolveTopEntries(entries: List<com.venom7t.lolguide.domain.ladder.model.LadderEntry>, region: Region) {
        entries.take(RESOLVE_COUNT).forEach { entry ->
            viewModelScope.launch {
                getSummonerByPuuid(entry.puuid, region).onSuccess { resolved ->
                    _state.update { it.copy(resolvedProfiles = it.resolvedProfiles + (entry.puuid to resolved)) }
                }
            }
        }
    }

    private companion object {
        const val RESOLVE_COUNT = 25
    }
}
