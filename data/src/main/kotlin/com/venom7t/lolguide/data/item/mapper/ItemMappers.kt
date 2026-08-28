package com.venom7t.lolguide.data.item.mapper

import com.venom7t.lolguide.data.item.local.ItemEntity
import com.venom7t.lolguide.data.item.local.ItemGoldEmbedded
import com.venom7t.lolguide.data.item.local.ItemStatsEmbedded
import com.venom7t.lolguide.data.item.remote.dto.ItemDto
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.domain.item.model.ItemGold
import com.venom7t.lolguide.domain.item.model.ItemStats

/** Riot keys map availability by numeric map id; 11 is Summoner's Rift. */
private const val SUMMONERS_RIFT_MAP_ID = "11"

/**
 * Riot expresses attack speed, crit, life steal and percent movement speed as
 * fractions (0.15 meaning 15%). They are scaled to whole percentages once,
 * here, so nothing downstream has to remember which convention a field uses.
 */
private const val FRACTION_TO_PERCENT = 100.0

fun ItemDto.toEntity(itemId: String, patchVersion: String, locale: AppLocale) = ItemEntity(
    id = itemId,
    name = name,
    plaintext = plaintext,
    description = description,
    imageFileName = image.full,
    tags = tags,
    fromIds = from,
    intoIds = into,
    depth = depth,
    requiredChampionId = requiredChampion,
    // Absent from the maps block means unavailable, not available by default.
    availableOnSummonersRift = maps[SUMMONERS_RIFT_MAP_ID] == true,
    patchVersion = patchVersion,
    locale = locale.dataDragonCode,
    gold = ItemGoldEmbedded(
        base = gold.base,
        total = gold.total,
        sell = gold.sell,
        purchasable = gold.purchasable,
    ),
    stats = ItemStatsEmbedded(
        attackDamage = stats.flatPhysicalDamage,
        abilityPower = stats.flatMagicDamage,
        health = stats.flatHealth,
        mana = stats.flatMana,
        armor = stats.flatArmor,
        magicResist = stats.flatMagicResist,
        attackSpeedPercent = stats.percentAttackSpeed * FRACTION_TO_PERCENT,
        critChancePercent = stats.flatCritChance * FRACTION_TO_PERCENT,
        healthRegen = stats.flatHealthRegen,
        moveSpeedFlat = stats.flatMoveSpeed,
        moveSpeedPercent = stats.percentMoveSpeed * FRACTION_TO_PERCENT,
        lifeStealPercent = stats.percentLifeSteal * FRACTION_TO_PERCENT,
    ),
)

fun ItemEntity.toDomain() = Item(
    id = id,
    name = name,
    plaintext = plaintext,
    description = description,
    imageFileName = imageFileName,
    tags = tags,
    from = fromIds,
    into = intoIds,
    depth = depth,
    requiredChampionId = requiredChampionId,
    isPurchasable = gold.purchasable,
    availableOnSummonersRift = availableOnSummonersRift,
    patchVersion = patchVersion,
    gold = ItemGold(
        base = gold.base,
        total = gold.total,
        sell = gold.sell,
        purchasable = gold.purchasable,
    ),
    stats = ItemStats(
        attackDamage = stats.attackDamage,
        abilityPower = stats.abilityPower,
        health = stats.health,
        mana = stats.mana,
        armor = stats.armor,
        magicResist = stats.magicResist,
        attackSpeedPercent = stats.attackSpeedPercent,
        critChancePercent = stats.critChancePercent,
        healthRegen = stats.healthRegen,
        moveSpeedFlat = stats.moveSpeedFlat,
        moveSpeedPercent = stats.moveSpeedPercent,
        lifeStealPercent = stats.lifeStealPercent,
    ),
)
