package com.venom7t.lolguide.presentation.match.detail

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.match.model.MatchDetail
import com.venom7t.lolguide.presentation.common.UiText

@Immutable
data class MatchDetailState(
    val isLoading: Boolean = true,
    val patchVersion: String? = null,
    val detail: MatchDetail? = null,
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
