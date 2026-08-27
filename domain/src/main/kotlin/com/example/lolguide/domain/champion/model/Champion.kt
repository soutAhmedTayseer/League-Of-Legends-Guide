package com.example.lolguide.domain.champion.model

/**
 * A champion as shown in the list and the header of the detail screen.
 *
 * [patchVersion] is carried on the model itself rather than being looked up
 * where it is displayed. That is deliberate: it makes it impossible to render
 * a champion's numbers next to a patch label that came from somewhere else
 * (AGENTS.md §1).
 */
data class Champion(
    val id: String,
    val key: String,
    val name: String,
    val title: String,
    val blurb: String,
    val tags: List<ChampionTag>,
    val partype: String,
    val imageFileName: String,
    val info: ChampionInfo,
    val stats: ChampionStats,
    val patchVersion: String,
)

/**
 * Riot's own role classification. Unrecognised values are kept as [Unknown]
 * with their raw text rather than dropped, so a new tag in a future patch
 * degrades to "shown but uncategorised" instead of vanishing.
 */
sealed interface ChampionTag {
    val raw: String

    data object Assassin : ChampionTag { override val raw = "Assassin" }
    data object Fighter : ChampionTag { override val raw = "Fighter" }
    data object Mage : ChampionTag { override val raw = "Mage" }
    data object Marksman : ChampionTag { override val raw = "Marksman" }
    data object Support : ChampionTag { override val raw = "Support" }
    data object Tank : ChampionTag { override val raw = "Tank" }
    data class Unknown(override val raw: String) : ChampionTag

    companion object {
        fun from(raw: String): ChampionTag = when (raw) {
            Assassin.raw -> Assassin
            Fighter.raw -> Fighter
            Mage.raw -> Mage
            Marksman.raw -> Marksman
            Support.raw -> Support
            Tank.raw -> Tank
            else -> Unknown(raw)
        }
    }
}

/** Riot's 0-10 "difficulty bars" shown on the champion select screen. */
data class ChampionInfo(
    val attack: Int,
    val defense: Int,
    val magic: Int,
    val difficulty: Int,
)

/**
 * Base stats at level 1 plus their per-level growth.
 *
 * Nothing here is ever computed or filled in with a plausible default; every
 * field comes straight from Data Dragon (AGENTS.md §1).
 */
data class ChampionStats(
    val hp: Double,
    val hpPerLevel: Double,
    val mp: Double,
    val mpPerLevel: Double,
    val moveSpeed: Double,
    val armor: Double,
    val armorPerLevel: Double,
    val spellBlock: Double,
    val spellBlockPerLevel: Double,
    val attackRange: Double,
    val hpRegen: Double,
    val hpRegenPerLevel: Double,
    val mpRegen: Double,
    val mpRegenPerLevel: Double,
    val crit: Double,
    val critPerLevel: Double,
    val attackDamage: Double,
    val attackDamagePerLevel: Double,
    val attackSpeed: Double,
    val attackSpeedPerLevel: Double,
)
