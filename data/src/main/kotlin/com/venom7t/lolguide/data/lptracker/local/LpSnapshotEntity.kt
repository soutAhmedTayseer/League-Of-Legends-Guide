package com.venom7t.lolguide.data.lptracker.local

import androidx.room.Entity

/**
 * One polled ranked-standing reading. Permanent, append-only history: unlike
 * the champion/item caches, an old snapshot is never wholesale-replaced --
 * it is the record of what the tracker observed at that moment (Phase 5
 * plan, mirrors the permanent match cache's reasoning).
 */
@Entity(
    tableName = "lp_snapshots",
    primaryKeys = ["puuid", "queueType", "capturedAtEpochMillis"],
)
data class LpSnapshotEntity(
    val puuid: String,
    val queueType: String,
    val tier: String,
    val rank: String,
    val leaguePoints: Int,
    val capturedAtEpochMillis: Long,
)
