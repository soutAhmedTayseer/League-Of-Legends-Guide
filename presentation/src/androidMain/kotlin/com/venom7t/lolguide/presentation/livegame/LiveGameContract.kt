package com.venom7t.lolguide.presentation.livegame

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.livegame.model.LiveGame
import com.venom7t.lolguide.presentation.common.UiText

@Immutable
data class LiveGameState(
    val isLoading: Boolean = true,
    val patchVersion: String? = null,
    val game: LiveGame? = null,
    /** True when the lookup succeeded but the summoner is simply not in a game right now. */
    val notInGame: Boolean = false,
    val error: UiText? = null,
)

sealed interface LiveGameEvent {
    data object ScreenOpened : LiveGameEvent
    data object Retry : LiveGameEvent
    data object BackClicked : LiveGameEvent
}

sealed interface LiveGameEffect {
    data object NavigateBack : LiveGameEffect
}
