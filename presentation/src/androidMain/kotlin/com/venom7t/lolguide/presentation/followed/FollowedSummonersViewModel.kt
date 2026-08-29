package com.venom7t.lolguide.presentation.followed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.followed.model.FollowedSummoner
import com.venom7t.lolguide.domain.followed.repository.FollowedSummonerRepository
import com.venom7t.lolguide.domain.followed.usecase.ObserveFollowedSummonersUseCase
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.domain.summoner.usecase.SearchSummonerUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FollowedSummonersViewModel (
    private val observeFollowedSummoners: ObserveFollowedSummonersUseCase,
    private val followedRepository: FollowedSummonerRepository,
    private val searchSummoner: SearchSummonerUseCase,
    private val resolvePatch: ResolvePatchUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(FollowedSummonersState())
    val state: StateFlow<FollowedSummonersState> = _state.asStateFlow()

    private val _effects = Channel<FollowedSummonersEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var hasStarted = false

    fun onEvent(event: FollowedSummonersEvent) {
        when (event) {
            FollowedSummonersEvent.ScreenOpened -> start()

            is FollowedSummonersEvent.SummonerClicked -> viewModelScope.launch {
                _effects.send(
                    FollowedSummonersEffect.NavigateToProfile(
                        riotIdName = event.summoner.riotIdName,
                        riotIdTagline = event.summoner.riotIdTagline,
                        region = event.summoner.region,
                    ),
                )
            }

            is FollowedSummonersEvent.UnfollowClicked -> viewModelScope.launch {
                followedRepository.unfollow(event.puuid)
            }

            FollowedSummonersEvent.BackClicked -> viewModelScope.launch {
                _effects.send(FollowedSummonersEffect.NavigateBack)
            }
        }
    }

    private fun start() {
        if (hasStarted) return
        hasStarted = true

        viewModelScope.launch {
            resolvePatch().onSuccess { patch -> _state.update { it.copy(patchVersion = patch.version) } }
        }

        observeFollowedSummoners()
            .onEach { followed ->
                _state.update { it.copy(isLoading = false, followed = followed) }
                resolveProfiles(followed)
            }
            .launchIn(viewModelScope)
    }

    /**
     * A followed record only stores the Riot id, not a profile icon or level
     * (Phase 4 decision: local-only, no per-row fetch by default) -- this is
     * the one place that's worth the extra call anyway, since the list a
     * player actually follows is small (a handful of summoners), unlike the
     * few-hundred-row Challenger ladder where the same idea would not scale.
     * A row whose lookup fails just keeps showing the fallback avatar
     * (AGENTS.md §8.2) -- it never blocks or errors the rest of the list.
     */
    private fun resolveProfiles(followed: List<FollowedSummoner>) {
        followed.forEach { summoner ->
            if (_state.value.resolvedProfiles.containsKey(summoner.puuid)) return@forEach
            viewModelScope.launch {
                searchSummoner("${summoner.riotIdName}#${summoner.riotIdTagline}", summoner.region)
                    .onSuccess { resolved ->
                        _state.update {
                            it.copy(resolvedProfiles = it.resolvedProfiles + (summoner.puuid to resolved))
                        }
                    }
            }
        }
    }
}
