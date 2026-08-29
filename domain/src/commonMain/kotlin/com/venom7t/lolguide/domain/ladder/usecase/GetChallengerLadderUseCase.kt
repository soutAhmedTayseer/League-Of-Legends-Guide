package com.venom7t.lolguide.domain.ladder.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.ladder.model.LadderEntry
import com.venom7t.lolguide.domain.ladder.repository.LadderRepository
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.summoner.model.RankedQueue

@Factory
class GetChallengerLadderUseCase(
    private val repository: LadderRepository,
) {
    suspend operator fun invoke(
        region: Region,
        queue: RankedQueue = RankedQueue.SOLO_DUO,
    ): Result<List<LadderEntry>> = repository.getChallengerLadder(region, queue.riotQueueType)
}
