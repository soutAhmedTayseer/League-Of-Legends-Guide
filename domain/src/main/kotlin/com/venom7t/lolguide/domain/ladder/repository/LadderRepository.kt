package com.venom7t.lolguide.domain.ladder.repository

import com.venom7t.lolguide.domain.ladder.model.LadderEntry
import com.venom7t.lolguide.domain.onboarding.model.Region

interface LadderRepository {
    suspend fun getChallengerLadder(region: Region, queue: String): Result<List<LadderEntry>>
}
