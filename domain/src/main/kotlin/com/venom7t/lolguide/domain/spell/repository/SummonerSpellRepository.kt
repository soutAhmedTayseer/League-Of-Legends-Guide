package com.venom7t.lolguide.domain.spell.repository

import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.spell.model.SummonerSpell

/** Like runes: one small payload, fetched on demand. */
interface SummonerSpellRepository {
    suspend fun getSummonerSpells(version: String, locale: AppLocale): Result<List<SummonerSpell>>
}
