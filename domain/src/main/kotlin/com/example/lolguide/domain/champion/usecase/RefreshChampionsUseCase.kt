package com.example.lolguide.domain.champion.usecase

import com.example.lolguide.domain.champion.repository.ChampionRepository
import com.example.lolguide.domain.common.AppLocale
import javax.inject.Inject

class RefreshChampionsUseCase @Inject constructor(
    private val championRepository: ChampionRepository,
) {
    suspend operator fun invoke(version: String, locale: AppLocale): Result<Unit> =
        championRepository.refreshChampions(version, locale)
}
