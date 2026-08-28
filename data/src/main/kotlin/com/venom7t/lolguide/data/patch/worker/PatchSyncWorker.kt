package com.venom7t.lolguide.data.patch.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.venom7t.lolguide.domain.champion.usecase.RefreshChampionsUseCase
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.item.usecase.RefreshItemsUseCase
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Periodic background sync: checks `versions.json`, and if the patch changed,
 * refreshes the champion and item caches before the user opens the app.
 *
 * This is what makes offline-first (AGENTS.md §7.1, Phase 0) actually mean
 * something on a new patch: without it, the first app open after a patch
 * drops still has to make the user wait on a foreground network call. With
 * it, the cache is warm by the time they tap the icon.
 *
 * A failure here is silent to the user by design -- this is opportunistic
 * background work, not a user-initiated action, so there is nothing to show
 * an error for. The existing foreground refresh path (Phase 0's
 * ChampionListViewModel) is what surfaces a real failure if one occurs.
 */
@HiltWorker
class PatchSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val resolvePatch: ResolvePatchUseCase,
    private val refreshChampions: RefreshChampionsUseCase,
    private val refreshItems: RefreshItemsUseCase,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val patch = resolvePatch().getOrElse {
            Timber.d("PatchSyncWorker: could not resolve patch, will retry on next run")
            return Result.retry()
        }

        // Locale is fixed to English for the background sync regardless of
        // device language: the worker has no UI context to read a live
        // locale from, and pre-warming the wrong language's cache would not
        // help a user who switches languages between syncs. The foreground
        // repositories still refresh in the device's actual locale on demand.
        val locale = AppLocale.ENGLISH

        // Same gate the foreground ViewModels apply (Phase 0/2): only refresh
        // on a genuine patch change. A periodic worker that re-downloaded the
        // full champion and item lists on every run regardless of whether
        // anything changed would be pointless traffic (AGENTS.md section 8.3).
        if (!patch.didPatchChange) {
            Timber.d("PatchSyncWorker: patch %s unchanged, nothing to sync", patch.version)
            return Result.success()
        }

        val championsResult = refreshChampions(patch.version, locale)
        val itemsResult = refreshItems(patch.version, locale)

        return if (championsResult.isSuccess && itemsResult.isSuccess) {
            Timber.d("PatchSyncWorker: synced patch %s", patch.version)
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "patch_sync"
    }
}
