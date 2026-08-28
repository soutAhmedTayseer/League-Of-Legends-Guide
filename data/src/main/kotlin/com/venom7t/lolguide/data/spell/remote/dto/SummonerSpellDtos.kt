package com.venom7t.lolguide.data.spell.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SummonerSpellListResponseDto(
    val data: Map<String, SummonerSpellDto> = emptyMap(),
)

@Serializable
data class SummonerSpellDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val key: String = "",
    val summonerLevel: Int = 1,
    /** Per-rank array; summoner spells have exactly one rank. */
    val cooldown: List<Double> = emptyList(),
    val modes: List<String> = emptyList(),
    val image: SpellImageDto = SpellImageDto(),
)

@Serializable
data class SpellImageDto(val full: String = "")
