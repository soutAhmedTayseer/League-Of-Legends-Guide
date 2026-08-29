package com.venom7t.lolguide.domain.rotation.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.rotation.model.ChampionRotation
import com.venom7t.lolguide.domain.rotation.repository.RotationRepository

@Factory
class GetCurrentRotationUseCase(
    private val repository: RotationRepository,
) {
    suspend operator fun invoke(region: Region): Result<ChampionRotation> =
        repository.getCurrentRotation(region)
}
