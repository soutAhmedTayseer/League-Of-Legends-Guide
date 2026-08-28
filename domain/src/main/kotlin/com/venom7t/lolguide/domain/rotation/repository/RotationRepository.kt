package com.venom7t.lolguide.domain.rotation.repository

import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.rotation.model.ChampionRotation

interface RotationRepository {
    suspend fun getCurrentRotation(region: Region): Result<ChampionRotation>
}
