package com.venom7t.lolguide.presentation.summoner.profile

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.clash.model.ClashTeam
import com.venom7t.lolguide.domain.mastery.model.ChampionMastery
import com.venom7t.lolguide.domain.match.model.DuoStats
import com.venom7t.lolguide.domain.match.model.MatchSummary
import com.venom7t.lolguide.domain.summoner.model.RankedEntry
import com.venom7t.lolguide.domain.summoner.model.Summoner
import com.venom7t.lolguide.presentation.common.UiText

@Immutable
data class SummonerProfileState(
    val isLoading: Boolean = true,
    /** True only for a pull-to-refresh re-fetch, once the profile is already showing. */
    val isRefreshing: Boolean = false,
    val patchVersion: String? = null,
    val summoner: Summoner? = null,
    val rankedEntries: List<RankedEntry> = emptyList(),
    val matches: List<MatchSummary> = emptyList(),
    /**
     * Riot's match payloads only carry [Champion.key] (a numeric id), never
     * [Champion.id] or the display name -- this is how match rows resolve a
     * numeric id back to real art and a real name instead of showing the
     * number itself.
     */
    val championsByKey: Map<String, Champion> = emptyMap(),
    val isLoadingMoreMatches: Boolean = false,
    val topMasteries: List<ChampionMastery> = emptyList(),
    val isInLiveGame: Boolean = false,
    val isFollowed: Boolean = false,
    val duoStats: List<DuoStats> = emptyList(),
    /** Null means "not loaded yet or genuinely not registered" -- see ClashRepository's doc comment. */
    val clashTeam: ClashTeam? = null,
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
    data object LpHistoryClicked : SummonerProfileEvent
}

sealed interface SummonerProfileEffect {
    data class NavigateToMatchDetail(val matchId: String, val viewingPuuid: String) : SummonerProfileEffect
    data class NavigateToLiveGame(val puuid: String) : SummonerProfileEffect
    data class NavigateToMasteries(val puuid: String) : SummonerProfileEffect
    data class NavigateToLpHistory(val puuid: String, val riotIdName: String, val riotIdTagline: String) :
        SummonerProfileEffect
    data class ShowSnackbar(val message: UiText) : SummonerProfileEffect
}
