package com.example.lolguide

import androidx.room.Entity
import androidx.room.PrimaryKey

// --- EXISTING MODELS (Keep these exactly as they are) ---
data class ChampionResponse(val data: Map<String, Champion>)

@Entity(tableName = "champions")
data class Champion(
    @PrimaryKey val id: String,
    val key: String?,
    val version: String?,
    val name: String,
    val title: String,
    val blurb: String,
    val tags: List<String>?,
    val partype: String?,
    val image: ImageInfo,
    val info: Info?,
    val stats: Stats?
)
data class ImageInfo(val full: String)
data class Info(val attack: Int, val defense: Int, val magic: Int, val difficulty: Int)
data class Stats(
    val hp: Double, val hpperlevel: Double, val mp: Double, val mpperlevel: Double,
    val movespeed: Double, val armor: Double, val armorperlevel: Double,
    val spellblock: Double, val spellblockperlevel: Double, val attackrange: Double,
    val hpregen: Double, val hpregenperlevel: Double, val mpregen: Double,
    val mpregenperlevel: Double, val crit: Double, val critperlevel: Double,
    val attackdamage: Double, val attackdamageperlevel: Double,
    val attackspeedperlevel: Double, val attackspeed: Double
)

// --- NEW MODELS FOR FEATURE 1 (ABILITIES) ---

data class ChampionDetailResponse(
    val data: Map<String, ChampionDetail>
)

data class ChampionDetail(
    val spells: List<Spell>,
    val passive: Passive
)

data class Spell(
    val id: String,
    val name: String,
    val description: String,
    val cooldownBurn: String, // String format (e.g., "14/12/10/8/6")
    val costBurn: String,     // String format (e.g., "50/60/70/80/90")
    val image: ImageInfo
)

data class Passive(
    val name: String,
    val description: String,
    val image: ImageInfo
)