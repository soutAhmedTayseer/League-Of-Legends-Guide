package com.venom7t.lolguide.data.match.repository

import com.venom7t.lolguide.data.common.di.IoDispatcher
import com.venom7t.lolguide.data.common.toAppError
import com.venom7t.lolguide.data.match.local.MatchDao
import com.venom7t.lolguide.data.match.local.MatchEntity
import com.venom7t.lolguide.data.match.mapper.toDetail
import com.venom7t.lolguide.data.match.mapper.toDomain
import com.venom7t.lolguide.data.match.mapper.toSummary
import com.venom7t.lolguide.data.riot.remote.RiotApi
import com.venom7t.lolguide.data.riot.remote.RiotApiUrls
import com.venom7t.lolguide.data.riot.remote.dto.MatchDto
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.match.model.MatchDetail
import com.venom7t.lolguide.domain.match.model.MatchSummary
import com.venom7t.lolguide.domain.match.model.MatchTimelineFrame
import com.venom7t.lolguide.domain.match.repository.MatchRepository
import com.venom7t.lolguide.domain.onboarding.model.Region
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber

class MatchRepositoryImpl constructor(
    private val api: RiotApi,
    private val dao: MatchDao,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MatchRepository {

    override suspend fun getMatchHistory(
        puuid: String,
        region: Region,
        count: Int,
        startIndex: Int,
    ): Result<List<MatchSummary>> = withContext(ioDispatcher) {
        runCatchingCancellable {
            val matchIds = api.getMatchIdsByPuuid(
                url = RiotApiUrls.matchIdsByPuuid(region.regionalRoute, puuid),
                count = count,
                start = startIndex,
            )

            // Each match is fetched (cache-first) in parallel rather than
            // sequentially -- a 20-match page issued one request at a time
            // would take noticeably longer without buying any correctness,
            // and 20 concurrent requests is well inside the 20 req/s dev-key
            // limit (AGENTS.md section 8.3).
            coroutineScope {
                matchIds.map { matchId ->
                    async { fetchMatchCached(matchId, region) }
                }.awaitAll()
            }.mapNotNull { (dto, wasCached) -> dto?.toSummary(puuid, wasCached) }
        }.recoverCatching { throwable -> throw throwable.toAppError() }
    }

    override suspend fun getMatchDetail(matchId: String, region: Region): Result<MatchDetail> =
        withContext(ioDispatcher) {
            runCatchingCancellable {
                val (dto, _) = fetchMatchCached(matchId, region)
                (dto ?: throw IllegalStateException("Match $matchId could not be fetched")).toDetail()
            }.recoverCatching { throwable -> throw throwable.toAppError() }
        }

    override suspend fun getMatchTimeline(
        matchId: String,
        region: Region,
    ): Result<List<MatchTimelineFrame>> = withContext(ioDispatcher) {
        runCatchingCancellable {
            // Timelines are not cached: they are large, requested far less
            // often than the match itself, and this repository's cache
            // contract is specifically about the immutable match payload.
            api.getMatchTimeline(RiotApiUrls.matchTimeline(region.regionalRoute, matchId)).toDomain()
        }.recoverCatching { throwable -> throw throwable.toAppError() }
    }

    /**
     * Cache-first fetch of one match. A match, once finished, cannot change
     * -- so a cache hit is returned without ever touching the network
     * (AGENTS.md section 8.3).
     */
    private suspend fun fetchMatchCached(matchId: String, region: Region): Pair<MatchDto?, Boolean> {
        val cached = dao.getById(matchId)
        if (cached != null) {
            val decoded = runCatching { json.decodeFromString(MatchDto.serializer(), cached.payloadJson) }
                .onFailure { Timber.w(it, "Failed to decode cached match %s", matchId) }
                .getOrNull()
            if (decoded != null) return decoded to true
            // A corrupt row falls through to a fresh network fetch rather
            // than failing the whole page over one bad cache entry.
        }

        val dto = api.getMatch(RiotApiUrls.match(region.regionalRoute, matchId))
        dao.insert(
            MatchEntity(
                matchId = matchId,
                payloadJson = json.encodeToString(MatchDto.serializer(), dto),
                cachedAtEpochMillis = System.currentTimeMillis(),
            )
        )
        return dto to false
    }
}
