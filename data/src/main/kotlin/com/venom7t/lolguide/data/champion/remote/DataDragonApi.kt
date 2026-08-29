package com.venom7t.lolguide.data.champion.remote

import com.venom7t.lolguide.data.champion.remote.dto.ChampionDetailResponseDto
import com.venom7t.lolguide.data.champion.remote.dto.ChampionListResponseDto
import com.venom7t.lolguide.data.item.remote.dto.ItemListResponseDto
import com.venom7t.lolguide.data.rune.remote.dto.RuneTreeDto
import com.venom7t.lolguide.data.spell.remote.dto.SummonerSpellListResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

private const val BASE_URL = "https://ddragon.leagueoflegends.com"

/**
 * Data Dragon, Riot's static content CDN. No API key, no rate limit.
 *
 * Every path is parameterised by `version` and `locale`. There is deliberately
 * no overload that defaults the version: the previous implementation hardcoded
 * `cdn/12.6.1/` here and served champion data years out of date (AGENTS.md §1).
 */
class DataDragonApi(private val client: HttpClient) {

    /**
     * All published patches, newest first. Index 0 is the live patch.
     *
     * Note this is under `api/`, not `cdn/`, and is the one call that is not
     * version-parameterised -- it is how the version is discovered.
     */
    suspend fun getVersions(): List<String> =
        client.get("$BASE_URL/api/versions.json").body()

    /**
     * Every champion, with base stats but without abilities or lore.
     *
     * @param locale a Data Dragon locale code such as `en_US` or `ar_AE`.
     */
    suspend fun getChampions(version: String, locale: String): ChampionListResponseDto =
        client.get("$BASE_URL/cdn/$version/data/$locale/champion.json").body()

    /**
     * One champion's abilities, lore and skin list.
     *
     * @param championId Data Dragon's string id (`MonkeyKing`, not `Wukong`).
     */
    suspend fun getChampionDetail(
        version: String,
        locale: String,
        championId: String,
    ): ChampionDetailResponseDto =
        client.get("$BASE_URL/cdn/$version/data/$locale/champion/$championId.json").body()

    /** Every shop item, including modes and maps this app does not show. */
    suspend fun getItems(version: String, locale: String): ItemListResponseDto =
        client.get("$BASE_URL/cdn/$version/data/$locale/item.json").body()

    /** The five rune trees. Returns a bare array, not an object. */
    suspend fun getRuneTrees(version: String, locale: String): List<RuneTreeDto> =
        client.get("$BASE_URL/cdn/$version/data/$locale/runesReforged.json").body()

    suspend fun getSummonerSpells(version: String, locale: String): SummonerSpellListResponseDto =
        client.get("$BASE_URL/cdn/$version/data/$locale/summoner.json").body()
}
