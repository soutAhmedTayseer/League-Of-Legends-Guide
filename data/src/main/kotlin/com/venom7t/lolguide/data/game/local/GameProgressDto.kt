package com.venom7t.lolguide.data.game.local

import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GameStats
import com.venom7t.lolguide.domain.game.model.RoundOutcome
import com.venom7t.lolguide.domain.game.model.RoundProgress
import kotlinx.serialization.Serializable

/**
 * The on-disk shapes for [RoundProgress] and [GameStats], stored as JSON
 * strings in the shared preferences DataStore (Phase 6 plan §6.3). Kept
 * separate from the domain models so a Room/DataStore schema detail never
 * leaks into `:domain`.
 */
@Serializable
data class RoundProgressDto(
    val mode: String,
    val epochDay: Long,
    val answerChampionId: String,
    val guessedIds: List<String>,
    val outcome: String,
) {
    fun toDomain(): RoundProgress = RoundProgress(
        mode = GameMode.fromName(mode),
        epochDay = epochDay,
        answerChampionId = answerChampionId,
        guessedIds = guessedIds,
        outcome = RoundOutcome.entries.firstOrNull { it.name == outcome } ?: RoundOutcome.IN_PROGRESS,
    )

    companion object {
        fun fromDomain(round: RoundProgress) = RoundProgressDto(
            mode = round.mode.name,
            epochDay = round.epochDay,
            answerChampionId = round.answerChampionId,
            guessedIds = round.guessedIds,
            outcome = round.outcome.name,
        )
    }
}

@Serializable
data class GameStatsDto(
    val mode: String,
    val played: Int,
    val won: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val lastCompletedEpochDay: Long?,
) {
    fun toDomain(): GameStats = GameStats(
        mode = GameMode.fromName(mode),
        played = played,
        won = won,
        currentStreak = currentStreak,
        bestStreak = bestStreak,
        lastCompletedEpochDay = lastCompletedEpochDay,
    )

    companion object {
        fun fromDomain(stats: GameStats) = GameStatsDto(
            mode = stats.mode.name,
            played = stats.played,
            won = stats.won,
            currentStreak = stats.currentStreak,
            bestStreak = stats.bestStreak,
            lastCompletedEpochDay = stats.lastCompletedEpochDay,
        )
    }
}
