package com.venom7t.lolguide.domain.game.repository

import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GameStats
import com.venom7t.lolguide.domain.game.model.RoundProgress
import kotlinx.coroutines.flow.Flow

/**
 * Persists round-in-progress and lifetime stats per mode, device-local
 * (Phase 6 plan -- no account/leaderboard, consistent with the game being
 * playable before Phase 5's auth existed).
 */
interface GameProgressRepository {

    /** Null when no round has been saved for [mode] yet today or ever. */
    fun observeRound(mode: GameMode): Flow<RoundProgress?>

    suspend fun saveRound(round: RoundProgress)

    fun observeStats(mode: GameMode): Flow<GameStats>

    suspend fun saveStats(stats: GameStats)
}
