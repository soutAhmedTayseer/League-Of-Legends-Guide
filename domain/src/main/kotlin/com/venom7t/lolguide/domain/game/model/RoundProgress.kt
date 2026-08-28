package com.venom7t.lolguide.domain.game.model

/**
 * How a round ended, or that it has not.
 *
 * [GAVE_UP] exists because there is no guess limit any more: the only other
 * way out of a round the player cannot solve is closing the app and never
 * finishing it, which would silently corrupt the streak (still "in
 * progress" forever). Giving up is an explicit, confirmed choice
 * (AGENTS.md §13) that counts as a loss for streak purposes, same as
 * [LOST] did before guesses were capped.
 */
enum class RoundOutcome { IN_PROGRESS, WON, LOST, GAVE_UP }

/**
 * A round in progress or finished, for one mode on one day.
 *
 * Persisted so closing the app mid-round resumes rather than restarts --
 * a daily puzzle that resets on a backgrounded app would be worse than no
 * persistence at all, because the answer is fixed and the player would simply
 * have lost their attempts.
 */
data class RoundProgress(
    val mode: GameMode,
    val epochDay: Long,
    val answerChampionId: String,
    /** Champion ids already guessed, oldest first. */
    val guessedIds: List<String>,
    val outcome: RoundOutcome,
) {
    val guessesUsed: Int get() = guessedIds.size

    val isFinished: Boolean get() = outcome != RoundOutcome.IN_PROGRESS

    companion object {
        fun start(mode: GameMode, epochDay: Long, answerChampionId: String) = RoundProgress(
            mode = mode,
            epochDay = epochDay,
            answerChampionId = answerChampionId,
            guessedIds = emptyList(),
            outcome = RoundOutcome.IN_PROGRESS,
        )
    }
}

/**
 * Lifetime statistics for one mode.
 *
 * [lastCompletedEpochDay] is what makes a streak a streak: a win only extends
 * the run when it lands the day after the previous one, otherwise the streak
 * restarts at 1.
 */
data class GameStats(
    val mode: GameMode,
    val played: Int = 0,
    val won: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedEpochDay: Long? = null,
) {
    val winRatePercent: Int
        get() = if (played == 0) 0 else (won * 100) / played

    /** Applies a finished round, returning the updated statistics. */
    fun applyResult(epochDay: Long, won: Boolean): GameStats {
        // A day already counted must not be counted twice -- replaying a
        // finished round would otherwise inflate the streak indefinitely.
        if (lastCompletedEpochDay == epochDay) return this

        val continues = won && lastCompletedEpochDay == epochDay - 1
        val streak = when {
            !won -> 0
            continues -> currentStreak + 1
            else -> 1
        }

        return copy(
            played = played + 1,
            won = this.won + if (won) 1 else 0,
            currentStreak = streak,
            bestStreak = maxOf(bestStreak, streak),
            lastCompletedEpochDay = epochDay,
        )
    }
}
