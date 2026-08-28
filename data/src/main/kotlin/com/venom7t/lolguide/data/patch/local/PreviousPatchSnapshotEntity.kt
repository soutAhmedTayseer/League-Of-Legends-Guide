package com.venom7t.lolguide.data.patch.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single stored snapshot: everything cached for one kind (champions or
 * items) as of one patch, serialized as JSON.
 *
 * [kind] is a fixed primary key ("champions" / "items") rather than an
 * auto-increment id -- there is deliberately at most one row per kind, since
 * this holds only the *previous* generation, not a history.
 */
@Entity(tableName = "previous_patch_snapshots")
data class PreviousPatchSnapshotEntity(
    @PrimaryKey val kind: String,
    val version: String,
    val payloadJson: String,
) {
    companion object {
        const val KIND_CHAMPIONS = "champions"
        const val KIND_ITEMS = "items"
    }
}
