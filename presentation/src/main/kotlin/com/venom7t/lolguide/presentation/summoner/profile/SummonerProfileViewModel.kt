package com.venom7t.lolguide.presentation.summoner.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.venom7t.lolguide.domain.clash.usecase.GetClashTeamUseCase
import com.venom7t.lolguide.domain.followed.usecase.ToggleFollowedSummonerUseCase
import com.venom7t.lolguide.domain.livegame.usecase.GetLiveGameUseCase
import com.venom7t.lolguide.domain.mastery.usecase.GetChampionMasteriesUseCase
import com.venom7t.lolguide.domain.match.usecase.ComputeDuoStatsUseCase
import com.venom7t.lolguide.domain.match.usecase.GetMatchHistoryUseCase
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.domain.summoner.model.Summoner
import com.venom7t.lolguide.domain.summoner.usecase.GetRankedEntriesUseCase
import com.venom7t.lolguide.domain.summoner.usecase.SearchSummonerUseCase
import com.venom7t.lolguide.presentation.common.toUiText
import com.venom7t.lolguide.presentation.navigation.SummonerProfileRoute
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
class SummonerProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val searchSummoner: SearchSummonerUseCase,
    private val getRankedEntries: GetRankedEntriesUseCase,
    private val getMatchHistory: GetMatchHistoryUseCase,
    private val getChampionMasteries: GetChampionMasteriesUseCase,
    private val getLiveGame: GetLiveGameUseCase,
    private val computeDuoStats: ComputeDuoStatsUseCase,
    private val getClashTeam: GetClashTeamUseCase,
    private val resolvePatch: ResolvePatchUseCase,
    private val toggleFollowed: ToggleFollowedSummonerUseCase,
    private val followedRepository: com.venom7t.lolguide.domain.followed.repository.FollowedSummonerRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<SummonerProfileRoute>()
    private val region = Region.valueOf(route.region)

    private val _state = MutableStateFlow(SummonerProfileState())
    val state: StateFlow<SummonerProfileState> = _state.asStateFlow()

    private val _effects = Channel<SummonerProfileEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var hasStarted = false
    private var resolvedSummoner: Summoner? = null

    fun onEvent(event: SummonerProfileEvent) {
        when (event) {
            SummonerProfileEvent.ScreenOpened -> start()
            SummonerProfileEvent.Retry -> load()
            SummonerProfileEvent.LoadMoreMatches -> loadMoreMatches()

            is SummonerProfileEvent.MatchClicked -> viewModelScope.launch {
                val puuid = resolvedSummoner?.puuid ?: return@launch
                _effects.send(SummonerProfileEffect.NavigateToMatchDetail(event.matchId, puuid))
            }

            SummonerProfileEvent.LiveGameClicked -> viewModelScope.launch {
                val puuid = resolvedSummoner?.puuid ?: return@launch
                _effects.send(SummonerProfileEffect.NavigateToLiveGame(puuid))
            }

            SummonerProfileEvent.MasteriesClicked -> viewModelScope.launch {
                val puuid = resolvedSummoner?.puuid ?: return@launch
                _effects.send(SummonerProfileEffect.NavigateToMasteries(puuid))
            }

            SummonerProfileEvent.FollowClicked -> followToggle()

            SummonerProfileEvent.LpHistoryClicked -> viewModelScope.launch {
                val summoner = resolvedSummoner ?: return@launch
                _effects.send(
                    SummonerProfileEffect.NavigateToLpHistory(
                        puuid = summoner.puuid,
                        riotIdName = summoner.riotIdName,
                        riotIdTagline = summoner.riotIdTagline,
                    ),
                )
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

            // Only used for versioning champion/profile icons here -- a
            // failed patch lookup should not block a profile that has no art
            // to show without it, so it degrades to no icons rather than an error.
            val patch = resolvePatch().getOrNull()?.version
            _state.update { it.copy(patchVersion = patch) }

            val summoner = searchSummoner("${route.riotIdName}#${route.riotIdTagline}", region)
                .getOrElse { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.toUiText()) }
                    return@launch
                }
            resolvedSummoner = summoner

            val isFollowed = followedRepository.isFollowed(summoner.puuid)

            val ranked = getRankedEntries(summoner).getOrDefault(emptyList())
            val matches = getMatchHistory(summoner.puuid, region).getOrDefault(emptyList())
            val masteries = getChampionMasteries(summoner.puuid, region)
                .getOrDefault(emptyList())
                .take(MASTERY_PREVIEW_COUNT)
            // A failed live-game lookup should never block the rest of the
            // profile from showing -- it degrades to "not shown", not an error.
            val isInLiveGame = getLiveGame(summoner.puuid, region).getOrNull() != null
            val duoStats = computeDuoStats(summoner.puuid, matches.map { it.matchId }, region)
            // Same "not shown, not an error" treatment as live game and
            // mastery -- a Clash lookup failure should not block the rest of
            // the profile.
            val clashTeam = getClashTeam(summoner.summonerId, region).getOrNull()

            _state.update {
                it.copy(
                    isLoading = false,
                    summoner = summoner,
                    rankedEntries = ranked,
                    matches = matches,
                    topMasteries = masteries,
                    isFollowed = isFollowed,
                    isInLiveGame = isInLiveGame,
                    duoStats = duoStats,
                    clashTeam = clashTeam,
                    error = null,
                )
            }
        }
    }

    private fun loadMoreMatches() {
        val summoner = resolvedSummoner ?: return
        if (_state.value.isLoadingMoreMatches) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingMoreMatches = true) }
            getMatchHistory(summoner.puuid, region, startIndex = _state.value.matches.size)
                .onSuccess { more ->
                    _state.update {
                        it.copy(isLoadingMoreMatches = false, matches = it.matches + more)
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoadingMoreMatches = false) }
                    _effects.send(SummonerProfileEffect.ShowSnackbar(throwable.toUiText()))
                }
        }
    }

    private fun followToggle() {
        val summoner = resolvedSummoner ?: return
        viewModelScope.launch {
            val nowFollowed = toggleFollowed(summoner)
            _state.update { it.copy(isFollowed = nowFollowed) }
        }
    }

    private companion object {
        const val MASTERY_PREVIEW_COUNT = 5
    }
}
