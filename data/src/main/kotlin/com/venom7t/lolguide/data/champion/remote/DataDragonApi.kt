package com.venom7t.lolguide.data.champion.remote

import com.venom7t.lolguide.data.champion.remote.dto.ChampionDetailResponseDto
import com.venom7t.lolguide.data.champion.remote.dto.ChampionListResponseDto
import com.venom7t.lolguide.data.item.remote.dto.ItemListResponseDto
import com.venom7t.lolguide.data.rune.remote.dto.RuneTreeDto
import com.venom7t.lolguide.data.spell.remote.dto.SummonerSpellListResponseDto
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

    /** Every shop item, including modes and maps this app does not show. */
    @GET("cdn/{version}/data/{locale}/item.json")
    suspend fun getItems(
        @Path("version") version: String,
        @Path("locale") locale: String,
    ): ItemListResponseDto

    /** The five rune trees. Returns a bare array, not an object. */
    @GET("cdn/{version}/data/{locale}/runesReforged.json")
    suspend fun getRuneTrees(
        @Path("version") version: String,
        @Path("locale") locale: String,
    ): List<RuneTreeDto>

    @GET("cdn/{version}/data/{locale}/summoner.json")
    suspend fun getSummonerSpells(
        @Path("version") version: String,
        @Path("locale") locale: String,
    ): SummonerSpellListResponseDto
}
