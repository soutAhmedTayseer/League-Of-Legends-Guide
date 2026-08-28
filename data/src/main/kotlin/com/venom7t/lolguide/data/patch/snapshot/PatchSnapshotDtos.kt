package com.venom7t.lolguide.data.patch.snapshot

import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionInfo
import com.venom7t.lolguide.domain.champion.model.ChampionStats
import com.venom7t.lolguide.domain.champion.model.ChampionTag
import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.domain.item.model.ItemGold
import com.venom7t.lolguide.domain.item.model.ItemStats
import kotlinx.serialization.Serializable

/**
 * Serializable mirrors of the domain models, used only to persist a snapshot
 * of the previous patch's cache as JSON (see [PreviousPatchSnapshotRepositoryImpl]).
 *
 * Domain models are not `@Serializable` themselves -- `:domain` has no
 * knowledge of serialization frameworks, per `AGENTS.md` §3 -- so this is the
 * `:data`-side conversion layer, kept separate from the DTOs used for the
 * live Data Dragon responses because this shape is our own, not Riot's.
 */

@Serializable
data class ChampionSnapshotDto(
    val id: String,
    val key: String,
    val name: String,
    val title: String,
    val blurb: String,
    val tags: List<String>,
    val partype: String,
    val imageFileName: String,
    val patchVersion: String,
    val attack: Int,
    val defense: Int,
    val magic: Int,
    val difficulty: Int,
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

fun Champion.toSnapshotDto() = ChampionSnapshotDto(
    id = id, key = key, name = name, title = title, blurb = blurb,
    tags = tags.map { it.raw }, partype = partype, imageFileName = imageFileName,
    patchVersion = patchVersion,
    attack = info.attack, defense = info.defense, magic = info.magic, difficulty = info.difficulty,
    hp = stats.hp, hpPerLevel = stats.hpPerLevel, mp = stats.mp, mpPerLevel = stats.mpPerLevel,
    moveSpeed = stats.moveSpeed, armor = stats.armor, armorPerLevel = stats.armorPerLevel,
    spellBlock = stats.spellBlock, spellBlockPerLevel = stats.spellBlockPerLevel,
    attackRange = stats.attackRange, hpRegen = stats.hpRegen, hpRegenPerLevel = stats.hpRegenPerLevel,
    mpRegen = stats.mpRegen, mpRegenPerLevel = stats.mpRegenPerLevel,
    crit = stats.crit, critPerLevel = stats.critPerLevel,
    attackDamage = stats.attackDamage, attackDamagePerLevel = stats.attackDamagePerLevel,
    attackSpeed = stats.attackSpeed, attackSpeedPerLevel = stats.attackSpeedPerLevel,
)

fun ChampionSnapshotDto.toDomain() = Champion(
    id = id, key = key, name = name, title = title, blurb = blurb,
    tags = tags.map(ChampionTag::from), partype = partype, imageFileName = imageFileName,
    patchVersion = patchVersion,
    info = ChampionInfo(attack = attack, defense = defense, magic = magic, difficulty = difficulty),
    stats = ChampionStats(
        hp = hp, hpPerLevel = hpPerLevel, mp = mp, mpPerLevel = mpPerLevel,
        moveSpeed = moveSpeed, armor = armor, armorPerLevel = armorPerLevel,
        spellBlock = spellBlock, spellBlockPerLevel = spellBlockPerLevel,
        attackRange = attackRange, hpRegen = hpRegen, hpRegenPerLevel = hpRegenPerLevel,
        mpRegen = mpRegen, mpRegenPerLevel = mpRegenPerLevel,
        crit = crit, critPerLevel = critPerLevel,
        attackDamage = attackDamage, attackDamagePerLevel = attackDamagePerLevel,
        attackSpeed = attackSpeed, attackSpeedPerLevel = attackSpeedPerLevel,
    ),
)

@Serializable
data class ItemSnapshotDto(
    val id: String,
    val name: String,
    val plaintext: String,
    val description: String,
    val imageFileName: String,
    val goldBase: Int,
    val goldTotal: Int,
    val goldSell: Int,
    val goldPurchasable: Boolean,
    val tags: List<String>,
    val from: List<String>,
    val into: List<String>,
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
    val depth: Int,
    val requiredChampionId: String?,
    val isPurchasable: Boolean,
    val availableOnSummonersRift: Boolean,
    val patchVersion: String,
)

fun Item.toSnapshotDto() = ItemSnapshotDto(
    id = id, name = name, plaintext = plaintext, description = description,
    imageFileName = imageFileName,
    goldBase = gold.base, goldTotal = gold.total, goldSell = gold.sell,
    goldPurchasable = gold.purchasable,
    tags = tags, from = from, into = into,
    attackDamage = stats.attackDamage, abilityPower = stats.abilityPower, health = stats.health,
    mana = stats.mana, armor = stats.armor, magicResist = stats.magicResist,
    attackSpeedPercent = stats.attackSpeedPercent, critChancePercent = stats.critChancePercent,
    healthRegen = stats.healthRegen, moveSpeedFlat = stats.moveSpeedFlat,
    moveSpeedPercent = stats.moveSpeedPercent, lifeStealPercent = stats.lifeStealPercent,
    depth = depth, requiredChampionId = requiredChampionId, isPurchasable = isPurchasable,
    availableOnSummonersRift = availableOnSummonersRift, patchVersion = patchVersion,
)

fun ItemSnapshotDto.toDomain() = Item(
    id = id, name = name, plaintext = plaintext, description = description,
    imageFileName = imageFileName,
    gold = ItemGold(base = goldBase, total = goldTotal, sell = goldSell, purchasable = goldPurchasable),
    tags = tags, from = from, into = into,
    stats = ItemStats(
        attackDamage = attackDamage, abilityPower = abilityPower, health = health, mana = mana,
        armor = armor, magicResist = magicResist, attackSpeedPercent = attackSpeedPercent,
        critChancePercent = critChancePercent, healthRegen = healthRegen,
        moveSpeedFlat = moveSpeedFlat, moveSpeedPercent = moveSpeedPercent,
        lifeStealPercent = lifeStealPercent,
    ),
    depth = depth, requiredChampionId = requiredChampionId, isPurchasable = isPurchasable,
    availableOnSummonersRift = availableOnSummonersRift, patchVersion = patchVersion,
)
