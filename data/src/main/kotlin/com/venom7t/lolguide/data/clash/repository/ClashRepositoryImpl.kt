package com.venom7t.lolguide.data.clash.repository

import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.common.toAppError
import com.venom7t.lolguide.data.riot.remote.RiotApi
import com.venom7t.lolguide.data.riot.remote.RiotApiUrls
import com.venom7t.lolguide.domain.clash.model.ClashTeam
import com.venom7t.lolguide.domain.clash.model.ClashTeamMember
import com.venom7t.lolguide.domain.clash.repository.ClashRepository
import com.venom7t.lolguide.domain.common.AppError
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.onboarding.model.Region
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClashRepositoryImpl @Inject constructor(
    private val api: RiotApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ClashRepository {

    override suspend fun getTeamForSummoner(summonerId: String, region: Region): Result<ClashTeam?> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                val players = try {
                    api.getClashPlayersBySummoner(
                        RiotApiUrls.clashPlayersByPuuid(region.platformId, summonerId),
                    )
                } catch (throwable: Throwable) {
                    val appError = throwable.toAppError()
                    // No registered team is Riot's normal answer between
                    // Clash tournaments, not a failure -- same treatment as
                    // LiveGameRepositoryImpl's "not in a game" 404.
                    if (appError is AppError.NotFound) return@runCatchingCancellable null
                    throw appError
                }
                val player = players.firstOrNull() ?: return@runCatchingCancellable null

                val team = api.getClashTeam(RiotApiUrls.clashTeam(region.platformId, player.teamId))
                val tournament = runCatching {
                    api.getClashTournament(
                        RiotApiUrls.clashTournament(region.platformId, team.tournamentId),
                    )
                }.getOrNull()
                val nextPhase = tournament?.schedule
                    ?.filter { !it.cancelled }
                    ?.minByOrNull { it.startTime }

                ClashTeam(
                    teamId = team.id,
                    name = team.name,
                    tier = team.tier,
                    members = team.players.map { ClashTeamMember(summonerId = it.summonerId, role = it.role) },
                    nextMatchEpochMillis = nextPhase?.startTime,
                )
            }.recoverCatching { throwable ->
                if (throwable is AppError) throw throwable
                throw throwable.toAppError()
            }
        }
}
