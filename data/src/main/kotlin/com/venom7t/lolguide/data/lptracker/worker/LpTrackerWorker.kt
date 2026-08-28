package com.venom7t.lolguide.data.lptracker.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.venom7t.lolguide.domain.lptracker.usecase.LpChange
import com.venom7t.lolguide.domain.lptracker.usecase.PollFollowedSummonersLpUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Periodic background poll: records an LP snapshot for every followed
 * summoner's ranked queues, and hands whatever changed since the previous
 * poll to [LpChangeNotifier].
 *
 * Like [com.venom7t.lolguide.data.patch.worker.PatchSyncWorker], failures
 * here are silent to the user -- this is opportunistic background work with
 * no UI context to surface an error into. A retry on the next scheduled run
 * is enough; there is no user-facing "sync failed" state for this feature
 * the way there is for a user-initiated screen load.
 */
@HiltWorker
class LpTrackerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val pollFollowedSummonersLp: PollFollowedSummonersLpUseCase,
    private val notifier: LpChangeNotifier,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val changes: List<LpChange> = try {
            pollFollowedSummonersLp()
        } catch (throwable: Throwable) {
            Timber.d(throwable, "LpTrackerWorker: poll failed, will retry on next run")
            return Result.retry()
        }

        changes.forEach { change -> notifier.notify(change) }
        Timber.d("LpTrackerWorker: recorded snapshots, %d change(s)", changes.size)
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "lp_tracker"
    }
}
