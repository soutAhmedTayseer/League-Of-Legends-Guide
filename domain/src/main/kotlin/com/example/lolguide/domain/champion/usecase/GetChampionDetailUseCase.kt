package com.example.lolguide.domain.champion.usecase

import com.example.lolguide.domain.champion.model.Champion
import com.example.lolguide.domain.champion.model.ChampionDetail
import com.example.lolguide.domain.champion.repository.ChampionRepository
import com.example.lolguide.domain.common.AppError
import com.example.lolguide.domain.common.AppLocale
import javax.inject.Inject

/**
 * Loads everything the detail screen needs as one unit.
 *
 * The header (from the cached list) and the abilities (from a separate
 * endpoint) are fetched together and carry the same [version], because the
 * screen must never show a champion's stats on one patch beside its abilities
 * on another (AGENTS.md §1).
 */
class GetChampionDetailUseCase @Inject constructor(
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
