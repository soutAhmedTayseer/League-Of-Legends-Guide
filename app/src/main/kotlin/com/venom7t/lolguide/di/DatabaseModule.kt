package com.venom7t.lolguide.di

import androidx.room.Room
import com.venom7t.lolguide.data.common.local.LolGuideDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    single {
        Room.databaseBuilder(androidContext(), LolGuideDatabase::class.java, LolGuideDatabase.NAME)
            // Destructive fallback was acceptable in Phase 0 when the only
            // content was a rebuildable CDN cache. Favourites are user-authored
            // and cannot be re-downloaded, so schema changes migrate properly
            // from here on.
            .addMigrations(
                LolGuideDatabase.MIGRATION_1_2,
                LolGuideDatabase.MIGRATION_2_3,
                LolGuideDatabase.MIGRATION_3_4,
                LolGuideDatabase.MIGRATION_4_5,
                LolGuideDatabase.MIGRATION_5_6,
                LolGuideDatabase.MIGRATION_6_7,
            )
            .build()
    }

    single { get<LolGuideDatabase>().championDao() }
    single { get<LolGuideDatabase>().favouriteChampionDao() }
    single { get<LolGuideDatabase>().itemDao() }
    single { get<LolGuideDatabase>().previousPatchSnapshotDao() }
    single { get<LolGuideDatabase>().matchDao() }
    single { get<LolGuideDatabase>().followedSummonerDao() }
    single { get<LolGuideDatabase>().lpSnapshotDao() }
    single { get<LolGuideDatabase>().savedBuildDao() }
}
