package com.venom7t.lolguide.domain.auth.usecase

import com.venom7t.lolguide.domain.auth.model.Account
import com.venom7t.lolguide.domain.auth.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAccountUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<Account?> = repository.observeAccount()
}

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(idToken: String): Result<Account> =
        repository.linkOrSignInWithGoogle(idToken)
}

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.signOut()
}
