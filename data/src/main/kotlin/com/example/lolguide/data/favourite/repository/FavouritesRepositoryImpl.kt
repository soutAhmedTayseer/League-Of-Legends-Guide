package com.example.lolguide.data.favourite.repository

import com.example.lolguide.data.common.di.IoDispatcher
import com.example.lolguide.data.common.toAppError
import com.example.lolguide.data.favourite.local.FavouriteChampionDao
import com.example.lolguide.data.favourite.local.FavouriteChampionEntity
import com.example.lolguide.domain.common.runCatchingCancellable
import com.example.lolguide.domain.favourite.repository.FavouritesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouritesRepositoryImpl @Inject constructor(
    private val dao: FavouriteChampionDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
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
        }
}
