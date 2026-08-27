package com.example.lolguide.domain.champion.repository

import com.example.lolguide.domain.champion.model.Champion
import com.example.lolguide.domain.champion.model.ChampionDetail
import com.example.lolguide.domain.common.AppLocale
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first champion access (AGENTS.md §7.1).
 *
 * The read path and the refresh path are deliberately separate. Observing
 * returns whatever is cached, immediately and forever; refreshing is a
 * fallible one-shot. That split is what lets a screen show cached champions
 * *and* an error banner at the same time, instead of choosing between them.
 */
interface ChampionRepository {

    /** Cached champions, emitting again whenever the cache changes. */
    fun observeChampions(): Flow<List<Champion>>

    /** A single cached champion, or null if it is not cached. */
    suspend fun getCachedChampion(championId: String): Champion?

    /**
     * Fetches the champion list for [version] and replaces the cache.
     *
     * The cache holds exactly one patch at a time: on a version change the
     * old rows are dropped rather than merged, so a stale champion from a
     * previous patch can never survive alongside current ones (AGENTS.md §1).
     */
    suspend fun refreshChampions(version: String, locale: AppLocale): Result<Unit>

    /** Abilities and lore for one champion, on [version]. */
    suspend fun getChampionDetail(
        championId: String,
        version: String,
        locale: AppLocale,
    ): Result<ChampionDetail>
}
