package com.venom7t.lolguide.domain.champion.usecase

import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionAliases
import com.venom7t.lolguide.domain.champion.model.ChampionAliases.normalise
import javax.inject.Inject

/**
 * Ranked champion search.
 *
 * Pure and synchronous: search must not hit the network or the database on
 * every keystroke.
 *
 * Results are **ranked, not merely filtered**. A plain `contains` check puts
 * Cassiopeia and Kassadin in arbitrary order for "kass", and never finds
 * Wukong at all because his Data Dragon id is `MonkeyKing`. Ranking by match
 * quality is what makes the first result the one the player meant.
 */
class SearchChampionsUseCase @Inject constructor() {

    operator fun invoke(champions: List<Champion>, query: String): List<Champion> {
        val normalised = query.normalise()
        if (normalised.isEmpty()) return champions

        val aliasTarget = ChampionAliases.resolve(query)

        return champions
            .mapNotNull { champion ->
                score(champion, normalised, aliasTarget)?.let { champion to it }
            }
            // Ascending: a lower score is a better match. Ties break on name so
            // the order is stable between recompositions.
            .sortedWith(compareBy({ it.second }, { it.first.name }))
            .map { it.first }
    }

    private fun score(champion: Champion, query: String, aliasTarget: String?): Int? {
        val name = champion.name.normalise()
        val id = champion.id.normalise()

        return when {
            // An alias is an explicit, curated match: always first.
            aliasTarget != null && champion.id == aliasTarget -> RANK_ALIAS
            name == query || id == query -> RANK_EXACT
            name.startsWith(query) || id.startsWith(query) -> RANK_PREFIX
            name.contains(query) || id.contains(query) -> RANK_CONTAINS
            champion.title.normalise().contains(query) -> RANK_TITLE
            // "ktrn" -> Katarina. Catches the way people type on a phone.
            isSubsequence(query, name) -> RANK_SUBSEQUENCE
            else -> null
        }
    }

    /** True when every character of [query] appears in [target], in order. */
    private fun isSubsequence(query: String, target: String): Boolean {
        if (query.isEmpty()) return true
        var index = 0
        for (character in target) {
            if (character == query[index]) {
                index++
                if (index == query.length) return true
            }
        }
        return false
    }

    private companion object {
        const val RANK_ALIAS = 0
        const val RANK_EXACT = 1
        const val RANK_PREFIX = 2
        const val RANK_CONTAINS = 3
        const val RANK_TITLE = 4
        const val RANK_SUBSEQUENCE = 5
    }
}
