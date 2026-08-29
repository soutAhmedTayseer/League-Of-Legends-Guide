package com.venom7t.lolguide.domain.champion.usecase

import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.repository.ChampionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Cached champions, alphabetised.
 *
 * Data Dragon returns its map in an order that is neither alphabetical nor
 * stable across patches, so sorting here keeps the list from reshuffling under
 * the user when a refresh lands.
 */
class ObserveChampionsUseCase @Inject constructor(
    private val championRepository: ChampionRepository,
) {
    operator fun invoke(): Flow<List<Champion>> =
        championRepository.observeChampions()
            .map { champions -> champions.sortedBy { it.name } }
}
