package com.venom7t.lolguide.data.common.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.venom7t.lolguide.data.champion.local.ChampionDao
import com.venom7t.lolguide.data.champion.local.ChampionEntity
import com.venom7t.lolguide.data.favourite.local.FavouriteChampionDao
import com.venom7t.lolguide.data.favourite.local.FavouriteChampionEntity
import com.venom7t.lolguide.data.item.local.ItemDao
import com.venom7t.lolguide.data.item.local.ItemEntity
import com.venom7t.lolguide.data.patch.local.PreviousPatchSnapshotDao
import com.venom7t.lolguide.data.patch.local.PreviousPatchSnapshotEntity

@Database(
    entities = [
        ChampionEntity::class,
        FavouriteChampionEntity::class,
        ItemEntity::class,
        PreviousPatchSnapshotEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class LolGuideDatabase : RoomDatabase() {

    abstract fun championDao(): ChampionDao

    abstract fun favouriteChampionDao(): FavouriteChampionDao

    abstract fun itemDao(): ItemDao

    abstract fun previousPatchSnapshotDao(): PreviousPatchSnapshotDao

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

        /**
         * Adds the item cache.
         *
         * Items are rebuildable from the CDN, so this table alone would
         * tolerate a destructive fallback -- but the same database holds
         * favourites, which are not rebuildable, so every migration from here
         * on has to be real.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `items` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `plaintext` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `imageFileName` TEXT NOT NULL,
                        `tags` TEXT NOT NULL,
                        `fromIds` TEXT NOT NULL,
                        `intoIds` TEXT NOT NULL,
                        `depth` INTEGER NOT NULL,
                        `requiredChampionId` TEXT,
                        `availableOnSummonersRift` INTEGER NOT NULL,
                        `patchVersion` TEXT NOT NULL,
                        `locale` TEXT NOT NULL,
                        `gold_base` INTEGER NOT NULL,
                        `gold_total` INTEGER NOT NULL,
                        `gold_sell` INTEGER NOT NULL,
                        `gold_purchasable` INTEGER NOT NULL,
                        `stat_attackDamage` REAL NOT NULL,
                        `stat_abilityPower` REAL NOT NULL,
                        `stat_health` REAL NOT NULL,
                        `stat_mana` REAL NOT NULL,
                        `stat_armor` REAL NOT NULL,
                        `stat_magicResist` REAL NOT NULL,
                        `stat_attackSpeedPercent` REAL NOT NULL,
                        `stat_critChancePercent` REAL NOT NULL,
                        `stat_healthRegen` REAL NOT NULL,
                        `stat_moveSpeedFlat` REAL NOT NULL,
                        `stat_moveSpeedPercent` REAL NOT NULL,
                        `stat_lifeStealPercent` REAL NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * Adds the previous-patch snapshot table, backing the Phase 3 patch
         * diff engine (AGENTS.md section 1 -- the diff is a derived result
         * computed from two real snapshots, never invented).
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `previous_patch_snapshots` (
                        `kind` TEXT NOT NULL,
                        `version` TEXT NOT NULL,
                        `payloadJson` TEXT NOT NULL,
                        PRIMARY KEY(`kind`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
