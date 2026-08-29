package com.venom7t.lolguide.domain.auth.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.auth.repository.AuthRepository

@Factory
class EnsureSignedInUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Result<String> = repository.ensureSignedIn()
}
