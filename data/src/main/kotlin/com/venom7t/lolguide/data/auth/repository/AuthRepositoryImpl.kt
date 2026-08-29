package com.venom7t.lolguide.data.auth.repository

import com.venom7t.lolguide.domain.auth.model.Account
import com.venom7t.lolguide.domain.auth.repository.AuthRepository
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl constructor(
    private val firebaseAuth: FirebaseAuth,
) : AuthRepository {

    override suspend fun ensureSignedIn(): Result<String> = runCatchingCancellable {
        firebaseAuth.currentUser?.uid?.let { return@runCatchingCancellable it }
        val result = firebaseAuth.signInAnonymously()
        result.user?.uid ?: error("Anonymous sign-in returned no user")
    }

    override fun currentUserId(): String? = firebaseAuth.currentUser?.uid

    override fun observeAccount(): Flow<Account?> =
        firebaseAuth.authStateChanged.map { it?.toAccount() }

    override suspend fun linkOrSignInWithGoogle(idToken: String): Result<Account> =
        runCatchingCancellable {
            val credential = GoogleAuthProvider.credential(idToken, null)
            val currentUser = firebaseAuth.currentUser

            val user = if (currentUser != null && currentUser.isAnonymous) {
                // Preserves the UID everything is synced under -- see the
                // interface doc comment on why linking, not plain sign-in,
                // is the default path here.
                try {
                    currentUser.linkWithCredential(credential).user
                } catch (collision: FirebaseAuthUserCollisionException) {
                    // This Google account already owns a different Firebase
                    // user (e.g. they signed in with it on another device
                    // first). Falling back to a plain sign-in switches to
                    // *that* existing account rather than failing outright --
                    // the anonymous session's local-only data is what is left
                    // behind in that case, which is the documented, expected
                    // trade-off of a same-Google-account collision.
                    firebaseAuth.signInWithCredential(credential).user
                }
            } else {
                firebaseAuth.signInWithCredential(credential).user
            }

            user?.toAccount() ?: error("Google sign-in returned no user")
        }

    override suspend fun signOut(): Result<Unit> = runCatchingCancellable {
        firebaseAuth.signOut()
        // The rest of the app (SyncOnStartUseCase and every repository that
        // pushes best-effort) assumes there is always *some* signed-in user
        // -- re-establishing a fresh anonymous session immediately is what
        // keeps that assumption true after a sign-out, rather than leaving
        // the app in a signed-out state nothing else here expects.
        firebaseAuth.signInAnonymously()
        Unit
    }

    private fun FirebaseUser.toAccount() = Account(
        uid = uid,
        isAnonymous = isAnonymous,
        displayName = displayName,
        email = email,
    )
}
