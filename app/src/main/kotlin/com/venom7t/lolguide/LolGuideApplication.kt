package com.venom7t.lolguide

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkerFactory
import com.venom7t.lolguide.di.APPLICATION_SCOPE
import com.venom7t.lolguide.di.appModule
import com.venom7t.lolguide.di.databaseModule
import com.venom7t.lolguide.di.networkModule
import com.venom7t.lolguide.domain.di.DomainModule
import com.venom7t.lolguide.domain.sync.usecase.SyncOnStartUseCase
import com.venom7t.lolguide.presentation.di.PresentationModule
import com.venom7t.lolguide.worker.LpTrackerScheduler
import com.venom7t.lolguide.worker.PatchSyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import timber.log.Timber

class LolGuideApplication : Application(), Configuration.Provider {

    // Read lazily: WorkManager's own lazy init (triggered the first time
    // something calls WorkManager.getInstance(), e.g. inside the schedulers
    // below) queries this getter, and that always happens after startKoin()
    // has already run in onCreate() -- so the Koin-provided WorkerFactory is
    // guaranteed to exist by the time this is evaluated.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(get<WorkerFactory>())
            .build()

    override fun onCreate() {
        super.onCreate()

        // Debug only: a release build should not be writing request URLs and
        // cache hits to logcat.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@LolGuideApplication)
            workManagerFactory()
            modules(
                appModule,
                databaseModule,
                networkModule,
                DomainModule().module,
                PresentationModule().module,
            )
        }

        get<PatchSyncScheduler>().schedule()
        get<LpTrackerScheduler>().schedule()

        // Phase 5: pull remote favourites/followed-summoners once per
        // process start. Off the main thread and best-effort -- see
        // SyncOnStartUseCase's doc comment on why a failure here is
        // swallowed rather than surfaced.
        val applicationScope: CoroutineScope = get(qualifier = APPLICATION_SCOPE)
        val syncOnStart: SyncOnStartUseCase = get()
        applicationScope.launch { syncOnStart() }
    }
}
