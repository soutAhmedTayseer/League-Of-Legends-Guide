package com.venom7t.lolguide.presentation.followed

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.followed.model.FollowedSummoner
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.summoner.model.Summoner

@Immutable
data class FollowedSummonersState(
    val isLoading: Boolean = true,
    val followed: List<FollowedSummoner> = emptyList(),
    val patchVersion: String? = null,
    /** Resolved lazily per row -- see FollowedSummonersViewModel.resolveProfile. Keyed by puuid. */
    val resolvedProfiles: Map<String, Summoner> = emptyMap(),
)

sealed interface FollowedSummonersEvent {
    data object ScreenOpened : FollowedSummonersEvent
    data class SummonerClicked(val summoner: FollowedSummoner) : FollowedSummonersEvent
    data class UnfollowClicked(val puuid: String) : FollowedSummonersEvent
    data object BackClicked : FollowedSummonersEvent
}

sealed interface FollowedSummonersEffect {
    data class NavigateToProfile(
        val riotIdName: String,
        val riotIdTagline: String,
        val region: Region,
    ) : FollowedSummonersEffect
    data object NavigateBack : FollowedSummonersEffect
}
