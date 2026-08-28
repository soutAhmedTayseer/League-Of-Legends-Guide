package com.venom7t.lolguide.domain.item.usecase

import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.domain.item.model.ItemStats
import javax.inject.Inject

/**
 * Gold value of a single unit of each stat, derived from the current patch.
 *
 * Riot publishes no such table, so the usual approach is to hardcode community
 * constants. This does not: those constants go stale silently every time a
 * basic item is re-costed, and a stale number presented as fact is exactly
 * what `AGENTS.md` §1 forbids.
 *
 * Instead each value is read off a basic item that sells precisely one stat,
 * from the same payload as the item being priced. The table therefore
 * re-derives itself every patch.
 */
data class StatGoldValues(
    val perAttackDamage: Double? = null,
    val perAbilityPower: Double? = null,
    val perHealth: Double? = null,
    val perMana: Double? = null,
    val perArmor: Double? = null,
    val perMagicResist: Double? = null,
    val perAttackSpeedPercent: Double? = null,
    val perCritChancePercent: Double? = null,
    val perHealthRegen: Double? = null,
) {
    /** True when no reference item resolved at all, so nothing can be priced. */
    val isEmpty: Boolean
        get() = listOf(
            perAttackDamage, perAbilityPower, perHealth, perMana, perArmor,
            perMagicResist, perAttackSpeedPercent, perCritChancePercent, perHealthRegen,
        ).all { it == null }
}

/**
 * The verdict for one item.
 *
 * @param percent stat value as a percentage of cost, or null when it cannot be
 *   computed at all.
 * @param isPartial true when at least one stat on the item had no reference
 *   price, so the figure understates the item. Reporting a partial result as a
 *   whole one would make items look worse than they are.
 * @param hasUnpublishedStats true when Riot shipped an empty stats block.
 *   Distinct from "worth nothing".
 */
data class GoldEfficiency(
    val percent: Double?,
    val statValueGold: Double,
    val isPartial: Boolean,
    val hasUnpublishedStats: Boolean,
)

class GoldEfficiencyCalculator @Inject constructor() {

    /**
     * Derives the price table from basic items in [items].
     *
     * Each reference item sells exactly one stat, so its cost divided by that
     * stat's magnitude is the gold-per-unit price.
     */
    fun deriveStatGoldValues(items: List<Item>): StatGoldValues {
        val byId = items.associateBy { it.id }

        fun price(itemId: String, stat: (ItemStats) -> Double): Double? {
            val item = byId[itemId] ?: return null
            val amount = stat(item.stats)
            if (amount <= 0.0 || item.gold.total <= 0) return null
            return item.gold.total / amount
        }

        return StatGoldValues(
            perAttackDamage = price(LONG_SWORD) { it.attackDamage },
            perAbilityPower = price(AMPLIFYING_TOME) { it.abilityPower },
            perHealth = price(RUBY_CRYSTAL) { it.health },
            perMana = price(SAPPHIRE_CRYSTAL) { it.mana },
            perArmor = price(CLOTH_ARMOR) { it.armor },
            perMagicResist = price(NULL_MAGIC_MANTLE) { it.magicResist },
            perAttackSpeedPercent = price(DAGGER) { it.attackSpeedPercent },
            perCritChancePercent = price(CLOAK_OF_AGILITY) { it.critChancePercent },
            perHealthRegen = price(REJUVENATION_BEAD) { it.healthRegen },
        )
    }

    fun calculate(item: Item, values: StatGoldValues): GoldEfficiency {
        if (item.stats.isEmpty) {
            // Riot did not publish machine-readable stats for this item. That
            // is not the same as the item having none, so no percentage is
            // claimed rather than reporting a misleading 0%.
            return GoldEfficiency(
                percent = null,
                statValueGold = 0.0,
                isPartial = false,
                hasUnpublishedStats = true,
            )
        }

        var total = 0.0
        var missingPrice = false

        fun add(amount: Double, perUnit: Double?) {
            if (amount == 0.0) return
            if (perUnit == null) {
                missingPrice = true
                return
            }
            total += amount * perUnit
        }

        with(item.stats) {
            add(attackDamage, values.perAttackDamage)
            add(abilityPower, values.perAbilityPower)
            add(health, values.perHealth)
            add(mana, values.perMana)
            add(armor, values.perArmor)
            add(magicResist, values.perMagicResist)
            add(attackSpeedPercent, values.perAttackSpeedPercent)
            add(critChancePercent, values.perCritChancePercent)
            add(healthRegen, values.perHealthRegen)
            // Movement speed and life steal are deliberately unpriced: no
            // single-stat basic item sells them at a stable rate, so any
            // number here would be invented.
        }

        val cost = item.gold.total
        return GoldEfficiency(
            percent = if (cost > 0) (total / cost) * 100.0 else null,
            statValueGold = total,
            isPartial = missingPrice,
            hasUnpublishedStats = false,
        )
    }

    private companion object {
        const val LONG_SWORD = "1036"
        const val AMPLIFYING_TOME = "1052"
        const val RUBY_CRYSTAL = "1028"
        const val SAPPHIRE_CRYSTAL = "1027"
        const val CLOTH_ARMOR = "1029"
        const val NULL_MAGIC_MANTLE = "1033"
        const val DAGGER = "1042"
        const val CLOAK_OF_AGILITY = "1018"
        const val REJUVENATION_BEAD = "1006"
    }
}
