package com.venom7t.lolguide.domain.item.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionStatCalculator
import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.domain.item.model.ItemStats

/**
 * Champion + level + items, totalled up.
 *
 * **Everything this produces is derived.** Riot ships level-1 base stats and
 * per-item stats; the combination, the effective-HP figures and the DPS
 * estimate are all this app's arithmetic and must be labelled as such in the
 * UI (`AGENTS.md` §1).
 */
data class BuildResult(
    val level: Int,
    val itemCount: Int,
    val totalGold: Int,

    val health: Double,
    val mana: Double,
    val attackDamage: Double,
    val abilityPower: Double,
    val armor: Double,
    val magicResist: Double,
    val attackSpeed: Double,
    val critChancePercent: Double,
    val moveSpeed: Double,

    /** Damage absorbed before dying, against each damage type. */
    val effectiveHealthPhysical: Double,
    val effectiveHealthMagic: Double,

    /**
     * Auto-attack damage per second against an unarmoured target, with no
     * abilities and no item passives. See [DpsAssumption].
     */
    val estimatedAutoAttackDps: Double,

    /**
     * True when at least one selected item had no machine-readable stats, so
     * the totals understate the build. The UI must say so rather than
     * presenting an incomplete sum as the answer.
     */
    val hasUnpublishedItemStats: Boolean,
)

/**
 * The assumptions behind [BuildResult.estimatedAutoAttackDps], which the UI is
 * required to show alongside it -- a DPS figure without its assumptions is
 * misinformation, not a simplification.
 *
 * An enum rather than strings because `:domain` holds no user-facing text
 * (AGENTS.md section 10); `:presentation` maps these to translated resources.
 */
enum class DpsAssumption {
    TARGET_HAS_NO_ARMOR,
    AUTO_ATTACKS_ONLY,
    NO_ITEM_PASSIVES,
    DEFAULT_CRIT_MULTIPLIER,
    ;

    companion object {
        val ALL: List<DpsAssumption> = entries
    }
}

@Factory
class BuildSimulator() {

    fun simulate(champion: Champion, items: List<Item>, level: Int): BuildResult {
        val base = ChampionStatCalculator.statsAtLevel(champion.stats, level)

        val itemStats = items.fold(ItemStats()) { accumulator, item -> accumulator + item.stats }
        val hasUnpublished = items.any { it.stats.isEmpty }

        val health = base.hp + itemStats.health
        val armor = base.armor + itemStats.armor
        val magicResist = base.spellBlock + itemStats.magicResist
        val attackDamage = base.attackDamage + itemStats.attackDamage
        val critChance = itemStats.critChancePercent.coerceAtMost(MAX_CRIT_PERCENT)

        // base.attackSpeed already folds in per-level growth, which League
        // applies as a percentage of the champion's base. Item attack speed is
        // a further percentage bonus on top of that.
        val attackSpeed = base.attackSpeed * (1.0 + itemStats.attackSpeedPercent / 100.0)

        val moveSpeed = (base.moveSpeed + itemStats.moveSpeedFlat) *
            (1.0 + itemStats.moveSpeedPercent / 100.0)

        return BuildResult(
            level = level,
            itemCount = items.size,
            totalGold = items.sumOf { it.gold.total },
            health = health,
            mana = base.mp + itemStats.mana,
            attackDamage = attackDamage,
            abilityPower = itemStats.abilityPower,
            armor = armor,
            magicResist = magicResist,
            attackSpeed = attackSpeed,
            critChancePercent = critChance,
            moveSpeed = moveSpeed,
            // Resistances reduce incoming damage by resist/(100+resist), which
            // is equivalent to multiplying survivability by (1 + resist/100).
            effectiveHealthPhysical = health * (1.0 + armor / 100.0),
            effectiveHealthMagic = health * (1.0 + magicResist / 100.0),
            estimatedAutoAttackDps = attackDamage *
                attackSpeed *
                (1.0 + (critChance / 100.0) * (CRIT_DAMAGE_MULTIPLIER - 1.0)),
            hasUnpublishedItemStats = hasUnpublished,
        )
    }

    private companion object {
        const val MAX_CRIT_PERCENT = 100.0

        /** A critical strike deals 175% of normal damage by default. */
        const val CRIT_DAMAGE_MULTIPLIER = 1.75
    }
}
