package com.venom7t.lolguide.domain.game.usecase

import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.game.model.GameMode
import javax.inject.Inject
import kotlin.math.absoluteValue

/**
 * Picks the day's answer deterministically, so every player on the same
 * calendar day gets the same champion with no server involved.
 *
 * The pool is the cached champion list, and each [GameMode] hashes
 * separately so the three modes do not all land on the same champion.
 * [epochDay] is computed from wall-clock time plus the device's UTC offset
 * rather than `java.time.LocalDate`, since `minSdk` is 24 and `java.time`
 * needs API 26+ or desugaring -- not worth adding for one date calculation
 * (Phase 6 plan).
 *
 * **Known consequence**, documented in the plan: the pool changes size when
 * Riot ships a champion, which shifts the modulo and can change what a past
 * day's answer was. Acceptable for a local game with no shared leaderboard.
 */
class PickDailyChampionUseCase @Inject constructor() {

    operator fun invoke(champions: List<Champion>, mode: GameMode, epochDay: Long): Champion? {
        if (champions.isEmpty()) return null
        // Sorted by id first so the pool's *order* is stable regardless of
        // whatever order the cache happens to return it in -- the index must
        // mean the same champion every time this runs, not just today.
        val sorted = champions.sortedBy { it.id }
        val hash = stableHash(epochDay, mode.name)
        val index = (hash.absoluteValue % sorted.size).toInt()
        return sorted[index]
    }

    fun currentEpochDay(): Long {
        val offsetMillis = java.util.TimeZone.getDefault().getOffset(System.currentTimeMillis())
        return (System.currentTimeMillis() + offsetMillis) / MILLIS_PER_DAY
    }

    /** A simple, deterministic string+long hash -- not cryptographic, just stable across runs. */
    private fun stableHash(epochDay: Long, salt: String): Long {
        var hash = epochDay
        for (char in salt) {
            hash = hash * 31 + char.code
        }
        return hash
    }

    private companion object {
        const val MILLIS_PER_DAY = 86_400_000L
    }
}
