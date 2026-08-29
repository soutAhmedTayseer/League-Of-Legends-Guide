package com.venom7t.lolguide.domain.champion.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.champion.repository.ChampionRepository
import com.venom7t.lolguide.domain.common.AppLocale

@Factory
class RefreshChampionsUseCase(
    private val championRepository: ChampionRepository,
) {
    suspend operator fun invoke(version: String, locale: AppLocale): Result<Unit> =
        championRepository.refreshChampions(version, locale)
}
