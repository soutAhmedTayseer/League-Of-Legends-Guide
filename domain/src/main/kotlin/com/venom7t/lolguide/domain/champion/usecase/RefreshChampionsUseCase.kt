package com.venom7t.lolguide.domain.champion.usecase

import com.venom7t.lolguide.domain.champion.repository.ChampionRepository
import com.venom7t.lolguide.domain.common.AppLocale
import javax.inject.Inject

class RefreshChampionsUseCase @Inject constructor(
    private val championRepository: ChampionRepository,
) {
    suspend operator fun invoke(version: String, locale: AppLocale): Result<Unit> =
        championRepository.refreshChampions(version, locale)
}
