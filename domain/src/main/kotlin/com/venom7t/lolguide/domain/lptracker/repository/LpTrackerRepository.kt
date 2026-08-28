package com.venom7t.lolguide.domain.lptracker.repository

import com.venom7t.lolguide.domain.lptracker.model.LpSnapshot
import com.venom7t.lolguide.domain.summoner.model.RankedQueue
import kotlinx.coroutines.flow.Flow

interface LpTrackerRepository {

    /** Records one poll's worth of ranked entries as new snapshots. */
    suspend fun recordSnapshots(puuid: String, snapshots: List<LpSnapshot>)

    /** History for one summoner/queue, newest first. */
    fun observeHistory(puuid: String, queueType: RankedQueue): Flow<List<LpSnapshot>>

    /** The most recent snapshot per tracked puuid/queue, for a notification diff. */
    suspend fun getLatestBefore(puuid: String, queueType: RankedQueue, beforeEpochMillis: Long): LpSnapshot?
}
