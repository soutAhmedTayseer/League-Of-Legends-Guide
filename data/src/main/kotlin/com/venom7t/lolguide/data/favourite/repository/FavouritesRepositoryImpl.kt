package com.venom7t.lolguide.data.favourite.repository

import com.venom7t.lolguide.data.common.di.ApplicationScope
import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.common.toAppError
import com.venom7t.lolguide.data.favourite.local.FavouriteChampionDao
import com.venom7t.lolguide.data.favourite.local.FavouriteChampionEntity
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.favourite.repository.FavouritesRepository
import com.venom7t.lolguide.domain.sync.repository.SyncRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouritesRepositoryImpl @Inject constructor(
    private val dao: FavouriteChampionDao,
    private val syncRepository: SyncRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : FavouritesRepository {

    override fun observeFavouriteIds(): Flow<Set<String>> =
        dao.observeIds().map { it.toSet() }

    override suspend fun isFavourite(championId: String): Boolean =
        withContext(ioDispatcher) { dao.isFavourite(championId) }

    override suspend fun toggle(championId: String): Result<Boolean> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                val wasFavourite = dao.isFavourite(championId)
                if (wasFavourite) {
                    dao.remove(championId)
                } else {
                    dao.add(
                        FavouriteChampionEntity(
                            championId = championId,
                            favouritedAtEpochMillis = System.currentTimeMillis(),
                        )
                    )
                }
                !wasFavourite
            }.recoverCatching { throwable -> throw throwable.toAppError() }
                .onSuccess { isNowFavourite -> pushBestEffort(championId, isNowFavourite) }
        }

    override suspend fun ensureFavourite(championId: String) {
        withContext(ioDispatcher) {
            if (dao.isFavourite(championId)) return@withContext
            dao.add(
                FavouriteChampionEntity(
                    championId = championId,
                    favouritedAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        }
    }

    /**
     * Fire-and-forget: favouriting must never make the UI wait on a network
     * round trip, and must not be cancelled by the caller's own scope ending
     * (e.g. the champion detail screen being left) -- see
     * [ApplicationScope]'s doc comment.
     */
    private fun pushBestEffort(championId: String, isFavourite: Boolean) {
        applicationScope.launch { syncRepository.pushFavourite(championId, isFavourite) }
    }
}
