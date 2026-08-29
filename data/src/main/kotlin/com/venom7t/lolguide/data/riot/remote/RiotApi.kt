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
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * The keyed Riot API.
 *
 * Riot splits requests across two host families that do not share a single
 * base URL -- platform routing (`na1.api.riotgames.com`, ...) and regional
 * routing (`americas.api.riotgames.com`, ...) -- and which host a call needs
 * depends on the endpoint, not on any URL path segment. Every method here
 * takes the fully resolved absolute URL instead of a fixed base plus a
 * relative path; callers (the repositories) build it from `Region.platformId`
 * or `Region.regionalRoute` (Phase 4 plan §Region routing).
 */
class RiotApi(private val client: HttpClient) {

    suspend fun getAccountByRiotId(url: String): AccountDto = client.get(url).body()

    /** The reverse lookup of [getAccountByRiotId] -- puuid to Riot id, for payloads that only carry a puuid. */
    suspend fun getAccountByPuuid(url: String): AccountDto = client.get(url).body()

    suspend fun getSummonerByPuuid(url: String): SummonerDto = client.get(url).body()

    suspend fun getLeagueEntriesBySummoner(url: String): List<LeagueEntryDto> = client.get(url).body()

    suspend fun getMatchIdsByPuuid(url: String, count: Int, start: Int): List<String> =
        client.get(url) {
            parameter("count", count)
            parameter("start", start)
        }.body()

    suspend fun getMatch(url: String): MatchDto = client.get(url).body()

    suspend fun getMatchTimeline(url: String): MatchTimelineDto = client.get(url).body()

    suspend fun getActiveGameByPuuid(url: String): SpectatorGameDto = client.get(url).body()

    suspend fun getChampionMasteries(url: String): List<ChampionMasteryDto> = client.get(url).body()

    suspend fun getChampionRotation(url: String): ChampionRotationDto = client.get(url).body()

    suspend fun getChallengerLeague(url: String): LeagueListDto = client.get(url).body()

    suspend fun getPlatformStatus(url: String): PlatformStatusDto = client.get(url).body()

    /** Empty list means "not registered for any Clash team right now" -- a normal outcome. */
    suspend fun getClashPlayersBySummoner(url: String): List<ClashPlayerDto> = client.get(url).body()

    suspend fun getClashTeam(url: String): ClashTeamDto = client.get(url).body()

    suspend fun getClashTournament(url: String): ClashTournamentDto = client.get(url).body()
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
