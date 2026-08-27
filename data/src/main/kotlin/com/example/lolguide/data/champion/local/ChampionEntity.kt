package com.example.lolguide.data.champion.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The cached champion row.
 *
 * [patchVersion] is stored per row rather than as a single global setting so
 * that a partially-completed refresh can never leave rows from two patches
 * looking equally current (AGENTS.md §1).
 */
@Entity(tableName = "champions")
data class ChampionEntity(
    @PrimaryKey val id: String,
    val championKey: String,
    val name: String,
    val title: String,
    val blurb: String,
    val tags: List<String>,
    val partype: String,
    val imageFileName: String,
    val patchVersion: String,
    val locale: String,
    @Embedded(prefix = "info_") val info: ChampionInfoEmbedded,
    @Embedded(prefix = "stats_") val stats: ChampionStatsEmbedded,
)

data class ChampionInfoEmbedded(
    val attack: Int,
    val defense: Int,
    val magic: Int,
    val difficulty: Int,
)

data class ChampionStatsEmbedded(
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
