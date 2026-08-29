package com.venom7t.lolguide.domain.timer.model

/**
 * A running or completed countdown for one of League's respawn timers.
 *
 * These durations are game constants, not data from any API -- League ships
 * no endpoint for objective timers, so they are hardcoded here deliberately
 * (the one place in this codebase where a hardcoded gameplay number is
 * correct, because it is not champion/patch data subject to `AGENTS.md` §1,
 * it is a rule of the game itself, unchanged across patches for years).
 */
enum class GameTimerPreset(val durationSeconds: Int) {
    BARON(360),
    DRAGON(300),
    HERALD(360),
    WARD(150),
    ;
}

data class GameTimer(
    val id: Long,
    val preset: GameTimerPreset,
    val startedAtEpochMillis: Long,
    val durationSeconds: Int = preset.durationSeconds,
) {
    fun remainingSeconds(nowEpochMillis: Long): Int {
        val elapsed = ((nowEpochMillis - startedAtEpochMillis) / 1000).toInt()
        return (durationSeconds - elapsed).coerceAtLeast(0)
    }

    fun isExpired(nowEpochMillis: Long): Boolean = remainingSeconds(nowEpochMillis) <= 0
}
