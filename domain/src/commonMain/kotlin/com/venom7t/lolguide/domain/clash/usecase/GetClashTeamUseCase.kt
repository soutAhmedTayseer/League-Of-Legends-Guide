package com.venom7t.lolguide.domain.clash.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.clash.model.ClashTeam
import com.venom7t.lolguide.domain.clash.repository.ClashRepository
import com.venom7t.lolguide.domain.onboarding.model.Region

@Factory
class GetClashTeamUseCase(
    private val repository: ClashRepository,
) {
    suspend operator fun invoke(summonerId: String, region: Region): Result<ClashTeam?> =
        repository.getTeamForSummoner(summonerId, region)
}
