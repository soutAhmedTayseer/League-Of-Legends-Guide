package com.venom7t.lolguide.domain.patch.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.patch.repository.PatchRepository

/**
 * The single source of the patch version every other call is parameterised by.
 *
 * Resolution order:
 *  1. Ask the network. If it answers, that is the truth and it is persisted.
 *  2. If the network fails but we have a cached version, use it and say so —
 *     the caller is expected to label the data as potentially stale.
 *  3. If both fail there is nothing honest to show, so this fails rather than
 *     inventing a version (AGENTS.md §1).
 */
@Factory
class ResolvePatchUseCase(
    private val patchRepository: PatchRepository,
) {

    suspend operator fun invoke(): Result<ResolvedPatch> {
        val cached = patchRepository.getCachedPatch()

        return patchRepository.refreshPatch().fold(
            onSuccess = { fresh ->
                Result.success(
                    ResolvedPatch(
                        version = fresh,
                        isStale = false,
                        previousVersion = cached.takeIf { it != fresh },
                    )
                )
            },
            onFailure = { error ->
                if (cached != null) {
                    Result.success(
                        ResolvedPatch(version = cached, isStale = true, previousVersion = null)
                    )
                } else {
                    Result.failure(error)
                }
            },
        )
    }
}

/**
 * @param version the patch all subsequent requests must use.
 * @param isStale true when this came from cache because the network failed;
 *   the UI must tell the user the data may be out of date.
 * @param previousVersion non-null when the patch changed this run, which means
 *   any cached champion data belongs to an older patch and must be refreshed.
 */
data class ResolvedPatch(
    val version: String,
    val isStale: Boolean,
    val previousVersion: String?,
) {
    val didPatchChange: Boolean get() = previousVersion != null
}
