package com.venom7t.lolguide.domain.spell.model

/**
 * A summoner spell.
 *
 * @param modes the game modes the spell is available in. Data Dragon returns
 *   every spell in one payload, including Arena- and ARAM-only ones, so a
 *   Summoner's Rift reference must filter on this rather than show all of them.
 * @param cooldownSeconds the base cooldown, before any haste.
 */
data class SummonerSpell(
    val id: String,
    val key: String,
    val name: String,
    val description: String,
    val imageFileName: String,
    val requiredSummonerLevel: Int,
    val cooldownSeconds: Double,
    val modes: List<String>,
    val patchVersion: String,
) {
    val isSummonersRift: Boolean get() = modes.contains(CLASSIC_MODE)

    companion object {
        const val CLASSIC_MODE = "CLASSIC"

        /**
         * Summoner Spell Haste reduces cooldowns multiplicatively, the same way
         * ability haste does: 100 haste halves the cooldown rather than
         * removing 100% of it.
         */
        fun cooldownWithHaste(baseSeconds: Double, haste: Double): Double =
            baseSeconds * (100.0 / (100.0 + haste.coerceAtLeast(0.0)))
    }
}
