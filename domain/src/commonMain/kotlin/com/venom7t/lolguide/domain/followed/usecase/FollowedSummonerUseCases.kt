package com.venom7t.lolguide.domain.followed.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.followed.model.FollowedSummoner
import com.venom7t.lolguide.domain.followed.repository.FollowedSummonerRepository
import com.venom7t.lolguide.domain.summoner.model.Summoner
import kotlinx.coroutines.flow.Flow

@Factory
class ObserveFollowedSummonersUseCase(
    private val repository: FollowedSummonerRepository,
) {
    operator fun invoke(): Flow<List<FollowedSummoner>> = repository.observeFollowed()
}

@Factory
class ToggleFollowedSummonerUseCase(
    private val repository: FollowedSummonerRepository,
) {
    /** Returns whether the summoner is followed after the toggle. */
    suspend operator fun invoke(summoner: Summoner): Boolean {
        val wasFollowed = repository.isFollowed(summoner.puuid)
        if (wasFollowed) {
            repository.unfollow(summoner.puuid)
        } else {
            repository.follow(summoner)
        }
        return !wasFollowed
    }
}
