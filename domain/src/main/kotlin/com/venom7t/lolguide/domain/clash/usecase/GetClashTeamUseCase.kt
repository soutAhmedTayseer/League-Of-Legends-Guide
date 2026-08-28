package com.venom7t.lolguide.domain.clash.usecase

import com.venom7t.lolguide.domain.clash.model.ClashTeam
import com.venom7t.lolguide.domain.clash.repository.ClashRepository
import com.venom7t.lolguide.domain.onboarding.model.Region
import javax.inject.Inject

class GetClashTeamUseCase @Inject constructor(
    private val repository: ClashRepository,
) {
    suspend operator fun invoke(summonerId: String, region: Region): Result<ClashTeam?> =
        repository.getTeamForSummoner(summonerId, region)
}
