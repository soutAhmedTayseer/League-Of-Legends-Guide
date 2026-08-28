package com.venom7t.lolguide.data.lptracker.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LpSnapshotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(snapshots: List<LpSnapshotEntity>)

    @Query(
        """
        SELECT * FROM lp_snapshots
        WHERE puuid = :puuid AND queueType = :queueType
        ORDER BY capturedAtEpochMillis DESC
        """,
    )
    fun observeHistory(puuid: String, queueType: String): Flow<List<LpSnapshotEntity>>

    @Query(
        """
        SELECT * FROM lp_snapshots
        WHERE puuid = :puuid AND queueType = :queueType AND capturedAtEpochMillis < :beforeEpochMillis
        ORDER BY capturedAtEpochMillis DESC
        LIMIT 1
        """,
    )
    suspend fun getLatestBefore(puuid: String, queueType: String, beforeEpochMillis: Long): LpSnapshotEntity?
}
