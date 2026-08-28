package com.venom7t.lolguide.data.item.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val plaintext: String,
    val description: String,
    val imageFileName: String,
    val tags: List<String>,
    val fromIds: List<String>,
    val intoIds: List<String>,
    val depth: Int,
    val requiredChampionId: String?,
    val availableOnSummonersRift: Boolean,
    val patchVersion: String,
    val locale: String,
    @Embedded(prefix = "gold_") val gold: ItemGoldEmbedded,
    @Embedded(prefix = "stat_") val stats: ItemStatsEmbedded,
)

data class ItemGoldEmbedded(
    val base: Int,
    val total: Int,
    val sell: Int,
    val purchasable: Boolean,
)

data class ItemStatsEmbedded(
    val attackDamage: Double,
    val abilityPower: Double,
    val health: Double,
    val mana: Double,
    val armor: Double,
    val magicResist: Double,
    val attackSpeedPercent: Double,
    val critChancePercent: Double,
    val healthRegen: Double,
    val moveSpeedFlat: Double,
    val moveSpeedPercent: Double,
    val lifeStealPercent: Double,
)
