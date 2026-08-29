package com.venom7t.lolguide.worker

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.venom7t.lolguide.data.lptracker.worker.LpTrackerWorker
import java.util.concurrent.TimeUnit

/**
 * Schedules [LpTrackerWorker] roughly every 30 minutes -- frequent enough
 * that "gained/lost LP" notifications feel timely, infrequent enough to stay
 * well clear of the 20 req/s / 100 req/2min dev-key rate limit even with a
 * handful of followed summoners (each poll costs one ACCOUNT-V1 +
 * SUMMONER-V4 + LEAGUE-V4 call per summoner, AGENTS.md section 8.3).
 */
class LpTrackerScheduler(
    private val workManager: WorkManager,
) {

    fun schedule() {
        val request = PeriodicWorkRequestBuilder<LpTrackerWorker>(
            repeatInterval = 30,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            LpTrackerWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
