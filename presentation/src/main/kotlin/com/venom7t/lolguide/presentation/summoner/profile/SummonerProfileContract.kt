package com.venom7t.lolguide.presentation.summoner.profile

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.mastery.model.ChampionMastery
import com.venom7t.lolguide.domain.match.model.MatchSummary
import com.venom7t.lolguide.domain.summoner.model.RankedEntry
import com.venom7t.lolguide.domain.summoner.model.Summoner
import com.venom7t.lolguide.presentation.common.UiText

@Immutable
data class SummonerProfileState(
    val isLoading: Boolean = true,
    val patchVersion: String? = null,
    val summoner: Summoner? = null,
    val rankedEntries: List<RankedEntry> = emptyList(),
    val matches: List<MatchSummary> = emptyList(),
    val isLoadingMoreMatches: Boolean = false,
    val topMasteries: List<ChampionMastery> = emptyList(),
    val isInLiveGame: Boolean = false,
    val isFollowed: Boolean = false,
    val error: UiText? = null,
)

sealed interface SummonerProfileEvent {
    data object ScreenOpened : SummonerProfileEvent
    data object Retry : SummonerProfileEvent
    data object LoadMoreMatches : SummonerProfileEvent
    data class MatchClicked(val matchId: String) : SummonerProfileEvent
    data object FollowClicked : SummonerProfileEvent
    data object LiveGameClicked : SummonerProfileEvent
    data object MasteriesClicked : SummonerProfileEvent
}

sealed interface SummonerProfileEffect {
    data class NavigateToMatchDetail(val matchId: String, val viewingPuuid: String) : SummonerProfileEffect
    data class NavigateToLiveGame(val puuid: String) : SummonerProfileEffect
    data class NavigateToMasteries(val puuid: String) : SummonerProfileEffect
    data class ShowSnackbar(val message: UiText) : SummonerProfileEffect
}
