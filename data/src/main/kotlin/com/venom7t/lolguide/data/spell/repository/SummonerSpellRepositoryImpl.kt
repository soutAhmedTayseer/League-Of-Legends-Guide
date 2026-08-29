package com.venom7t.lolguide.data.spell.repository

import com.venom7t.lolguide.data.champion.remote.DataDragonApi
import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.common.toAppError
import com.venom7t.lolguide.domain.common.AppError
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.spell.model.SummonerSpell
import com.venom7t.lolguide.domain.spell.repository.SummonerSpellRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SummonerSpellRepositoryImpl constructor(
    private val api: DataDragonApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SummonerSpellRepository {

    override suspend fun getSummonerSpells(
        version: String,
        locale: AppLocale,
    ): Result<List<SummonerSpell>> = withContext(ioDispatcher) {
        runCatchingCancellable {
            val response = api.getSummonerSpells(
                version = version,
                locale = locale.dataDragonCode,
            )
            if (response.data.isEmpty()) {
                throw AppError.Serialization("summoner.json contained no spells")
            }

            response.data.values.map { dto ->
                SummonerSpell(
                    id = dto.id,
                    key = dto.key,
                    name = dto.name,
                    description = dto.description,
                    imageFileName = dto.image.full,
                    requiredSummonerLevel = dto.summonerLevel,
                    // Summoner spells have a single rank, so the cooldown array
                    // holds one entry. Default to 0 rather than crash if Riot
                    // ever ships an empty one.
                    cooldownSeconds = dto.cooldown.firstOrNull() ?: 0.0,
                    modes = dto.modes,
                    patchVersion = version,
                )
            }.sortedBy { it.name }
        }.recoverCatching { throwable -> throw throwable.toAppError() }
    }
}
