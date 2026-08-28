package com.venom7t.lolguide.domain.game.usecase

import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GameStats
import com.venom7t.lolguide.domain.game.model.GuessResult
import com.venom7t.lolguide.domain.game.model.RoundOutcome
import com.venom7t.lolguide.domain.game.model.RoundProgress
import com.venom7t.lolguide.domain.game.repository.GameProgressRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Returns today's round for [mode] -- resuming a saved one if it is still
 * today's, or starting a fresh one otherwise (a new day always means a new
 * answer, so yesterday's unfinished round cannot carry over).
 */
class StartOrResumeRoundUseCase @Inject constructor(
    private val repository: GameProgressRepository,
    private val pickDailyChampion: PickDailyChampionUseCase,
) {
    suspend operator fun invoke(champions: List<Champion>, mode: GameMode): RoundProgress? {
        val today = pickDailyChampion.currentEpochDay()
        val saved = repository.observeRound(mode).first()
        if (saved != null && saved.epochDay == today) return saved

        val answer = pickDailyChampion(champions, mode, today) ?: return null
        val fresh = RoundProgress.start(mode, today, answer.id)
        repository.saveRound(fresh)
        return fresh
    }
}

/**
 * Submits one guess: scores it, appends it to the round, persists the
 * updated round, and rolls lifetime stats forward the moment the round
 * finishes (win or out of guesses).
 */
class SubmitGuessUseCase @Inject constructor(
    private val repository: GameProgressRepository,
    private val evaluateGuess: EvaluateGuessUseCase,
) {
    suspend operator fun invoke(
        round: RoundProgress,
        guess: Champion,
        answer: Champion,
    ): Pair<RoundProgress, GuessResult> {
        val result = evaluateGuess(guess, answer)
        val guessedIds = round.guessedIds + guess.id
        val outcome = when {
            result.isCorrect -> RoundOutcome.WON
            guessedIds.size >= round.mode.maxGuesses -> RoundOutcome.LOST
            else -> RoundOutcome.IN_PROGRESS
        }
        val updated = round.copy(guessedIds = guessedIds, outcome = outcome)
        repository.saveRound(updated)

        if (updated.isFinished) {
            val stats = repository.observeStats(round.mode).first()
            repository.saveStats(stats.applyResult(round.epochDay, won = outcome == RoundOutcome.WON))
        }

        return updated to result
    }
}
