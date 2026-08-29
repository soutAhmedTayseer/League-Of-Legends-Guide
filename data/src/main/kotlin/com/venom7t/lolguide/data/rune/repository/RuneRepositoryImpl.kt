package com.venom7t.lolguide.data.rune.repository

import com.venom7t.lolguide.data.champion.remote.DataDragonApi
import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.common.toAppError
import com.venom7t.lolguide.domain.common.AppError
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.rune.model.Rune
import com.venom7t.lolguide.domain.rune.model.RuneSlot
import com.venom7t.lolguide.domain.rune.model.RuneTree
import com.venom7t.lolguide.domain.rune.repository.RuneRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RuneRepositoryImpl constructor(
    private val api: DataDragonApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : RuneRepository {

    override suspend fun getRuneTrees(
        version: String,
        locale: AppLocale,
    ): Result<List<RuneTree>> = withContext(ioDispatcher) {
        runCatchingCancellable {
            val trees = api.getRuneTrees(version = version, locale = locale.dataDragonCode)
            if (trees.isEmpty()) {
                throw AppError.Serialization("runesReforged.json contained no trees")
            }

            trees.map { dto ->
                RuneTree(
                    id = dto.id,
                    key = dto.key,
                    name = dto.name,
                    iconPath = dto.icon,
                    slots = dto.slots.map { slot ->
                        RuneSlot(
                            runes = slot.runes.map { rune ->
                                Rune(
                                    id = rune.id,
                                    key = rune.key,
                                    name = rune.name,
                                    iconPath = rune.icon,
                                    shortDescription = rune.shortDesc,
                                    longDescription = rune.longDesc,
                                )
                            }
                        )
                    },
                )
            }
        }.recoverCatching { throwable -> throw throwable.toAppError() }
    }
}
