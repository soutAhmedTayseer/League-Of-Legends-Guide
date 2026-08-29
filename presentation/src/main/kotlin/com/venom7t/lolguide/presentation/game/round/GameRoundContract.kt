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
    val outcome: RoundOutcome = RoundOutcome.IN_PROGRESS,
    /** The answer's name, revealed only once the round is finished. */
    val revealedAnswerName: String? = null,
    /** For Ability mode: the answer's ability icon file name + patch. Null while loading. */
    val abilityIconFileName: String? = null,
    val patchVersion: String? = null,
    val answerChampionId: String? = null,
    val stats: GameStats? = null,
    /** Gates the give-up confirmation dialog (AGENTS.md §13). */
    val pendingGiveUp: Boolean = false,
    /** Gates the leave-mid-round confirmation dialog (AGENTS.md §13). */
    val pendingBack: Boolean = false,
    val error: UiText? = null,
) {
    val isFinished: Boolean get() = outcome != RoundOutcome.IN_PROGRESS

    /**
     * Splash art widens one step per wrong guess up to [GameMode.splashZoomSteps],
     * then holds at full reveal -- there is no guess limit any more, so this
     * must not keep growing forever.
     */
    val splashZoomStep: Int get() = guesses.size.coerceAtMost(mode.splashZoomSteps)
}

sealed interface GameRoundEvent {
    data object ScreenOpened : GameRoundEvent
    data class QueryChanged(val query: String) : GameRoundEvent
    data class SuggestionSelected(val champion: Champion) : GameRoundEvent
    data object BackClicked : GameRoundEvent
    data object BackConfirmed : GameRoundEvent
    data object BackCancelled : GameRoundEvent
    data object PlayAgainDismissed : GameRoundEvent
    data object GiveUpClicked : GameRoundEvent
    data object GiveUpConfirmed : GameRoundEvent
    data object GiveUpCancelled : GameRoundEvent
}

sealed interface GameRoundEffect {
    data object NavigateBack : GameRoundEffect
}
