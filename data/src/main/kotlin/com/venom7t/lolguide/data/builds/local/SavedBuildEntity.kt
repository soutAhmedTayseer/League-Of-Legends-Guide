package com.venom7t.lolguide.data.builds.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_builds")
data class SavedBuildEntity(
    @PrimaryKey val id: String,
    val championId: String,
    /** Stored as JSON via [com.venom7t.lolguide.data.common.local.Converters] -- see that class's doc comment. */
    val itemIds: List<String>,
    val level: Int,
    val savedAtEpochMillis: Long,
)
