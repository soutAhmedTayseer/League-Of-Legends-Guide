package com.venom7t.lolguide.domain.mastery.repository

import com.venom7t.lolguide.domain.mastery.model.ChampionMastery
import com.venom7t.lolguide.domain.onboarding.model.Region

interface MasteryRepository {
    suspend fun getChampionMasteries(puuid: String, region: Region): Result<List<ChampionMastery>>
}
