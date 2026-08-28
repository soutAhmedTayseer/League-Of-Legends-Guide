package com.venom7t.lolguide

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.venom7t.lolguide.data.common.di.ApplicationScope
import com.venom7t.lolguide.domain.sync.usecase.SyncOnStartUseCase
import com.venom7t.lolguide.worker.LpTrackerScheduler
import com.venom7t.lolguide.worker.PatchSyncScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class LolGuideApplication : Application(), Configuration.Provider {

    // Lets WorkManager construct workers through Hilt, so a Worker (e.g.
    // PatchSyncWorker) can take injected dependencies in its constructor
    // exactly like anything else in the app, rather than resolving them
    // through a manual service-locator inside doWork().
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var patchSyncScheduler: PatchSyncScheduler

    @Inject
    lateinit var lpTrackerScheduler: LpTrackerScheduler

    @Inject
    lateinit var syncOnStart: SyncOnStartUseCase

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Debug only: a release build should not be writing request URLs and
        // cache hits to logcat.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        patchSyncScheduler.schedule()
        lpTrackerScheduler.schedule()

        // Phase 5: pull remote favourites/followed-summoners once per
        // process start. Off the main thread and best-effort -- see
        // SyncOnStartUseCase's doc comment on why a failure here is
        // swallowed rather than surfaced.
        applicationScope.launch { syncOnStart() }
    }
}
