package com.venom7t.lolguide.domain.timer.model

/**
 * A running cooldown for an enemy's summoner spell, tracked per lane and
 * slot (each lane gets two spell slots -- a summoner carries two spells).
 *
 * [durationSeconds] is the spell's base cooldown from Data Dragon
 * ([com.venom7t.lolguide.domain.spell.model.SummonerSpell.cooldownSeconds]),
 * not a hardcoded literal -- unlike the objective presets in
 * [GameTimerPreset], a spell's cooldown is patch data, so it has to come
 * from the cache rather than be written into this class (AGENTS.md §1).
 * There is no way for this app to know the enemy's actual ability haste, so
 * the timer necessarily shows the base cooldown rather than their true one.
 */
data class SpellTimer(
    val lane: EnemyLane,
    val slotIndex: Int,
    val spellId: String,
    val startedAtEpochMillis: Long,
    val durationSeconds: Int,
) {
    fun remainingSeconds(nowEpochMillis: Long): Int {
        val elapsed = ((nowEpochMillis - startedAtEpochMillis) / 1000).toInt()
        return (durationSeconds - elapsed).coerceAtLeast(0)
    }

    fun isExpired(nowEpochMillis: Long): Boolean = remainingSeconds(nowEpochMillis) <= 0
}
