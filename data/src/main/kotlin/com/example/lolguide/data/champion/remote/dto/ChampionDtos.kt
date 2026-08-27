package com.example.lolguide.data.champion.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Dragon wire types. These never leave `:data` (AGENTS.md §3).
 *
 * Field names match Riot's JSON exactly, which is why they are lowercase and
 * unpunctuated; the mapper is what turns them into readable domain models.
 */

@Serializable
data class ChampionListResponseDto(
    val version: String = "",
    val data: Map<String, ChampionDto> = emptyMap(),
)

@Serializable
data class ChampionDto(
    val id: String = "",
    val key: String = "",
    val name: String = "",
    val title: String = "",
    val blurb: String = "",
    val info: ChampionInfoDto = ChampionInfoDto(),
    val image: ImageDto = ImageDto(),
    val tags: List<String> = emptyList(),
    val partype: String = "",
    val stats: ChampionStatsDto = ChampionStatsDto(),
)

@Serializable
data class ChampionInfoDto(
    val attack: Int = 0,
    val defense: Int = 0,
    val magic: Int = 0,
    val difficulty: Int = 0,
)

@Serializable
data class ImageDto(
    val full: String = "",
    val sprite: String = "",
    val group: String = "",
)

@Serializable
data class ChampionStatsDto(
    val hp: Double = 0.0,
    val hpperlevel: Double = 0.0,
    val mp: Double = 0.0,
    val mpperlevel: Double = 0.0,
    val movespeed: Double = 0.0,
    val armor: Double = 0.0,
    val armorperlevel: Double = 0.0,
    val spellblock: Double = 0.0,
    val spellblockperlevel: Double = 0.0,
    val attackrange: Double = 0.0,
    val hpregen: Double = 0.0,
    val hpregenperlevel: Double = 0.0,
    val mpregen: Double = 0.0,
    val mpregenperlevel: Double = 0.0,
    val crit: Double = 0.0,
    val critperlevel: Double = 0.0,
    val attackdamage: Double = 0.0,
    val attackdamageperlevel: Double = 0.0,
    val attackspeed: Double = 0.0,
    val attackspeedperlevel: Double = 0.0,
)

@Serializable
data class ChampionDetailResponseDto(
    val data: Map<String, ChampionDetailDto> = emptyMap(),
)

@Serializable
data class ChampionDetailDto(
    val id: String = "",
    val lore: String = "",
    val passive: PassiveDto = PassiveDto(),
    val spells: List<SpellDto> = emptyList(),
)

@Serializable
data class PassiveDto(
    val name: String = "",
    val description: String = "",
    val image: ImageDto = ImageDto(),
)

@Serializable
data class SpellDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val image: ImageDto = ImageDto(),
    /**
     * The real per-rank arrays, not Riot's `cooldownBurn` / `costBurn`
     * strings. The burn fields are pre-flattened display text
     * ("14/12/10/8/6") and lose information -- some abilities render as
     * "60" there when the array actually differs per rank.
     */
    val cooldown: List<Double> = emptyList(),
    val cost: List<Double> = emptyList(),
    val costType: String = "",
    @SerialName("maxrank") val maxRank: Int = 0,
)
