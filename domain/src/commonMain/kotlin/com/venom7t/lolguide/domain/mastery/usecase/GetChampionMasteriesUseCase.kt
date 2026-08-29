package com.venom7t.lolguide.domain.mastery.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.mastery.model.ChampionMastery
import com.venom7t.lolguide.domain.mastery.repository.MasteryRepository
import com.venom7t.lolguide.domain.onboarding.model.Region

@Factory
class GetChampionMasteriesUseCase(
    private val repository: MasteryRepository,
) {
    /** Sorted by points descending -- "your top champions" is the only order this list is ever shown in. */
    suspend operator fun invoke(puuid: String, region: Region): Result<List<ChampionMastery>> =
        repository.getChampionMasteries(puuid, region)
            .map { it.sortedByDescending { mastery -> mastery.championPoints } }
}
