package com.venom7t.lolguide.domain.patch.repository

import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.item.model.Item

/**
 * The champion and item cache **as it stood on the previous patch**, retained
 * one generation back.
 *
 * The live champion/item caches (`ChampionRepository`, `ItemRepository`) are
 * replaced wholesale on every patch change (Phase 0 §7.1, Phase 2 §1.3) --
 * that is correct for "what does the game look like now" but means the
 * previous patch's data is gone by the time a diff would want it. This
 * repository is populated with a copy of the live cache immediately before
 * each wholesale replace, so exactly one prior generation is always available.
 */
interface PreviousPatchSnapshotRepository {

    /** Null before the first-ever patch transition this install has seen. */
    suspend fun getPreviousChampions(): Pair<String, List<Champion>>?

    suspend fun getPreviousItems(): Pair<String, List<Item>>?

    /**
     * Called immediately before the live champion cache is overwritten with a
     * new patch's data, so [current] becomes retrievable as "the previous
     * patch" from that point on.
     */
    suspend fun captureChampionSnapshot(version: String, current: List<Champion>)

    suspend fun captureItemSnapshot(version: String, current: List<Item>)
}
