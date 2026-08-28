package com.venom7t.lolguide.presentation.game.round

import androidx.compose.runtime.Immutable
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GameStats
import com.venom7t.lolguide.domain.game.model.GuessResult
import com.venom7t.lolguide.domain.game.model.RoundOutcome
import com.venom7t.lolguide.presentation.common.UiText

@Immutable
data class GameRoundState(
    val mode: GameMode = GameMode.CLASSIC,
    val isLoading: Boolean = true,
    val query: String = "",
    val suggestions: List<Champion> = emptyList(),
    val guesses: List<GuessResult> = emptyList(),
    val guessesRemaining: Int = 0,
    val outcome: RoundOutcome = RoundOutcome.IN_PROGRESS,
    /** The answer's name, revealed only once the round is finished. */
    val revealedAnswerName: String? = null,
    /** For Ability mode: the answer's ability icon file name + patch. Null while loading. */
    val abilityIconFileName: String? = null,
    val patchVersion: String? = null,
    val answerChampionId: String? = null,
    val stats: GameStats? = null,
    val error: UiText? = null,
) {
    val isFinished: Boolean get() = outcome != RoundOutcome.IN_PROGRESS

    /** Splash art zooms out one step per wrong guess -- more guesses used means less zoom. */
    val splashZoomStep: Int get() = guesses.size
}

sealed interface GameRoundEvent {
    data object ScreenOpened : GameRoundEvent
    data class QueryChanged(val query: String) : GameRoundEvent
    data class SuggestionSelected(val champion: Champion) : GameRoundEvent
    data object BackClicked : GameRoundEvent
    data object PlayAgainDismissed : GameRoundEvent
}

sealed interface GameRoundEffect {
    data object NavigateBack : GameRoundEffect
}
