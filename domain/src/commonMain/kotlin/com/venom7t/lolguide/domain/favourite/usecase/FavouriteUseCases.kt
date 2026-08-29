package com.venom7t.lolguide.domain.favourite.usecase

import com.venom7t.lolguide.domain.favourite.repository.FavouritesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavouriteIdsUseCase @Inject constructor(
    private val repository: FavouritesRepository,
) {
    operator fun invoke(): Flow<Set<String>> = repository.observeFavouriteIds()
}

class ToggleFavouriteUseCase @Inject constructor(
    private val repository: FavouritesRepository,
) {
    /** Returns whether the champion is a favourite after the toggle. */
    suspend operator fun invoke(championId: String): Result<Boolean> =
        repository.toggle(championId)
}
