package com.venom7t.lolguide.data.item.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemListResponseDto(
    val data: Map<String, ItemDto> = emptyMap(),
)

@Serializable
data class ItemDto(
    val name: String = "",
    val description: String = "",
    val plaintext: String = "",
    val image: ItemImageDto = ItemImageDto(),
    val gold: ItemGoldDto = ItemGoldDto(),
    val tags: List<String> = emptyList(),
    val from: List<String> = emptyList(),
    val into: List<String> = emptyList(),
    val stats: ItemStatsDto = ItemStatsDto(),
    val depth: Int = 1,
    /** Set only for champion-locked items such as Viktor's or Gangplank's. */
    val requiredChampion: String? = null,
    /**
     * Map availability keyed by map id as a string. "11" is Summoner's Rift.
     * Riot ships Arena and ARAM exclusives in the same payload, so a Rift
     * reference has to filter on this.
     */
    val maps: Map<String, Boolean> = emptyMap(),
)

@Serializable
data class ItemImageDto(val full: String = "")

@Serializable
data class ItemGoldDto(
    val base: Int = 0,
    val total: Int = 0,
    val sell: Int = 0,
    val purchasable: Boolean = false,
)

/**
 * Riot's stat keys, verbatim.
 *
 * Many modern items ship an empty object here because their stats live only in
 * the description text. That is why the domain distinguishes "no stats
 * published" from "worth nothing".
 */
@Serializable
data class ItemStatsDto(
    @SerialName("FlatPhysicalDamageMod") val flatPhysicalDamage: Double = 0.0,
    @SerialName("FlatMagicDamageMod") val flatMagicDamage: Double = 0.0,
    @SerialName("FlatHPPoolMod") val flatHealth: Double = 0.0,
    @SerialName("FlatMPPoolMod") val flatMana: Double = 0.0,
    @SerialName("FlatArmorMod") val flatArmor: Double = 0.0,
    @SerialName("FlatSpellBlockMod") val flatMagicResist: Double = 0.0,
    @SerialName("PercentAttackSpeedMod") val percentAttackSpeed: Double = 0.0,
    @SerialName("FlatCritChanceMod") val flatCritChance: Double = 0.0,
    @SerialName("FlatHPRegenMod") val flatHealthRegen: Double = 0.0,
    @SerialName("FlatMovementSpeedMod") val flatMoveSpeed: Double = 0.0,
    @SerialName("PercentMovementSpeedMod") val percentMoveSpeed: Double = 0.0,
    @SerialName("PercentLifeStealMod") val percentLifeSteal: Double = 0.0,
)
