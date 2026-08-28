package com.example.lolguide.data.common.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lolguide.data.champion.local.ChampionDao
import com.example.lolguide.data.champion.local.ChampionEntity
import com.example.lolguide.data.favourite.local.FavouriteChampionDao
import com.example.lolguide.data.favourite.local.FavouriteChampionEntity

@Database(
    entities = [ChampionEntity::class, FavouriteChampionEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class LolGuideDatabase : RoomDatabase() {

    abstract fun championDao(): ChampionDao

    abstract fun favouriteChampionDao(): FavouriteChampionDao

    companion object {
        const val NAME = "lol_guide.db"

        /**
         * Adds the favourites table.
         *
         * This is a real migration rather than a destructive fallback because
         * favourites are **user-authored data**: unlike the champion cache,
         * they cannot be re-downloaded. Phase 0 could get away with dropping
         * everything on a schema change; from here on it would lose something
         * the user made.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `favourite_champions` (
                        `championId` TEXT NOT NULL,
                        `favouritedAtEpochMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`championId`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
