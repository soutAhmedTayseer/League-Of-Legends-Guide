package com.venom7t.lolguide.domain.item.repository

import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.item.model.Item
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first item access, mirroring ChampionRepository (AGENTS.md 7.1):
 * observing serves the cache, refreshing is a fallible one-shot that only
 * writes to it.
 */
interface ItemRepository {

    fun observeItems(): Flow<List<Item>>

    suspend fun getCachedItem(itemId: String): Item?

    /** Resolves several ids at once, for build paths and the simulator. */
    suspend fun getCachedItems(itemIds: List<String>): List<Item>

    suspend fun refreshItems(version: String, locale: AppLocale): Result<Unit>
}
