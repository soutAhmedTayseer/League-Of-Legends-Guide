package com.venom7t.lolguide.domain.item.model

/**
 * A shop item.
 *
 * @param from ids of the components this builds out of.
 * @param into ids of the items this builds into. Together these two form the
 *   build-path graph the UI renders as a tree.
 * @param requiredChampionId set for champion-locked items (Viktor, Gangplank),
 *   which must not appear in a generic browse list as if anyone could buy them.
 */
data class Item(
    val id: String,
    val name: String,
    val plaintext: String,
    val description: String,
    val imageFileName: String,
    val gold: ItemGold,
    val tags: List<String>,
    val from: List<String>,
    val into: List<String>,
    val stats: ItemStats,
    val depth: Int,
    val requiredChampionId: String?,
    val isPurchasable: Boolean,
    val availableOnSummonersRift: Boolean,
    val patchVersion: String,
) {
    /** A component: buildable into something else, and not itself built from parts. */
    val isBasic: Boolean get() = from.isEmpty() && into.isNotEmpty()

    /** A finished item: nothing builds out of it. */
    val isFinished: Boolean get() = into.isEmpty() && from.isNotEmpty()
}

data class ItemGold(
    /** Cost of this item given you already own its components. */
    val base: Int,
    /** Cost from nothing, components included. */
    val total: Int,
    val sell: Int,
    val purchasable: Boolean,
)

/**
 * The stats Data Dragon publishes for an item.
 *
 * **Riot does not publish stats for every item.** Since the item overhaul,
 * many finished items carry their stats only in the description text, leaving
 * the machine-readable `stats` block empty. [isEmpty] exists so callers can
 * tell "this item genuinely has no stats" apart from "Riot did not publish
 * them", rather than silently treating an unpublished item as worthless
 * (AGENTS.md §1).
 */
data class ItemStats(
    val attackDamage: Double = 0.0,
    val abilityPower: Double = 0.0,
    val health: Double = 0.0,
    val mana: Double = 0.0,
    val armor: Double = 0.0,
    val magicResist: Double = 0.0,
    val attackSpeedPercent: Double = 0.0,
    val critChancePercent: Double = 0.0,
    val healthRegen: Double = 0.0,
    val moveSpeedFlat: Double = 0.0,
    val moveSpeedPercent: Double = 0.0,
    val lifeStealPercent: Double = 0.0,
) {
    val isEmpty: Boolean
        get() = attackDamage == 0.0 && abilityPower == 0.0 && health == 0.0 &&
            mana == 0.0 && armor == 0.0 && magicResist == 0.0 &&
            attackSpeedPercent == 0.0 && critChancePercent == 0.0 &&
            healthRegen == 0.0 && moveSpeedFlat == 0.0 &&
            moveSpeedPercent == 0.0 && lifeStealPercent == 0.0

    operator fun plus(other: ItemStats) = ItemStats(
        attackDamage = attackDamage + other.attackDamage,
        abilityPower = abilityPower + other.abilityPower,
        health = health + other.health,
        mana = mana + other.mana,
        armor = armor + other.armor,
        magicResist = magicResist + other.magicResist,
        attackSpeedPercent = attackSpeedPercent + other.attackSpeedPercent,
        critChancePercent = critChancePercent + other.critChancePercent,
        healthRegen = healthRegen + other.healthRegen,
        moveSpeedFlat = moveSpeedFlat + other.moveSpeedFlat,
        moveSpeedPercent = moveSpeedPercent + other.moveSpeedPercent,
        lifeStealPercent = lifeStealPercent + other.lifeStealPercent,
    )
}
