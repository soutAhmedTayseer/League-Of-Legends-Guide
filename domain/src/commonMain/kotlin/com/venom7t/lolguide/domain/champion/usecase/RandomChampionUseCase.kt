package com.venom7t.lolguide.domain.champion.usecase

import com.venom7t.lolguide.domain.champion.model.Champion
import javax.inject.Inject
import kotlin.random.Random

/**
 * Picks a random champion from an already-filtered pool.
 *
 * Takes the pool rather than fetching one, so "random support" and "random AP
 * jungler" are just the roulette applied to the list screen's active filter --
 * no second filtering path to keep in sync.
 */
class RandomChampionUseCase @Inject constructor() {

    /**
     * @param excludeId the previous result, so pressing reroll twice does not
     *   hand back the same champion. Ignored when the pool has only one entry.
     */
    operator fun invoke(
        pool: List<Champion>,
        excludeId: String? = null,
        random: Random = Random.Default,
    ): Champion? {
        if (pool.isEmpty()) return null

        val candidates = if (excludeId != null && pool.size > 1) {
            pool.filterNot { it.id == excludeId }
        } else {
            pool
        }

        return candidates.getOrNull(random.nextInt(candidates.size))
    }
}
