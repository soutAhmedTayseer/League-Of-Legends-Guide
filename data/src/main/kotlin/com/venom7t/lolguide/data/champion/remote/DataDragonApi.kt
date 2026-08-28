package com.venom7t.lolguide.data.champion.remote

import com.venom7t.lolguide.data.champion.remote.dto.ChampionDetailResponseDto
import com.venom7t.lolguide.data.champion.remote.dto.ChampionListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Data Dragon, Riot's static content CDN. No API key, no rate limit.
 *
 * Every path is parameterised by `version` and `locale`. There is deliberately
 * no overload that defaults the version: the previous implementation hardcoded
 * `cdn/12.6.1/` here and served champion data years out of date (AGENTS.md §1).
 */
interface DataDragonApi {

    /**
     * All published patches, newest first. Index 0 is the live patch.
     *
     * Note this is under `api/`, not `cdn/`, and is the one call that is not
     * version-parameterised -- it is how the version is discovered.
     */
    @GET("api/versions.json")
    suspend fun getVersions(): List<String>

    /**
     * Every champion, with base stats but without abilities or lore.
     *
     * @param locale a Data Dragon locale code such as `en_US` or `ar_AE`.
     */
    @GET("cdn/{version}/data/{locale}/champion.json")
    suspend fun getChampions(
        @Path("version") version: String,
        @Path("locale") locale: String,
    ): ChampionListResponseDto

    /**
     * One champion's abilities, lore and skin list.
     *
     * @param championId Data Dragon's string id (`MonkeyKing`, not `Wukong`).
     */
    @GET("cdn/{version}/data/{locale}/champion/{championId}.json")
    suspend fun getChampionDetail(
        @Path("version") version: String,
        @Path("locale") locale: String,
        @Path("championId") championId: String,
    ): ChampionDetailResponseDto
}
