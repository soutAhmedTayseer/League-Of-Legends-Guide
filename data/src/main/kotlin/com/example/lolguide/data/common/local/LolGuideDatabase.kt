package com.example.lolguide.data.common.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lolguide.data.champion.local.ChampionDao
import com.example.lolguide.data.champion.local.ChampionEntity

/**
 * The app's single Room database.
 *
 * Phase 1 adds favourites, Phase 2 saved rune pages, Phase 4 cached matches.
 * Each of those is a schema bump with a real migration -- destructive fallback
 * is acceptable only while the only content here is a rebuildable CDN cache.
 */
@Database(
    entities = [ChampionEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class LolGuideDatabase : RoomDatabase() {

    abstract fun championDao(): ChampionDao

    companion object {
        const val NAME = "lol_guide.db"
    }
}
