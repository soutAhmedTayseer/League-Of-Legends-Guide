package com.example.lolguide.di

import android.content.Context
import androidx.room.Room
import com.example.lolguide.data.champion.local.ChampionDao
import com.example.lolguide.data.common.local.LolGuideDatabase
import com.example.lolguide.data.favourite.local.FavouriteChampionDao
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
            .addMigrations(LolGuideDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideChampionDao(database: LolGuideDatabase): ChampionDao = database.championDao()

    @Provides
    fun provideFavouriteChampionDao(database: LolGuideDatabase): FavouriteChampionDao =
        database.favouriteChampionDao()
}
