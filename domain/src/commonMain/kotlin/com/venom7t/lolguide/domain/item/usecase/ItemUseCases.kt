package com.venom7t.lolguide.domain.item.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.domain.item.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Items that belong in a browse list.
 *
 * Data Dragon's payload includes trinkets, Arena-only items, removed items
 * kept for old match data, and champion-locked items. Showing all of them
 * would make the browser useless, so the filter is applied here once rather
 * than being re-derived by every caller.
 */
@Factory
class ObservePurchasableItemsUseCase(
    private val repository: ItemRepository,
) {
    operator fun invoke(): Flow<List<Item>> =
        repository.observeItems().map { items ->
            items
                .filter { it.isPurchasable && it.availableOnSummonersRift }
                .filter { it.requiredChampionId == null }
                .sortedWith(compareBy({ it.gold.total }, { it.name }))
        }
}

@Factory
class RefreshItemsUseCase(
    private val repository: ItemRepository,
) {
    suspend operator fun invoke(version: String, locale: AppLocale): Result<Unit> =
        repository.refreshItems(version, locale)
}

/**
 * Resolves an item's components and the items it builds into, one level deep
 * in each direction.
 *
 * Deliberately not recursive: a full transitive tree for a legendary item is
 * dozens of nodes and unreadable on a phone. One level up and one level down
 * is what a player actually asks -- "what do I buy first" and "where does this
 * go".
 */
@Factory
class GetBuildPathUseCase(
    private val repository: ItemRepository,
) {
    suspend operator fun invoke(item: Item): BuildPath = BuildPath(
        components = repository.getCachedItems(item.from),
        buildsInto = repository.getCachedItems(item.into),
    )
}

data class BuildPath(
    val components: List<Item>,
    val buildsInto: List<Item>,
)
