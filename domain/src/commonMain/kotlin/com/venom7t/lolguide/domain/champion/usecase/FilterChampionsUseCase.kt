package com.venom7t.lolguide.domain.champion.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionFilter
import com.venom7t.lolguide.domain.champion.model.DamageType
import com.venom7t.lolguide.domain.champion.model.Difficulty

/**
 * Applies the list filters.
 *
 * Within a category the options are OR-ed (Mage or Assassin), across
 * categories they are AND-ed (a Mage *and* low difficulty). That is what
 * players expect from filter chips, and the opposite convention makes multi-
 * select useless — selecting a second role would narrow to nothing.
 */
@Factory
class FilterChampionsUseCase() {

    operator fun invoke(
        champions: List<Champion>,
        filter: ChampionFilter,
        favouriteIds: Set<String>,
    ): List<Champion> {
        if (!filter.isActive) return champions

        return champions.filter { champion ->
            matchesRole(champion, filter) &&
                matchesResource(champion, filter) &&
                matchesDifficulty(champion, filter) &&
                matchesDamageType(champion, filter) &&
                (!filter.favouritesOnly || champion.id in favouriteIds)
        }
    }

    private fun matchesRole(champion: Champion, filter: ChampionFilter): Boolean =
        filter.roles.isEmpty() || champion.tags.any { it in filter.roles }

    private fun matchesResource(champion: Champion, filter: ChampionFilter): Boolean =
        filter.resources.isEmpty() || champion.partype in filter.resources

    private fun matchesDifficulty(champion: Champion, filter: ChampionFilter): Boolean =
        filter.difficulties.isEmpty() ||
            Difficulty.of(champion.info.difficulty) in filter.difficulties

    private fun matchesDamageType(champion: Champion, filter: ChampionFilter): Boolean =
        filter.damageTypes.isEmpty() || DamageType.of(champion.info) in filter.damageTypes
}
