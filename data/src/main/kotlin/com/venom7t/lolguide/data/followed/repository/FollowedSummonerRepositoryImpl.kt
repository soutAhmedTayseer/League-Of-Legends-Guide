package com.venom7t.lolguide.data.followed.repository

import com.venom7t.lolguide.data.common.di.ApplicationScope
import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.followed.local.FollowedSummonerDao
import com.venom7t.lolguide.data.followed.local.FollowedSummonerEntity
import com.venom7t.lolguide.domain.followed.model.FollowedSummoner
import com.venom7t.lolguide.domain.followed.repository.FollowedSummonerRepository
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.summoner.model.Summoner
import com.venom7t.lolguide.domain.sync.repository.SyncRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowedSummonerRepositoryImpl @Inject constructor(
    private val dao: FollowedSummonerDao,
    private val syncRepository: SyncRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val applicationScope: CoroutineScope,
) : FollowedSummonerRepository {

    override fun observeFollowed(): Flow<List<FollowedSummoner>> =
        dao.observeAll().map { entities -> entities.mapNotNull { it.toDomain() } }

    override suspend fun isFollowed(puuid: String): Boolean =
        withContext(ioDispatcher) { dao.isFollowed(puuid) }

    override suspend fun follow(summoner: Summoner) {
        val followedAt = System.currentTimeMillis()
        withContext(ioDispatcher) {
            dao.insert(
                FollowedSummonerEntity(
                    puuid = summoner.puuid,
                    riotIdName = summoner.riotIdName,
                    riotIdTagline = summoner.riotIdTagline,
                    regionName = summoner.region.name,
                    followedAtEpochMillis = followedAt,
                )
            )
        }
        pushFollowBestEffort(
            FollowedSummoner(
                puuid = summoner.puuid,
                riotIdName = summoner.riotIdName,
                riotIdTagline = summoner.riotIdTagline,
                region = summoner.region,
                followedAtEpochMillis = followedAt,
            ),
        )
    }

    override suspend fun followRaw(summoner: FollowedSummoner) {
        withContext(ioDispatcher) {
            dao.insert(
                FollowedSummonerEntity(
                    puuid = summoner.puuid,
                    riotIdName = summoner.riotIdName,
                    riotIdTagline = summoner.riotIdTagline,
                    regionName = summoner.region.name,
                    followedAtEpochMillis = summoner.followedAtEpochMillis,
                ),
            )
        }
    }

    override suspend fun unfollow(puuid: String) {
        withContext(ioDispatcher) { dao.delete(puuid) }
        applicationScope.launch { syncRepository.pushUnfollowedSummoner(puuid) }
    }

    private fun pushFollowBestEffort(summoner: FollowedSummoner) {
        applicationScope.launch { syncRepository.pushFollowedSummoner(summoner) }
    }

    private fun FollowedSummonerEntity.toDomain(): FollowedSummoner? {
        // A region enum value that no longer exists (renamed/removed in a
        // future change) drops the row from the visible list rather than
        // crashing the whole followed-summoners screen over one bad entry.
        val region = Region.entries.firstOrNull { it.name == regionName } ?: return null
        return FollowedSummoner(
            puuid = puuid,
            riotIdName = riotIdName,
            riotIdTagline = riotIdTagline,
            region = region,
            followedAtEpochMillis = followedAtEpochMillis,
        )
    }
}
