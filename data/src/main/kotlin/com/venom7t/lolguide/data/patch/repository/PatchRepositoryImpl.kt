package com.venom7t.lolguide.data.patch.repository

import com.venom7t.lolguide.data.champion.remote.DataDragonApi
import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.common.toAppError
import com.venom7t.lolguide.data.patch.local.PatchLocalDataSource
import com.venom7t.lolguide.domain.common.AppError
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.patch.repository.PatchRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatchRepositoryImpl @Inject constructor(
    private val api: DataDragonApi,
    private val local: PatchLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PatchRepository {

    override suspend fun getCachedPatch(): String? = withContext(ioDispatcher) {
        local.getPatch()
    }

    override suspend fun refreshPatch(): Result<String> = withContext(ioDispatcher) {
        runCatchingCancellable {
            val versions = api.getVersions()

            // versions.json is ordered newest-first. An empty array means Riot
            // served something unusable; persisting "" would poison every
            // later request with a blank version segment.
            val current = versions.firstOrNull()?.takeIf { it.isNotBlank() }
                ?: throw AppError.Serialization("versions.json contained no versions")

            local.setPatch(current)
            Timber.d("Resolved current patch: %s", current)
            current
        }.recoverCatching { throwable ->
            throw throwable.toAppError()
        }
    }
}
