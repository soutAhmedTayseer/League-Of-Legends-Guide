package com.venom7t.lolguide

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.venom7t.lolguide.worker.PatchSyncScheduler
import dagger.hilt.android.HiltAndroidApp
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
    }
}
