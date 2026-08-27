package com.example.lolguide.domain.patch.repository

/**
 * Resolves which patch the app is showing data for.
 *
 * This exists because the previous implementation hardcoded `12.6.1` into the
 * endpoint path and therefore served champion data years out of date. Nothing
 * in this codebase may name a patch version literally; it comes from here
 * (AGENTS.md §1).
 */
interface PatchRepository {

    /**
     * The last patch we successfully resolved, or null on a first run.
     *
     * Reading this is cheap and never touches the network, so the UI can paint
     * cached content immediately instead of blocking on `versions.json`.
     */
    suspend fun getCachedPatch(): String?

    /**
     * Fetches the current patch from Data Dragon and persists it.
     *
     * Returns the resolved version. Callers compare it against
     * [getCachedPatch] to decide whether the champion cache is now stale.
     */
    suspend fun refreshPatch(): Result<String>
}
