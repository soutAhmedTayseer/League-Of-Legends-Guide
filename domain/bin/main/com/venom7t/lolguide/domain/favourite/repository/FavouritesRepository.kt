package com.venom7t.lolguide.domain.favourite.repository

import kotlinx.coroutines.flow.Flow

/**
 * The user's favourited champions.
 *
 * Unlike the champion cache, this is **user-authored data**: it cannot be
 * re-downloaded if it is lost. That is why the Room schema change introducing
 * it also removes destructive migration fallback (Phase 1 plan, §1.3).
 */
interface FavouritesRepository {

    /** Favourited champion ids, emitting again on every change. */
    fun observeFavouriteIds(): Flow<Set<String>>

    suspend fun isFavourite(championId: String): Boolean

    /** Adds or removes, returning the state the champion ended up in. */
    suspend fun toggle(championId: String): Result<Boolean>
}
