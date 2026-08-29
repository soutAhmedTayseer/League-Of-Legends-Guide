package com.venom7t.lolguide.domain.auth.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.auth.model.Account
import com.venom7t.lolguide.domain.auth.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

@Factory
class ObserveAccountUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<Account?> = repository.observeAccount()
}

@Factory
class SignInWithGoogleUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(idToken: String): Result<Account> =
        repository.linkOrSignInWithGoogle(idToken)
}

@Factory
class SignOutUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.signOut()
}
