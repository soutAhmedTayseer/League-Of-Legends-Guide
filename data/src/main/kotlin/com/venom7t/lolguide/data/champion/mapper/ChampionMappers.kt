package com.venom7t.lolguide.data.champion.mapper

import com.venom7t.lolguide.data.champion.local.ChampionEntity
import com.venom7t.lolguide.data.champion.local.ChampionInfoEmbedded
import com.venom7t.lolguide.data.champion.local.ChampionStatsEmbedded
import com.venom7t.lolguide.data.champion.remote.dto.ChampionDetailDto
import com.venom7t.lolguide.data.champion.remote.dto.ChampionDto
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionDetail
import com.venom7t.lolguide.domain.champion.model.ChampionInfo
import com.venom7t.lolguide.domain.champion.model.ChampionStats
import com.venom7t.lolguide.domain.champion.model.ChampionTag
import com.venom7t.lolguide.domain.champion.model.Passive
import com.venom7t.lolguide.domain.champion.model.Skin
import com.venom7t.lolguide.domain.champion.model.Spell
import com.venom7t.lolguide.domain.champion.model.SpellSlot
import com.venom7t.lolguide.domain.common.AppLocale

/**
 * DTO/Entity to domain conversions. Mappers live in `:data` and are the only
 * place wire shapes are allowed to be known (AGENTS.md §3).
 */

fun ChampionDto.toEntity(patchVersion: String, locale: AppLocale) = ChampionEntity(
    id = id,
    championKey = key,
    name = name,
    title = title,
    blurb = blurb,
    tags = tags,
    partype = partype,
    imageFileName = image.full,
    patchVersion = patchVersion,
    locale = locale.dataDragonCode,
    info = ChampionInfoEmbedded(
        attack = info.attack,
        defense = info.defense,
        magic = info.magic,
        difficulty = info.difficulty,
    ),
    stats = ChampionStatsEmbedded(
        hp = stats.hp,
        hpPerLevel = stats.hpperlevel,
        mp = stats.mp,
        mpPerLevel = stats.mpperlevel,
        moveSpeed = stats.movespeed,
        armor = stats.armor,
        armorPerLevel = stats.armorperlevel,
        spellBlock = stats.spellblock,
        spellBlockPerLevel = stats.spellblockperlevel,
        attackRange = stats.attackrange,
        hpRegen = stats.hpregen,
        hpRegenPerLevel = stats.hpregenperlevel,
        mpRegen = stats.mpregen,
        mpRegenPerLevel = stats.mpregenperlevel,
        crit = stats.crit,
        critPerLevel = stats.critperlevel,
        attackDamage = stats.attackdamage,
        attackDamagePerLevel = stats.attackdamageperlevel,
        attackSpeed = stats.attackspeed,
        attackSpeedPerLevel = stats.attackspeedperlevel,
    ),
)

fun ChampionEntity.toDomain() = Champion(
    id = id,
    key = championKey,
    name = name,
    title = title,
    blurb = blurb,
    tags = tags.map(ChampionTag::from),
    partype = partype,
    imageFileName = imageFileName,
    patchVersion = patchVersion,
    info = ChampionInfo(
        attack = info.attack,
        defense = info.defense,
        magic = info.magic,
        difficulty = info.difficulty,
    ),
    stats = ChampionStats(
        hp = stats.hp,
        hpPerLevel = stats.hpPerLevel,
        mp = stats.mp,
        mpPerLevel = stats.mpPerLevel,
        moveSpeed = stats.moveSpeed,
        armor = stats.armor,
        armorPerLevel = stats.armorPerLevel,
        spellBlock = stats.spellBlock,
        spellBlockPerLevel = stats.spellBlockPerLevel,
        attackRange = stats.attackRange,
        hpRegen = stats.hpRegen,
        hpRegenPerLevel = stats.hpRegenPerLevel,
        mpRegen = stats.mpRegen,
        mpRegenPerLevel = stats.mpRegenPerLevel,
        crit = stats.crit,
        critPerLevel = stats.critPerLevel,
        attackDamage = stats.attackDamage,
        attackDamagePerLevel = stats.attackDamagePerLevel,
        attackSpeed = stats.attackSpeed,
        attackSpeedPerLevel = stats.attackSpeedPerLevel,
    ),
)

fun ChampionDetailDto.toDomain(championId: String, patchVersion: String) = ChampionDetail(
    championId = championId.ifEmpty { id },
    lore = lore,
    patchVersion = patchVersion,
    passive = Passive(
        name = passive.name,
        description = passive.description,
        imageFileName = passive.image.full,
    ),
    skins = skins.map { skin ->
        Skin(
            id = skin.id,
            num = skin.num,
            name = skin.name,
            hasChromas = skin.chromas,
        )
    },
    // Data Dragon orders `spells` Q, W, E, R. Anything beyond the fourth entry
    // is not a keybound ability and is dropped rather than mislabelled.
    spells = spells.mapIndexedNotNull { index, dto ->
        val slot = SpellSlot.fromIndex(index) ?: return@mapIndexedNotNull null
        Spell(
            id = dto.id,
            slot = slot,
            name = dto.name,
            description = dto.description,
            imageFileName = dto.image.full,
            cooldownPerRank = dto.cooldown,
            costPerRank = dto.cost,
            costType = dto.costType,
            // Riot occasionally ships maxrank 0 for placeholder abilities.
            // Fall back to the actual array length instead of showing a table
            // with no rows.
            maxRank = if (dto.maxRank > 0) dto.maxRank else dto.cooldown.size,
        )
    },
)
