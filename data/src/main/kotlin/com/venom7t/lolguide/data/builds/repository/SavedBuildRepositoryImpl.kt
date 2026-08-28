package com.venom7t.lolguide.data.builds.repository

import com.venom7t.lolguide.data.builds.local.SavedBuildDao
import com.venom7t.lolguide.data.builds.local.SavedBuildEntity
import com.venom7t.lolguide.data.common.di.ApplicationScope
import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.common.toAppError
import com.venom7t.lolguide.domain.builds.model.SavedBuild
import com.venom7t.lolguide.domain.builds.repository.SavedBuildRepository
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.sync.repository.SyncRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedBuildRepositoryImpl @Inject constructor(
    private val dao: SavedBuildDao,
    private val syncRepository: SyncRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : SavedBuildRepository {

    override fun observeSavedBuilds(championId: String): Flow<List<SavedBuild>> =
        dao.observeForChampion(championId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: String): SavedBuild? =
        withContext(ioDispatcher) { dao.getById(id)?.toDomain() }

    override suspend fun saveBuild(
        championId: String,
        itemIds: List<String>,
        level: Int,
    ): Result<SavedBuild> = withContext(ioDispatcher) {
        runCatchingCancellable {
            val build = SavedBuild(
                id = UUID.randomUUID().toString(),
                championId = championId,
                itemIds = itemIds,
                level = level,
                savedAtEpochMillis = System.currentTimeMillis(),
            )
            dao.insert(build.toEntity())
            build
        }.recoverCatching { throwable -> throw throwable.toAppError() }
            .onSuccess { build -> pushSavedBestEffort(build) }
    }

    override suspend fun deleteBuild(id: String): Result<Unit> = withContext(ioDispatcher) {
        runCatchingCancellable { dao.delete(id) }
            .recoverCatching { throwable -> throw throwable.toAppError() }
            .onSuccess { pushDeletedBestEffort(id) }
    }

    override suspend fun ensureBuild(build: SavedBuild) {
        withContext(ioDispatcher) {
            if (dao.exists(build.id)) return@withContext
            dao.insert(build.toEntity())
        }
    }

    /**
     * Fire-and-forget for the same reason [com.venom7t.lolguide.data.favourite.repository.FavouritesRepositoryImpl]'s
     * push is: saving a build must never make the UI wait on a network round
     * trip, and must survive the caller's own scope ending.
     */
    private fun pushSavedBestEffort(build: SavedBuild) {
        applicationScope.launch { syncRepository.pushSavedBuild(build) }
    }

    private fun pushDeletedBestEffort(id: String) {
        applicationScope.launch { syncRepository.pushDeletedSavedBuild(id) }
    }
}

private fun SavedBuildEntity.toDomain() = SavedBuild(
    id = id,
    championId = championId,
    itemIds = itemIds,
    level = level,
    savedAtEpochMillis = savedAtEpochMillis,
)

private fun SavedBuild.toEntity() = SavedBuildEntity(
    id = id,
    championId = championId,
    itemIds = itemIds,
    level = level,
    savedAtEpochMillis = savedAtEpochMillis,
)
