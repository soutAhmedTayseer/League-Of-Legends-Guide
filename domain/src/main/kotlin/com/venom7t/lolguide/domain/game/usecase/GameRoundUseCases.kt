package com.venom7t.lolguide.domain.game.usecase

import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GameStats
import com.venom7t.lolguide.domain.game.model.GuessResult
import com.venom7t.lolguide.domain.game.model.RoundOutcome
import com.venom7t.lolguide.domain.game.model.RoundProgress
import com.venom7t.lolguide.domain.game.repository.GameProgressRepository
import com.venom7t.lolguide.domain.sync.repository.SyncRepository
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
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(
        round: RoundProgress,
        guess: Champion,
        answer: Champion,
    ): Pair<RoundProgress, GuessResult> {
        val result = evaluateGuess(guess, answer)
        val guessedIds = round.guessedIds + guess.id
        // No guess limit: a round only ends here on a correct guess. The
        // other way out is GiveUpRoundUseCase, an explicit player choice.
        val outcome = if (result.isCorrect) RoundOutcome.WON else RoundOutcome.IN_PROGRESS
        val updated = round.copy(guessedIds = guessedIds, outcome = outcome)
        repository.saveRound(updated)

        if (updated.isFinished) {
            val stats = repository.observeStats(round.mode).first()
                .applyResult(round.epochDay, won = outcome == RoundOutcome.WON)
            repository.saveStats(stats)
            // Best-effort: losing this to being offline is fine, the local
            // record (the source of truth for this device) is already saved.
            syncRepository.pushGameStats(round.mode, stats)
        }

        return updated to result
    }
}

/**
 * Ends an unfinished round as a loss at the player's request and reveals the
 * answer. Confirmed in the UI first (AGENTS.md §13) since it forfeits the
 * round's streak the same way running out of guesses used to.
 */
class GiveUpRoundUseCase @Inject constructor(
    private val repository: GameProgressRepository,
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(round: RoundProgress): RoundProgress {
        val updated = round.copy(outcome = RoundOutcome.GAVE_UP)
        repository.saveRound(updated)

        val stats = repository.observeStats(round.mode).first().applyResult(round.epochDay, won = false)
        repository.saveStats(stats)
        syncRepository.pushGameStats(round.mode, stats)

        return updated
    }
}
