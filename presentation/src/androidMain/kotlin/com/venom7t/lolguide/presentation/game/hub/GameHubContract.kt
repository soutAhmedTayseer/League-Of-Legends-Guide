package com.venom7t.lolguide.presentation.game.hub

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GameStats

@Immutable
data class GameHubState(
    val stats: Map<GameMode, GameStats> = emptyMap(),
    /** Time left until today's daily puzzles reset, at UTC midnight. */
    val millisUntilReset: Long = 0L,
)

sealed interface GameHubEvent {
    data object ScreenOpened : GameHubEvent
    data class ModeClicked(val mode: GameMode) : GameHubEvent
    data object BackClicked : GameHubEvent
}

sealed interface GameHubEffect {
    data class NavigateToRound(val mode: GameMode) : GameHubEffect
    data object NavigateBack : GameHubEffect
}
