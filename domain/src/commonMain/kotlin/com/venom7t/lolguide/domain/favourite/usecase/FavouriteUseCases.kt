package com.venom7t.lolguide.domain.favourite.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.favourite.repository.FavouritesRepository
import kotlinx.coroutines.flow.Flow

@Factory
class ObserveFavouriteIdsUseCase(
    private val repository: FavouritesRepository,
) {
    operator fun invoke(): Flow<Set<String>> = repository.observeFavouriteIds()
}

@Factory
class ToggleFavouriteUseCase(
    private val repository: FavouritesRepository,
) {
    /** Returns whether the champion is a favourite after the toggle. */
    suspend operator fun invoke(championId: String): Result<Boolean> =
        repository.toggle(championId)
}
