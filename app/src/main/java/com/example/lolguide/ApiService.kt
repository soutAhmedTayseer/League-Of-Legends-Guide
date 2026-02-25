package com.example.lolguide

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    // Existing call
    @GET("cdn/12.6.1/data/en_US/champion.json")
    suspend fun getChampions(): ChampionResponse

    // NEW API CALL FOR FEATURE 1
    @GET("cdn/{version}/data/en_US/champion/{championId}.json")
    suspend fun getChampionDetail(
        @Path("version") version: String,
        @Path("championId") championId: String
    ): ChampionDetailResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://ddragon.leagueoflegends.com/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}