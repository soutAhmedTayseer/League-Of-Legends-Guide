package com.venom7t.lolguide.presentation.followed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.followed.repository.FollowedSummonerRepository
import com.venom7t.lolguide.domain.followed.usecase.ObserveFollowedSummonersUseCase
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

@HiltViewModel
class FollowedSummonersViewModel @Inject constructor(
    private val observeFollowedSummoners: ObserveFollowedSummonersUseCase,
    private val followedRepository: FollowedSummonerRepository,
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
        observeFollowedSummoners()
            .onEach { followed -> _state.update { it.copy(followed = followed) } }
            .launchIn(viewModelScope)
    }
}
