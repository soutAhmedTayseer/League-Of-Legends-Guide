package com.venom7t.lolguide.domain.auth.repository

/**
 * The sync identity. Anonymous only for this phase (Phase 5 plan) -- every
 * install gets a stable UID with no sign-up flow, which is enough to key
 * per-device Firestore sync. There is no email/Google sign-in yet.
 */
interface AuthRepository {

    /** Signs in anonymously if not already signed in. Idempotent. */
    suspend fun ensureSignedIn(): Result<String>

    /** Null if [ensureSignedIn] has not succeeded yet this process. */
    fun currentUserId(): String?
}
