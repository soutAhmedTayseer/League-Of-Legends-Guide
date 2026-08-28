package com.venom7t.lolguide.di

import android.content.Context
import androidx.room.Room
import com.venom7t.lolguide.data.champion.local.ChampionDao
import com.venom7t.lolguide.data.common.local.LolGuideDatabase
import com.venom7t.lolguide.data.favourite.local.FavouriteChampionDao
import com.venom7t.lolguide.data.item.local.ItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LolGuideDatabase =
        Room.databaseBuilder(context, LolGuideDatabase::class.java, LolGuideDatabase.NAME)
            // Destructive fallback was acceptable in Phase 0 when the only
            // content was a rebuildable CDN cache. Favourites are user-authored
            // and cannot be re-downloaded, so schema changes migrate properly
            // from here on.
            .addMigrations(
                LolGuideDatabase.MIGRATION_1_2,
                LolGuideDatabase.MIGRATION_2_3,
            )
            .build()

    @Provides
    fun provideChampionDao(database: LolGuideDatabase): ChampionDao = database.championDao()

    @Provides
    fun provideFavouriteChampionDao(database: LolGuideDatabase): FavouriteChampionDao =
        database.favouriteChampionDao()

    @Provides
    fun provideItemDao(database: LolGuideDatabase): ItemDao = database.itemDao()
}
