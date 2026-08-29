package com.venom7t.lolguide.di

import android.content.Context
import androidx.work.WorkManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.venom7t.lolguide.data.auth.repository.AuthRepositoryImpl
import com.venom7t.lolguide.data.builds.repository.SavedBuildRepositoryImpl
import com.venom7t.lolguide.data.champion.repository.ChampionRepositoryImpl
import com.venom7t.lolguide.data.clash.repository.ClashRepositoryImpl
import com.venom7t.lolguide.data.favourite.repository.FavouritesRepositoryImpl
import com.venom7t.lolguide.data.game.repository.GameProgressRepositoryImpl
import com.venom7t.lolguide.data.lptracker.repository.LpTrackerRepositoryImpl
import com.venom7t.lolguide.data.sync.repository.SyncRepositoryImpl
import com.venom7t.lolguide.data.followed.repository.FollowedSummonerRepositoryImpl
import com.venom7t.lolguide.data.item.repository.ItemRepositoryImpl
import com.venom7t.lolguide.data.ladder.repository.LadderRepositoryImpl
import com.venom7t.lolguide.data.livegame.repository.LiveGameRepositoryImpl
import com.venom7t.lolguide.data.mastery.repository.MasteryRepositoryImpl
import com.venom7t.lolguide.data.match.repository.MatchRepositoryImpl
import com.venom7t.lolguide.data.onboarding.repository.OnboardingRepositoryImpl
import com.venom7t.lolguide.data.patch.repository.PreviousPatchSnapshotRepositoryImpl
import com.venom7t.lolguide.data.rotation.repository.RotationRepositoryImpl
import com.venom7t.lolguide.data.rune.repository.RuneRepositoryImpl
import com.venom7t.lolguide.data.settings.repository.SettingsRepositoryImpl
import com.venom7t.lolguide.data.spell.repository.SummonerSpellRepositoryImpl
import com.venom7t.lolguide.data.status.repository.ServerStatusRepositoryImpl
import com.venom7t.lolguide.data.summoner.repository.RecentSearchRepositoryImpl
import com.venom7t.lolguide.data.summoner.repository.SummonerRepositoryImpl
import com.venom7t.lolguide.data.voiceline.repository.VoiceLineRepositoryImpl
import com.venom7t.lolguide.data.common.di.ApplicationScope
import com.venom7t.lolguide.data.common.di.DefaultDispatcher
import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.patch.repository.PatchRepositoryImpl
import com.venom7t.lolguide.domain.auth.repository.AuthRepository
import com.venom7t.lolguide.domain.builds.repository.SavedBuildRepository
import com.venom7t.lolguide.domain.champion.repository.ChampionRepository
import com.venom7t.lolguide.domain.clash.repository.ClashRepository
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.favourite.repository.FavouritesRepository
import com.venom7t.lolguide.domain.game.repository.GameProgressRepository
import com.venom7t.lolguide.domain.lptracker.repository.LpTrackerRepository
import com.venom7t.lolguide.domain.sync.repository.SyncRepository
import com.venom7t.lolguide.domain.followed.repository.FollowedSummonerRepository
import com.venom7t.lolguide.domain.item.repository.ItemRepository
import com.venom7t.lolguide.domain.ladder.repository.LadderRepository
import com.venom7t.lolguide.domain.livegame.repository.LiveGameRepository
import com.venom7t.lolguide.domain.mastery.repository.MasteryRepository
import com.venom7t.lolguide.domain.match.repository.MatchRepository
import com.venom7t.lolguide.domain.onboarding.repository.OnboardingRepository
import com.venom7t.lolguide.domain.patch.repository.PreviousPatchSnapshotRepository
import com.venom7t.lolguide.domain.rotation.repository.RotationRepository
import com.venom7t.lolguide.domain.rune.repository.RuneRepository
import com.venom7t.lolguide.domain.settings.repository.SettingsRepository
import com.venom7t.lolguide.domain.spell.repository.SummonerSpellRepository
import com.venom7t.lolguide.domain.status.repository.ServerStatusRepository
import com.venom7t.lolguide.domain.summoner.repository.RecentSearchRepository
import com.venom7t.lolguide.domain.summoner.repository.SummonerRepository
import com.venom7t.lolguide.domain.voiceline.repository.VoiceLineRepository
import com.venom7t.lolguide.domain.patch.repository.PatchRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.Locale
import javax.inject.Singleton

/**
 * Binds data-layer implementations to the domain interfaces they satisfy.
 *
 * `:app` is the only module that sees both sides, which is what keeps
 * `:presentation` from being able to import a repository implementation
 * (AGENTS.md §3).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindChampionRepository(impl: ChampionRepositoryImpl): ChampionRepository

    @Binds
    @Singleton
    abstract fun bindPatchRepository(impl: PatchRepositoryImpl): PatchRepository

    @Binds
    @Singleton
    abstract fun bindFavouritesRepository(impl: FavouritesRepositoryImpl): FavouritesRepository

    @Binds
    @Singleton
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository

    @Binds
    @Singleton
    abstract fun bindRuneRepository(impl: RuneRepositoryImpl): RuneRepository

    @Binds
    @Singleton
    abstract fun bindSummonerSpellRepository(
        impl: SummonerSpellRepositoryImpl,
    ): SummonerSpellRepository

    @Binds
    @Singleton
    abstract fun bindPreviousPatchSnapshotRepository(
        impl: PreviousPatchSnapshotRepositoryImpl,
    ): PreviousPatchSnapshotRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(impl: OnboardingRepositoryImpl): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindVoiceLineRepository(impl: VoiceLineRepositoryImpl): VoiceLineRepository

    @Binds
    @Singleton
    abstract fun bindSummonerRepository(impl: SummonerRepositoryImpl): SummonerRepository

    @Binds
    @Singleton
    abstract fun bindRecentSearchRepository(impl: RecentSearchRepositoryImpl): RecentSearchRepository

    @Binds
    @Singleton
    abstract fun bindMatchRepository(impl: MatchRepositoryImpl): MatchRepository

    @Binds
    @Singleton
    abstract fun bindLiveGameRepository(impl: LiveGameRepositoryImpl): LiveGameRepository

    @Binds
    @Singleton
    abstract fun bindMasteryRepository(impl: MasteryRepositoryImpl): MasteryRepository

    @Binds
    @Singleton
    abstract fun bindLadderRepository(impl: LadderRepositoryImpl): LadderRepository

    @Binds
    @Singleton
    abstract fun bindServerStatusRepository(impl: ServerStatusRepositoryImpl): ServerStatusRepository

    @Binds
    @Singleton
    abstract fun bindRotationRepository(impl: RotationRepositoryImpl): RotationRepository

    @Binds
    @Singleton
    abstract fun bindFollowedSummonerRepository(
        impl: FollowedSummonerRepositoryImpl,
    ): FollowedSummonerRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    @Binds
    @Singleton
    abstract fun bindLpTrackerRepository(impl: LpTrackerRepositoryImpl): LpTrackerRepository

    @Binds
    @Singleton
    abstract fun bindClashRepository(impl: ClashRepositoryImpl): ClashRepository

    @Binds
    @Singleton
    abstract fun bindGameProgressRepository(impl: GameProgressRepositoryImpl): GameProgressRepository

    @Binds
    @Singleton
    abstract fun bindSavedBuildRepository(impl: SavedBuildRepositoryImpl): SavedBuildRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): kotlinx.coroutines.CoroutineScope =
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + ioDispatcher)
}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private const val PREFERENCES_NAME = "lol_guide_preferences"

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile(PREFERENCES_NAME)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): com.google.firebase.auth.FirebaseAuth =
        com.google.firebase.auth.FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): com.google.firebase.firestore.FirebaseFirestore =
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
object LocaleModule {

    /**
     * The locale to request content in.
     *
     * Resolved from the device language so Data Dragon serves Arabic lore and
     * ability text to an Arabic device, rather than an Arabic UI wrapped
     * around English game text (AGENTS.md §10).
     *
     * Not a singleton: an in-app language switch recreates the activity, and
     * this must be re-read rather than frozen at process start.
     */
    @Provides
    fun provideAppLocale(): AppLocale =
        AppLocale.fromLanguageTag(Locale.getDefault().language)
}
