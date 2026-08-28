package com.venom7t.lolguide.domain.status.repository

import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.status.model.ServerStatus

interface ServerStatusRepository {
    suspend fun getStatus(region: Region): Result<ServerStatus>
}
