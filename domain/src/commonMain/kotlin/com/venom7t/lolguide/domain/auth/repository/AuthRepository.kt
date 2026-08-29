package com.venom7t.lolguide.domain.auth.repository

import com.venom7t.lolguide.domain.auth.model.Account
import kotlinx.coroutines.flow.Flow

/**
 * The sync identity. Every install gets an anonymous UID with no sign-up
 * flow (Phase 5), and can optionally upgrade that to a real Google identity
 * (Phase 5 addendum) without losing anything synced under the anonymous UID
 * -- see [linkOrSignInWithGoogle]'s doc comment on why that distinction
 * matters.
 */
interface AuthRepository {

    /** Signs in anonymously if not already signed in. Idempotent. */
    suspend fun ensureSignedIn(): Result<String>

    /** Null if [ensureSignedIn] has not succeeded yet this process. */
    fun currentUserId(): String?

    /** The current identity, updating live across sign-in/link/sign-out. */
    fun observeAccount(): Flow<Account?>

    /**
     * Upgrades the current anonymous session to a Google identity, or signs
     * into an existing Google account if the anonymous link fails because
     * that Google account is already tied to a different Firebase user
     * (Firebase's `ERROR_CREDENTIAL_ALREADY_IN_USE`).
     *
     * Linking rather than plain sign-in is what keeps the data synced under
     * today's anonymous UID: a bare `signInWithCredential` would switch the
     * app to a *different* UID and the favourites/followed-summoners synced
     * so far would appear to vanish, still sitting under the old anonymous
     * document the user can no longer reach.
     */
    suspend fun linkOrSignInWithGoogle(idToken: String): Result<Account>

    /**
     * Drops back to a fresh anonymous session. Does not delete any data --
     * Firestore documents under the previous UID are simply no longer
     * reachable from this device once signed out, same as any other
     * Firebase Auth sign-out.
     */
    suspend fun signOut(): Result<Unit>
}
