package com.venom7t.lolguide.data.match.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A cached, full match.
 *
 * Unlike the champion/item caches, this table is **never wholesale-replaced**
 * -- a finished match cannot change, so once fetched it is cached forever
 * (AGENTS.md §8.3). [payloadJson] holds the full serialized [com.venom7t.lolguide.data.riot.remote.dto.MatchDto]
 * rather than a flattened set of columns: this data is read back exactly once
 * per row (a re-visit to a match detail screen), so a schema-less blob avoids
 * a migration every time a new field is needed off the participant payload.
 */
@Entity(tableName = "cached_matches")
data class MatchEntity(
    @PrimaryKey val matchId: String,
    val payloadJson: String,
    val cachedAtEpochMillis: Long,
)
