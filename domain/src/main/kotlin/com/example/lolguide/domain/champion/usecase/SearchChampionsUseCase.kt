package com.example.lolguide.domain.champion.usecase

import com.example.lolguide.domain.champion.model.Champion
import javax.inject.Inject

/**
 * Filters an already-loaded champion list.
 *
 * Pure and synchronous on purpose: searching must not hit the network or the
 * database on every keystroke. Phase 1 extends this with community aliases
 * ("mundo", "asol", "kata"); Phase 0 matches name and title only.
 */
class SearchChampionsUseCase @Inject constructor() {

    operator fun invoke(champions: List<Champion>, query: String): List<Champion> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return champions

        return champions.filter { champion ->
            champion.name.contains(trimmed, ignoreCase = true) ||
                champion.title.contains(trimmed, ignoreCase = true)
        }
    }
}
