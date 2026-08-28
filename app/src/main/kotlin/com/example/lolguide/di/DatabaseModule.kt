package com.example.lolguide.di

import android.content.Context
import androidx.room.Room
import com.example.lolguide.data.champion.local.ChampionDao
import com.example.lolguide.data.common.local.LolGuideDatabase
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
            // Acceptable only while everything stored is a rebuildable CDN
            // cache: on a schema change the app simply re-downloads. This must
            // be replaced with real migrations before Phase 1 adds favourites,
            // which are user-authored data that cannot be regenerated.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideChampionDao(database: LolGuideDatabase): ChampionDao = database.championDao()
}
