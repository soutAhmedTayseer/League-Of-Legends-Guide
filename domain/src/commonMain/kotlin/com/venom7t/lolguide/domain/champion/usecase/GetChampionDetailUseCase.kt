package com.venom7t.lolguide.domain.champion.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionDetail
import com.venom7t.lolguide.domain.champion.repository.ChampionRepository
import com.venom7t.lolguide.domain.common.AppError
import com.venom7t.lolguide.domain.common.AppLocale

/**
 * Loads everything the detail screen needs as one unit.
 *
 * The header (from the cached list) and the abilities (from a separate
 * endpoint) are fetched together and carry the same [version], because the
 * screen must never show a champion's stats on one patch beside its abilities
 * on another (AGENTS.md §1).
 */
@Factory
class GetChampionDetailUseCase(
    private val championRepository: ChampionRepository,
) {

    suspend operator fun invoke(
        championId: String,
        version: String,
        locale: AppLocale,
    ): Result<ChampionWithDetail> {
        val champion = championRepository.getCachedChampion(championId)
            ?: return Result.failure(AppError.NotFound(championId))

        return championRepository.getChampionDetail(championId, version, locale)
            .map { detail -> ChampionWithDetail(champion = champion, detail = detail) }
    }
}

data class ChampionWithDetail(
    val champion: Champion,
    val detail: ChampionDetail,
)
