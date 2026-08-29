package com.venom7t.lolguide.presentation.common

import com.venom7t.lolguide.domain.champion.model.ChampionTag
import com.venom7t.lolguide.domain.champion.model.DamageType
import com.venom7t.lolguide.domain.champion.model.Difficulty
import com.venom7t.lolguide.presentation.R

/**
 * Domain enums to translatable labels.
 *
 * Kept here rather than on the enums themselves: `:domain` is pure Kotlin and
 * has no access to Android resources (AGENTS.md §3), and a `displayName`
 * baked into the model would be untranslatable.
 */

fun ChampionTag.labelUiText(): UiText = when (this) {
    ChampionTag.Assassin -> uiText(R.string.role_assassin)
    ChampionTag.Fighter -> uiText(R.string.role_fighter)
    ChampionTag.Mage -> uiText(R.string.role_mage)
    ChampionTag.Marksman -> uiText(R.string.role_marksman)
    ChampionTag.Support -> uiText(R.string.role_support)
    ChampionTag.Tank -> uiText(R.string.role_tank)
    // A tag Riot added after this build shipped. Showing its raw value is
    // better than hiding the champion's role entirely.
    is ChampionTag.Unknown -> raw.asUiText()
}

fun Difficulty.labelUiText(): UiText = when (this) {
    Difficulty.LOW -> uiText(R.string.difficulty_low)
    Difficulty.MEDIUM -> uiText(R.string.difficulty_medium)
    Difficulty.HIGH -> uiText(R.string.difficulty_high)
}

fun DamageType.labelUiText(): UiText = when (this) {
    DamageType.PHYSICAL -> uiText(R.string.damage_physical)
    DamageType.MAGIC -> uiText(R.string.damage_magic)
    DamageType.HYBRID -> uiText(R.string.damage_hybrid)
}

/**
 * Resource names ("Mana", "Energy", "Rage") come straight from Data Dragon,
 * which already localises them for the requested locale. Wrapping them as
 * [UiText.Raw] rather than mapping to string resources avoids maintaining a
 * parallel translation of text Riot already translated.
 */
fun resourceLabelUiText(partype: String): UiText = partype.asUiText()

/** The base skin is named "default" in the payload; show the champion instead. */
fun skinDisplayName(skinName: String, championName: String): String =
    if (skinName.equals("default", ignoreCase = true)) championName else skinName
