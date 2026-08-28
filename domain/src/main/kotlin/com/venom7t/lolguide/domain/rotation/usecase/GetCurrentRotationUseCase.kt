package com.venom7t.lolguide.domain.rotation.usecase

import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.rotation.model.ChampionRotation
import com.venom7t.lolguide.domain.rotation.repository.RotationRepository
import javax.inject.Inject

class GetCurrentRotationUseCase @Inject constructor(
    private val repository: RotationRepository,
) {
    suspend operator fun invoke(region: Region): Result<ChampionRotation> =
        repository.getCurrentRotation(region)
}
