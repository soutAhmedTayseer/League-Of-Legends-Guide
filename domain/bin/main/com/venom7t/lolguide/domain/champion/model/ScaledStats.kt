package com.venom7t.lolguide.domain.champion.model

/**
 * A champion's stats at a chosen level.
 *
 * **These are derived values, not source data.** Data Dragon ships only the
 * level-1 base and a per-level growth figure; everything between is computed
 * here. `AGENTS.md` §1 requires derived numbers be labelled as such in the UI,
 * which is why this type is deliberately distinct from [ChampionStats] rather
 * than reusing it — a screen cannot accidentally present a computed value as
 * if it came from Riot.
 */
data class ScaledStats(
    val level: Int,
    val hp: Double,
    val mp: Double,
    val armor: Double,
    val spellBlock: Double,
    val attackDamage: Double,
    val attackSpeed: Double,
    val hpRegen: Double,
    val mpRegen: Double,
    /** Not level-scaled in League; carried through so the UI has one source. */
    val moveSpeed: Double,
    val attackRange: Double,
)

object ChampionStatCalculator {

    const val MIN_LEVEL = 1
    const val MAX_LEVEL = 18

    /**
     * League's growth curve. Stats do not scale linearly: each level is worth
     * slightly more than the last.
     *
     *     stat(n) = base + growth * (n - 1) * (0.7025 + 0.0175 * (n - 1))
     *
     * At level 1 the multiplier term is zero, so the result is exactly the
     * base stat. That identity is the correctness check on this function.
     */
    private fun scale(base: Double, growth: Double, level: Int): Double {
        val steps = (level - 1).toDouble()
        return base + growth * steps * (0.7025 + 0.0175 * steps)
    }

    /**
     * Attack speed is the exception: `attackspeedperlevel` is a *percentage*
     * increase applied to the base, not a flat addition. Treating it like the
     * other stats overstates late-game attack speed by an order of magnitude.
     */
    private fun scaleAttackSpeed(base: Double, growthPercent: Double, level: Int): Double {
        val steps = (level - 1).toDouble()
        val multiplier = (growthPercent / 100.0) * steps * (0.7025 + 0.0175 * steps)
        return base * (1.0 + multiplier)
    }

    fun statsAtLevel(stats: ChampionStats, level: Int): ScaledStats {
        val clamped = level.coerceIn(MIN_LEVEL, MAX_LEVEL)
        return ScaledStats(
            level = clamped,
            hp = scale(stats.hp, stats.hpPerLevel, clamped),
            mp = scale(stats.mp, stats.mpPerLevel, clamped),
            armor = scale(stats.armor, stats.armorPerLevel, clamped),
            spellBlock = scale(stats.spellBlock, stats.spellBlockPerLevel, clamped),
            attackDamage = scale(stats.attackDamage, stats.attackDamagePerLevel, clamped),
            attackSpeed = scaleAttackSpeed(stats.attackSpeed, stats.attackSpeedPerLevel, clamped),
            hpRegen = scale(stats.hpRegen, stats.hpRegenPerLevel, clamped),
            mpRegen = scale(stats.mpRegen, stats.mpRegenPerLevel, clamped),
            moveSpeed = stats.moveSpeed,
            attackRange = stats.attackRange,
        )
    }
}
