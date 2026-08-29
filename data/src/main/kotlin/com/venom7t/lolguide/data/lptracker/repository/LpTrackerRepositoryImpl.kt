package com.venom7t.lolguide.data.lptracker.repository

import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.lptracker.local.LpSnapshotDao
import com.venom7t.lolguide.data.lptracker.local.LpSnapshotEntity
import com.venom7t.lolguide.domain.lptracker.model.LpSnapshot
import com.venom7t.lolguide.domain.lptracker.repository.LpTrackerRepository
import com.venom7t.lolguide.domain.summoner.model.RankedQueue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LpTrackerRepositoryImpl constructor(
    private val dao: LpSnapshotDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : LpTrackerRepository {

    override suspend fun recordSnapshots(puuid: String, snapshots: List<LpSnapshot>) {
        withContext(ioDispatcher) {
            dao.insertAll(snapshots.map { it.toEntity() })
        }
    }

    override fun observeHistory(puuid: String, queueType: RankedQueue): Flow<List<LpSnapshot>> =
        dao.observeHistory(puuid, queueType.riotQueueType).map { entities ->
            entities.mapNotNull { it.toDomain() }
        }

    override suspend fun getLatestBefore(
        puuid: String,
        queueType: RankedQueue,
        beforeEpochMillis: Long,
    ): LpSnapshot? = withContext(ioDispatcher) {
        dao.getLatestBefore(puuid, queueType.riotQueueType, beforeEpochMillis)?.toDomain()
    }

    private fun LpSnapshot.toEntity() = LpSnapshotEntity(
        puuid = puuid,
        queueType = queueType.riotQueueType,
        tier = tier,
        rank = rank,
        leaguePoints = leaguePoints,
        capturedAtEpochMillis = capturedAtEpochMillis,
    )

    private fun LpSnapshotEntity.toDomain(): LpSnapshot? {
        val queue = RankedQueue.fromRiotQueueType(queueType) ?: return null
        return LpSnapshot(
            puuid = puuid,
            queueType = queue,
            tier = tier,
            rank = rank,
            leaguePoints = leaguePoints,
            capturedAtEpochMillis = capturedAtEpochMillis,
        )
    }
}
