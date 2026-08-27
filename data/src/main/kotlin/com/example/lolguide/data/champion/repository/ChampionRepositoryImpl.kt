package com.example.lolguide.data.champion.repository

import com.example.lolguide.data.champion.local.ChampionDao
import com.example.lolguide.data.champion.mapper.toDomain
import com.example.lolguide.data.champion.mapper.toEntity
import com.example.lolguide.data.champion.remote.DataDragonApi
import com.example.lolguide.data.common.di.IoDispatcher
import com.example.lolguide.data.common.toAppError
import com.example.lolguide.domain.champion.model.Champion
import com.example.lolguide.domain.champion.model.ChampionDetail
import com.example.lolguide.domain.champion.repository.ChampionRepository
import com.example.lolguide.domain.common.AppError
import com.example.lolguide.domain.common.AppLocale
import com.example.lolguide.domain.common.runCatchingCancellable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first champion repository (AGENTS.md §7.1).
 *
 * Reads always come from Room. The network only ever writes to Room; it never
 * feeds the UI directly. That is what makes the list render instantly on a
 * cold start with no connection.
 */
@Singleton
class ChampionRepositoryImpl @Inject constructor(
    private val api: DataDragonApi,
    private val dao: ChampionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ChampionRepository {

    override fun observeChampions(): Flow<List<Champion>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getCachedChampion(championId: String): Champion? =
        withContext(ioDispatcher) {
            dao.getById(championId)?.toDomain()
        }

    override suspend fun refreshChampions(
        version: String,
        locale: AppLocale,
    ): Result<Unit> = withContext(ioDispatcher) {
        runCatchingCancellable {
            val response = api.getChampions(version = version, locale = locale.dataDragonCode)

            // An empty payload is a failure, not an empty cache. Wiping the
            // table on a malformed response would turn a bad request into a
            // blank app with no way back offline (AGENTS.md §7.2).
            if (response.data.isEmpty()) {
                throw AppError.Serialization("champion.json contained no champions")
            }

            val entities = response.data.values.map { it.toEntity(version, locale) }
            dao.replaceAll(entities)
            Timber.d("Cached %d champions for patch %s", entities.size, version)
        }.recoverCatching { throwable ->
            throw throwable.toAppError()
        }
    }

    override suspend fun getChampionDetail(
        championId: String,
        version: String,
        locale: AppLocale,
    ): Result<ChampionDetail> = withContext(ioDispatcher) {
        runCatchingCancellable {
            val response = api.getChampionDetail(
                version = version,
                locale = locale.dataDragonCode,
                championId = championId,
            )

            // The response is a single-entry map keyed by champion id. Take it
            // by key rather than by position so a future multi-entry payload
            // cannot silently return the wrong champion.
            val detail = response.data[championId]
                ?: response.data.values.singleOrNull()
                ?: throw AppError.NotFound(championId)

            detail.toDomain(championId = championId, patchVersion = version)
        }.recoverCatching { throwable ->
            throw throwable.toAppError()
        }
    }
}
