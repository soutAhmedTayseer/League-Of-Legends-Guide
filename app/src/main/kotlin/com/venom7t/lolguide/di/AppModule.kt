package com.venom7t.lolguide.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.venom7t.lolguide.data.auth.repository.AuthRepositoryImpl
import com.venom7t.lolguide.data.builds.repository.SavedBuildRepositoryImpl
import com.venom7t.lolguide.data.champion.repository.ChampionRepositoryImpl
import com.venom7t.lolguide.data.clash.repository.ClashRepositoryImpl
import com.venom7t.lolguide.data.favourite.repository.FavouritesRepositoryImpl
import com.venom7t.lolguide.data.followed.repository.FollowedSummonerRepositoryImpl
import com.venom7t.lolguide.data.game.repository.GameProgressRepositoryImpl
import com.venom7t.lolguide.data.item.repository.ItemRepositoryImpl
import com.venom7t.lolguide.data.lptracker.worker.LpChangeNotifier
import com.venom7t.lolguide.data.lptracker.worker.LpTrackerWorker
import com.venom7t.lolguide.data.patch.worker.PatchSyncWorker
import com.venom7t.lolguide.data.ladder.repository.LadderRepositoryImpl
import com.venom7t.lolguide.data.livegame.repository.LiveGameRepositoryImpl
import com.venom7t.lolguide.data.lptracker.repository.LpTrackerRepositoryImpl
import com.venom7t.lolguide.data.mastery.repository.MasteryRepositoryImpl
import com.venom7t.lolguide.data.match.repository.MatchRepositoryImpl
import com.venom7t.lolguide.data.onboarding.repository.OnboardingRepositoryImpl
import com.venom7t.lolguide.data.patch.local.PatchLocalDataSource
import com.venom7t.lolguide.data.patch.repository.PatchRepositoryImpl
import com.venom7t.lolguide.data.patch.repository.PreviousPatchSnapshotRepositoryImpl
import com.venom7t.lolguide.data.rotation.repository.RotationRepositoryImpl
import com.venom7t.lolguide.data.rune.repository.RuneRepositoryImpl
import com.venom7t.lolguide.data.settings.repository.SettingsRepositoryImpl
import com.venom7t.lolguide.data.spell.repository.SummonerSpellRepositoryImpl
import com.venom7t.lolguide.data.status.repository.ServerStatusRepositoryImpl
import com.venom7t.lolguide.data.summoner.repository.RecentSearchRepositoryImpl
import com.venom7t.lolguide.data.summoner.repository.SummonerRepositoryImpl
import com.venom7t.lolguide.data.sync.repository.SyncRepositoryImpl
import com.venom7t.lolguide.data.voiceline.remote.VoiceLineProbe
import com.venom7t.lolguide.data.voiceline.repository.VoiceLineRepositoryImpl
import com.venom7t.lolguide.domain.auth.repository.AuthRepository
import com.venom7t.lolguide.domain.builds.repository.SavedBuildRepository
import com.venom7t.lolguide.domain.champion.repository.ChampionRepository
import com.venom7t.lolguide.domain.clash.repository.ClashRepository
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.favourite.repository.FavouritesRepository
import com.venom7t.lolguide.domain.followed.repository.FollowedSummonerRepository
import com.venom7t.lolguide.domain.game.repository.GameProgressRepository
import com.venom7t.lolguide.domain.item.repository.ItemRepository
import com.venom7t.lolguide.domain.ladder.repository.LadderRepository
import com.venom7t.lolguide.domain.livegame.repository.LiveGameRepository
import com.venom7t.lolguide.domain.lptracker.repository.LpTrackerRepository
import com.venom7t.lolguide.domain.mastery.repository.MasteryRepository
import com.venom7t.lolguide.domain.match.repository.MatchRepository
import com.venom7t.lolguide.domain.onboarding.repository.OnboardingRepository
import com.venom7t.lolguide.domain.patch.repository.PatchRepository
import com.venom7t.lolguide.domain.patch.repository.PreviousPatchSnapshotRepository
import com.venom7t.lolguide.domain.rotation.repository.RotationRepository
import com.venom7t.lolguide.domain.rune.repository.RuneRepository
import com.venom7t.lolguide.domain.settings.repository.SettingsRepository
import com.venom7t.lolguide.domain.spell.repository.SummonerSpellRepository
import com.venom7t.lolguide.domain.status.repository.ServerStatusRepository
import com.venom7t.lolguide.domain.summoner.repository.RecentSearchRepository
import com.venom7t.lolguide.domain.summoner.repository.SummonerRepository
import com.venom7t.lolguide.domain.sync.repository.SyncRepository
import com.venom7t.lolguide.domain.voiceline.repository.VoiceLineRepository
import com.venom7t.lolguide.navigation.AppStartViewModel
import com.venom7t.lolguide.worker.LpTrackerScheduler
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import com.venom7t.lolguide.worker.PatchSyncScheduler
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.Locale

private const val PREFERENCES_NAME = "lol_guide_preferences"

val IO_DISPATCHER = named("io")
val DEFAULT_DISPATCHER = named("default")
val APPLICATION_SCOPE = named("applicationScope")

/**
 * Binds data-layer implementations to the domain interfaces they satisfy.
 *
 * `:app` is the only module that sees both sides, which is what keeps
 * `:presentation` from being able to import a repository implementation
 * (AGENTS.md §3).
 */
val appModule = module {

    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile(PREFERENCES_NAME)
        }
    }

    single(IO_DISPATCHER) { Dispatchers.IO as CoroutineDispatcher }
    single(DEFAULT_DISPATCHER) { Dispatchers.Default as CoroutineDispatcher }
    single(APPLICATION_SCOPE) {
        CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>(IO_DISPATCHER))
    }

    single { Firebase.auth }
    single { Firebase.firestore }

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
    factory { AppLocale.fromLanguageTag(Locale.getDefault().language) }

    single { WorkManager.getInstance(androidContext()) }

    single { PatchLocalDataSource(get()) }
    single { VoiceLineProbe(get(DATA_DRAGON_RETROFIT), get(IO_DISPATCHER)) }

    single<ChampionRepository> { ChampionRepositoryImpl(get(), get(), get(), get(IO_DISPATCHER)) }
    single<PatchRepository> { PatchRepositoryImpl(get(), get(), get(IO_DISPATCHER)) }
    single<FavouritesRepository> {
        FavouritesRepositoryImpl(get(), get(), get(IO_DISPATCHER), get(APPLICATION_SCOPE))
    }
    single<ItemRepository> { ItemRepositoryImpl(get(), get(), get(), get(IO_DISPATCHER)) }
    single<RuneRepository> { RuneRepositoryImpl(get(), get(IO_DISPATCHER)) }
    single<SummonerSpellRepository> { SummonerSpellRepositoryImpl(get(), get(IO_DISPATCHER)) }
    single<PreviousPatchSnapshotRepository> {
        PreviousPatchSnapshotRepositoryImpl(get(), get(), get(IO_DISPATCHER))
    }
    single<OnboardingRepository> { OnboardingRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<VoiceLineRepository> { VoiceLineRepositoryImpl(get()) }
    single<SummonerRepository> { SummonerRepositoryImpl(get(), get(IO_DISPATCHER)) }
    single<RecentSearchRepository> { RecentSearchRepositoryImpl(get()) }
    single<MatchRepository> { MatchRepositoryImpl(get(), get(), get(), get(IO_DISPATCHER)) }
    single<LiveGameRepository> { LiveGameRepositoryImpl(get(), get(IO_DISPATCHER)) }
    single<MasteryRepository> { MasteryRepositoryImpl(get(), get(IO_DISPATCHER)) }
    single<LadderRepository> { LadderRepositoryImpl(get(), get(IO_DISPATCHER)) }
    single<ServerStatusRepository> { ServerStatusRepositoryImpl(get(), get(IO_DISPATCHER)) }
    single<RotationRepository> { RotationRepositoryImpl(get(), get(IO_DISPATCHER)) }
    single<FollowedSummonerRepository> {
        FollowedSummonerRepositoryImpl(get(), get(), get(IO_DISPATCHER), get(APPLICATION_SCOPE))
    }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<SyncRepository> { SyncRepositoryImpl(get(), get()) }
    single<LpTrackerRepository> { LpTrackerRepositoryImpl(get(), get(IO_DISPATCHER)) }
    single<ClashRepository> { ClashRepositoryImpl(get(), get(IO_DISPATCHER)) }
    single<GameProgressRepository> { GameProgressRepositoryImpl(get(), get()) }
    single<SavedBuildRepository> {
        SavedBuildRepositoryImpl(get(), get(), get(IO_DISPATCHER), get(APPLICATION_SCOPE))
    }

    single { PatchSyncScheduler(get()) }
    single { LpTrackerScheduler(get()) }
    single { LpChangeNotifier(androidContext()) }

    workerOf(::PatchSyncWorker)
    workerOf(::LpTrackerWorker)

    viewModel { AppStartViewModel(get(), get(), get(), get()) }
}
