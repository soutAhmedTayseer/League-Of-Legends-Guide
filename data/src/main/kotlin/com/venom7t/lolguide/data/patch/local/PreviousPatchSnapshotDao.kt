package com.venom7t.lolguide.data.patch.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PreviousPatchSnapshotDao {

    @Query("SELECT * FROM previous_patch_snapshots WHERE kind = :kind LIMIT 1")
    suspend fun get(kind: String): PreviousPatchSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PreviousPatchSnapshotEntity)
}
