package com.venom7t.lolguide.domain.auth.usecase

import com.venom7t.lolguide.domain.auth.repository.AuthRepository
import javax.inject.Inject

class EnsureSignedInUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Result<String> = repository.ensureSignedIn()
}
