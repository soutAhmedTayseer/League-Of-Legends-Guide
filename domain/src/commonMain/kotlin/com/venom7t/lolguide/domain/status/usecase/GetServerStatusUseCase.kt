package com.venom7t.lolguide.domain.status.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.status.model.ServerStatus
import com.venom7t.lolguide.domain.status.repository.ServerStatusRepository

@Factory
class GetServerStatusUseCase(
    private val repository: ServerStatusRepository,
) {
    suspend operator fun invoke(region: Region): Result<ServerStatus> = repository.getStatus(region)
}
