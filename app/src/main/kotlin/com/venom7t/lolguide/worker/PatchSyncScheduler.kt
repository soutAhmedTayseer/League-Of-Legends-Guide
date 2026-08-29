package com.venom7t.lolguide.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.venom7t.lolguide.data.patch.worker.PatchSyncWorker
import java.util.concurrent.TimeUnit

/**
 * Schedules [PatchSyncWorker] to run roughly twice a day.
 *
 * League patches land on a predictable two-week cadence, so there is no
 * benefit to checking more often than a few times daily -- this exists to
 * have the cache warm by the time the user next opens the app, not to be a
 * live patch-change notifier.
 */
class PatchSyncScheduler(
    private val workManager: WorkManager,
) {

    fun schedule() {
        val request = PeriodicWorkRequestBuilder<PatchSyncWorker>(
            repeatInterval = 12,
            repeatIntervalTimeUnit = TimeUnit.HOURS,
        )
            .setConstraints(
                Constraints.Builder()
                    // Respects the user's data saver / metered network
                    // choice, since this work is invisible to them and
                    // therefore must not be the reason their data cap runs out.
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            PatchSyncWorker.UNIQUE_WORK_NAME,
            // KEEP: if a sync is already scheduled, a fresh app launch should
            // not reset its timer -- that would mean the worker could be
            // perpetually postponed by frequent app opens and never actually
            // run.
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
