package com.venom7t.lolguide.data.status.repository

import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.common.toAppError
import com.venom7t.lolguide.data.riot.remote.RiotApi
import com.venom7t.lolguide.data.riot.remote.RiotApiUrls
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.status.model.IncidentSeverity
import com.venom7t.lolguide.domain.status.model.ServerIncident
import com.venom7t.lolguide.domain.status.model.ServerStatus
import com.venom7t.lolguide.domain.status.repository.ServerStatusRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ServerStatusRepositoryImpl constructor(
    private val api: RiotApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ServerStatusRepository {

    override suspend fun getStatus(region: Region): Result<ServerStatus> = withContext(ioDispatcher) {
        runCatchingCancellable {
            val dto = api.getPlatformStatus(RiotApiUrls.platformStatus(region.platformId))
            ServerStatus(
                region = dto.name,
                incidents = dto.incidents.map { incident ->
                    ServerIncident(
                        id = incident.id,
                        // Riot ships every incident title translated; English
                        // is picked explicitly rather than trusting locale
                        // ordering, with the first available title as a
                        // fallback if English was not provided.
                        titleEn = incident.titles.firstOrNull { it.locale == AppLocale.ENGLISH.languageTag }
                            ?.content
                            ?: incident.titles.firstOrNull()?.content
                            ?: "",
                        severity = IncidentSeverity.fromRiotValue(incident.incidentSeverity),
                    )
                },
            )
        }.recoverCatching { throwable -> throw throwable.toAppError() }
    }
}
