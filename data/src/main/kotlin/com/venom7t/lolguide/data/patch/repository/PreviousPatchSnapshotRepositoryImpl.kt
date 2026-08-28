package com.venom7t.lolguide.data.patch.repository

import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.patch.local.PreviousPatchSnapshotDao
import com.venom7t.lolguide.data.patch.local.PreviousPatchSnapshotEntity
import com.venom7t.lolguide.data.patch.snapshot.ChampionSnapshotDto
import com.venom7t.lolguide.data.patch.snapshot.ItemSnapshotDto
import com.venom7t.lolguide.data.patch.snapshot.toDomain
import com.venom7t.lolguide.data.patch.snapshot.toSnapshotDto
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.domain.patch.repository.PreviousPatchSnapshotRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores exactly one prior generation of the champion and item caches, so the
 * patch diff engine has something to compare the live cache against.
 *
 * Reading and writing both go through JSON rather than a fully-typed Room
 * table per kind: this data is read exactly once per patch (when "what's new"
 * is opened) and written exactly once per patch (right before a refresh), so
 * the flexibility of a schema-less blob outweighs the cost of a query-time
 * parse, and it means adding a field to Champion or Item never requires a
 * migration here.
 */
@Singleton
class PreviousPatchSnapshotRepositoryImpl @Inject constructor(
    private val dao: PreviousPatchSnapshotDao,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PreviousPatchSnapshotRepository {

    override suspend fun getPreviousChampions(): Pair<String, List<Champion>>? =
        withContext(ioDispatcher) {
            val entity = dao.get(PreviousPatchSnapshotEntity.KIND_CHAMPIONS) ?: return@withContext null
            runCatching {
                val dtos = json.decodeFromString(
                    ListSerializer(ChampionSnapshotDto.serializer()),
                    entity.payloadJson,
                )
                entity.version to dtos.map { it.toDomain() }
            }.onFailure { throwable ->
                // A corrupt snapshot must never crash the diff feature; it is
                // strictly a nice-to-have, so log and behave as if there were
                // no snapshot at all.
                Timber.w(throwable, "Failed to decode previous champion snapshot")
            }.getOrNull()
        }

    override suspend fun getPreviousItems(): Pair<String, List<Item>>? =
        withContext(ioDispatcher) {
            val entity = dao.get(PreviousPatchSnapshotEntity.KIND_ITEMS) ?: return@withContext null
            runCatching {
                val dtos = json.decodeFromString(
                    ListSerializer(ItemSnapshotDto.serializer()),
                    entity.payloadJson,
                )
                entity.version to dtos.map { it.toDomain() }
            }.onFailure { throwable ->
                Timber.w(throwable, "Failed to decode previous item snapshot")
            }.getOrNull()
        }

    override suspend fun captureChampionSnapshot(version: String, current: List<Champion>) {
        withContext(ioDispatcher) {
            if (current.isEmpty()) return@withContext
            val payload = json.encodeToString(
                ListSerializer(ChampionSnapshotDto.serializer()),
                current.map { it.toSnapshotDto() },
            )
            dao.upsert(
                PreviousPatchSnapshotEntity(
                    kind = PreviousPatchSnapshotEntity.KIND_CHAMPIONS,
                    version = version,
                    payloadJson = payload,
                )
            )
        }
    }

    override suspend fun captureItemSnapshot(version: String, current: List<Item>) {
        withContext(ioDispatcher) {
            if (current.isEmpty()) return@withContext
            val payload = json.encodeToString(
                ListSerializer(ItemSnapshotDto.serializer()),
                current.map { it.toSnapshotDto() },
            )
            dao.upsert(
                PreviousPatchSnapshotEntity(
                    kind = PreviousPatchSnapshotEntity.KIND_ITEMS,
                    version = version,
                    payloadJson = payload,
                )
            )
        }
    }
}
