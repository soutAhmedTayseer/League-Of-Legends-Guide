package com.venom7t.lolguide.data.favourite.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A favourited champion.
 *
 * Stores only the id, not a copy of the champion: the champion cache is
 * replaced wholesale on every patch, and a denormalised copy here would go
 * stale the moment that happened.
 */
@Entity(tableName = "favourite_champions")
data class FavouriteChampionEntity(
    @PrimaryKey val championId: String,
    val favouritedAtEpochMillis: Long,
)
