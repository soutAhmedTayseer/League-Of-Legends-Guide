package com.venom7t.lolguide.presentation.followed

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.followed.model.FollowedSummoner
import com.venom7t.lolguide.domain.onboarding.model.Region

@Immutable
data class FollowedSummonersState(
    val followed: List<FollowedSummoner> = emptyList(),
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
