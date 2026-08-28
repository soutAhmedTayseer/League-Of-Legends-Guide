package com.venom7t.lolguide.data.rune.remote.dto

import kotlinx.serialization.Serializable

/** `runesReforged.json` is a bare JSON array of trees, not an object. */
@Serializable
data class RuneTreeDto(
    val id: Int = 0,
    val key: String = "",
    val icon: String = "",
    val name: String = "",
    val slots: List<RuneSlotDto> = emptyList(),
)

@Serializable
data class RuneSlotDto(
    val runes: List<RuneDto> = emptyList(),
)

@Serializable
data class RuneDto(
    val id: Int = 0,
    val key: String = "",
    val icon: String = "",
    val name: String = "",
    val shortDesc: String = "",
    val longDesc: String = "",
)
