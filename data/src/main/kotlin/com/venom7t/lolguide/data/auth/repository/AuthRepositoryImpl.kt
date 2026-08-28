package com.venom7t.lolguide.data.auth.repository

import com.google.firebase.auth.FirebaseAuth
import com.venom7t.lolguide.domain.auth.repository.AuthRepository
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : AuthRepository {

    override suspend fun ensureSignedIn(): Result<String> = runCatchingCancellable {
        firebaseAuth.currentUser?.uid?.let { return@runCatchingCancellable it }
        val result = firebaseAuth.signInAnonymously().await()
        result.user?.uid ?: error("Anonymous sign-in returned no user")
    }

    override fun currentUserId(): String? = firebaseAuth.currentUser?.uid
}
