package com.venom7t.lolguide.presentation.match.detail

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.match.model.MatchDetail
import com.venom7t.lolguide.presentation.common.UiText

@Immutable
data class MatchDetailState(
    val isLoading: Boolean = true,
    val patchVersion: String? = null,
    val detail: MatchDetail? = null,
    /** Resolves each participant's numeric Riot championId to real art/name -- see MatchMappers.kt. */
    val championsByKey: Map<String, Champion> = emptyMap(),
    /** Highlights the viewing summoner's own row among the ten participants. */
    val viewingPuuid: String = "",
    val error: UiText? = null,
)

sealed interface MatchDetailEvent {
    data object ScreenOpened : MatchDetailEvent
    data object Retry : MatchDetailEvent
    data object BackClicked : MatchDetailEvent
}

sealed interface MatchDetailEffect {
    data object NavigateBack : MatchDetailEffect
}
