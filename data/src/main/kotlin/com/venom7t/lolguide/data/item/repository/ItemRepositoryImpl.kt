package com.venom7t.lolguide.data.item.repository

import com.venom7t.lolguide.data.champion.remote.DataDragonApi
import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.common.toAppError
import com.venom7t.lolguide.data.item.local.ItemDao
import com.venom7t.lolguide.data.item.mapper.toDomain
import com.venom7t.lolguide.data.item.mapper.toEntity
import com.venom7t.lolguide.domain.common.AppError
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.domain.item.repository.ItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val api: DataDragonApi,
    private val dao: ItemDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ItemRepository {

    override fun observeItems(): Flow<List<Item>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getCachedItem(itemId: String): Item? = withContext(ioDispatcher) {
        dao.getById(itemId)?.toDomain()
    }

    override suspend fun getCachedItems(itemIds: List<String>): List<Item> =
        withContext(ioDispatcher) {
            if (itemIds.isEmpty()) return@withContext emptyList()
            val found = dao.getByIds(itemIds).associateBy { it.id }
            // Preserve the caller's order: a build path reads left to right,
            // and rows from an IN query come back in whatever order SQLite
            // chose, which is not the order the ids were listed in.
            itemIds.mapNotNull { found[it]?.toDomain() }
        }

    override suspend fun refreshItems(
        version: String,
        locale: AppLocale,
    ): Result<Unit> = withContext(ioDispatcher) {
        runCatchingCancellable {
            val response = api.getItems(version = version, locale = locale.dataDragonCode)

            // An empty payload is a failure, not an empty shop. Wiping the
            // table on a malformed response would break offline browsing.
            if (response.data.isEmpty()) {
                throw AppError.Serialization("item.json contained no items")
            }

            val entities = response.data.map { (itemId, dto) ->
                dto.toEntity(itemId = itemId, patchVersion = version, locale = locale)
            }
            dao.replaceAll(entities)
            Timber.d("Cached %d items for patch %s", entities.size, version)
        }.recoverCatching { throwable -> throw throwable.toAppError() }
    }
}
