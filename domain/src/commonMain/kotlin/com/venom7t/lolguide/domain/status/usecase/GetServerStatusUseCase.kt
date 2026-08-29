package com.venom7t.lolguide.domain.status.usecase

import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.status.model.ServerStatus
import com.venom7t.lolguide.domain.status.repository.ServerStatusRepository
import javax.inject.Inject

class GetServerStatusUseCase @Inject constructor(
    private val repository: ServerStatusRepository,
) {
    suspend operator fun invoke(region: Region): Result<ServerStatus> = repository.getStatus(region)
}
