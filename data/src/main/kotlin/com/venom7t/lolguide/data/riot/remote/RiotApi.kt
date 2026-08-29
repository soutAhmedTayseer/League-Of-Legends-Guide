package com.venom7t.lolguide.data.riot.remote

import com.venom7t.lolguide.data.riot.remote.dto.AccountDto
import com.venom7t.lolguide.data.riot.remote.dto.ChampionMasteryDto
import com.venom7t.lolguide.data.riot.remote.dto.ChampionRotationDto
import com.venom7t.lolguide.data.riot.remote.dto.ClashPlayerDto
import com.venom7t.lolguide.data.riot.remote.dto.ClashTeamDto
import com.venom7t.lolguide.data.riot.remote.dto.ClashTournamentDto
import com.venom7t.lolguide.data.riot.remote.dto.LeagueEntryDto
import com.venom7t.lolguide.data.riot.remote.dto.LeagueListDto
import com.venom7t.lolguide.data.riot.remote.dto.MatchDto
import com.venom7t.lolguide.data.riot.remote.dto.MatchTimelineDto
import com.venom7t.lolguide.data.riot.remote.dto.PlatformStatusDto
import com.venom7t.lolguide.data.riot.remote.dto.SpectatorGameDto
import com.venom7t.lolguide.data.riot.remote.dto.SummonerDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * The keyed Riot API.
 *
 * Riot splits requests across two host families that do not share a single
 * base URL -- platform routing (`na1.api.riotgames.com`, ...) and regional
 * routing (`americas.api.riotgames.com`, ...) -- and which host a call needs
 * depends on the endpoint, not on any URL path segment. Retrofit's
 * `baseUrl()` cannot express that, so every method here takes the fully
 * resolved absolute URL as `@Url` instead of a fixed base plus a relative
 * path; callers (the repositories) build it from `Region.platformId` or
 * `Region.regionalRoute` (Phase 4 plan §Region routing). The dummy base URL
 * configured on this Retrofit instance is never actually used for a request.
 */
interface RiotApi {

    @GET
    suspend fun getAccountByRiotId(@Url url: String): AccountDto

    /** The reverse lookup of [getAccountByRiotId] -- puuid to Riot id, for payloads that only carry a puuid. */
    @GET
    suspend fun getAccountByPuuid(@Url url: String): AccountDto

    @GET
    suspend fun getSummonerByPuuid(@Url url: String): SummonerDto

    @GET
    suspend fun getLeagueEntriesBySummoner(@Url url: String): List<LeagueEntryDto>

    @GET
    suspend fun getMatchIdsByPuuid(
        @Url url: String,
        @Query("count") count: Int,
        @Query("start") start: Int,
    ): List<String>

    @GET
    suspend fun getMatch(@Url url: String): MatchDto

    @GET
    suspend fun getMatchTimeline(@Url url: String): MatchTimelineDto

    @GET
    suspend fun getActiveGameByPuuid(@Url url: String): SpectatorGameDto

    @GET
    suspend fun getChampionMasteries(@Url url: String): List<ChampionMasteryDto>

    @GET
    suspend fun getChampionRotation(@Url url: String): ChampionRotationDto

    @GET
    suspend fun getChallengerLeague(@Url url: String): LeagueListDto

    @GET
    suspend fun getPlatformStatus(@Url url: String): PlatformStatusDto

    /** Empty list means "not registered for any Clash team right now" -- a normal outcome. */
    @GET
    suspend fun getClashPlayersBySummoner(@Url url: String): List<ClashPlayerDto>

    @GET
    suspend fun getClashTeam(@Url url: String): ClashTeamDto

    @GET
    suspend fun getClashTournament(@Url url: String): ClashTournamentDto
}

/**
 * Builds the absolute URLs [RiotApi] needs, since a single `baseUrl` cannot
 * express Riot's two-host-family routing (see [RiotApi]'s doc comment).
 */
object RiotApiUrls {

    fun accountByRiotId(regionalRoute: String, name: String, tagline: String): String =
        "https://$regionalRoute.api.riotgames.com/riot/account/v1/accounts/by-riot-id/" +
            "${name.encode()}/${tagline.encode()}"

    fun accountByPuuid(regionalRoute: String, puuid: String): String =
        "https://$regionalRoute.api.riotgames.com/riot/account/v1/accounts/by-puuid/$puuid"

    fun summonerByPuuid(platformId: String, puuid: String): String =
        "https://$platformId.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/$puuid"

    fun leagueEntriesBySummoner(platformId: String, summonerId: String): String =
        "https://$platformId.api.riotgames.com/lol/league/v4/entries/by-summoner/$summonerId"

    fun matchIdsByPuuid(regionalRoute: String, puuid: String): String =
        "https://$regionalRoute.api.riotgames.com/lol/match/v5/matches/by-puuid/$puuid/ids"

    fun match(regionalRoute: String, matchId: String): String =
        "https://$regionalRoute.api.riotgames.com/lol/match/v5/matches/$matchId"

    fun matchTimeline(regionalRoute: String, matchId: String): String =
        "https://$regionalRoute.api.riotgames.com/lol/match/v5/matches/$matchId/timeline"

    fun activeGameByPuuid(platformId: String, puuid: String): String =
        "https://$platformId.api.riotgames.com/lol/spectator/v5/active-games/by-summoner/$puuid"

    fun championMasteries(platformId: String, puuid: String): String =
        "https://$platformId.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-puuid/$puuid"

    fun championRotation(platformId: String): String =
        "https://$platformId.api.riotgames.com/lol/platform/v3/champion-rotations"

    fun challengerLeague(platformId: String, queue: String): String =
        "https://$platformId.api.riotgames.com/lol/league/v4/challengerleagues/by-queue/$queue"

    fun platformStatus(platformId: String): String =
        "https://$platformId.api.riotgames.com/lol/status/v4/platform-data"

    fun clashPlayersByPuuid(platformId: String, puuid: String): String =
        "https://$platformId.api.riotgames.com/lol/clash/v1/players/by-puuid/$puuid"

    fun clashTeam(platformId: String, teamId: String): String =
        "https://$platformId.api.riotgames.com/lol/clash/v1/teams/$teamId"

    fun clashTournament(platformId: String, tournamentId: Int): String =
        "https://$platformId.api.riotgames.com/lol/clash/v1/tournaments/$tournamentId"

    /** Riot IDs can contain spaces and non-ASCII characters and must be percent-encoded. */
    private fun String.encode(): String =
        java.net.URLEncoder.encode(this, "UTF-8").replace("+", "%20")
}
