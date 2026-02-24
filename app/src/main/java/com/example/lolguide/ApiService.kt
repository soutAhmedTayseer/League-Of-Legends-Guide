package com.example.lolguide

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiService {
    @GET("cdn/16.1.1/data/en_US/champion.json")
    suspend fun getChampions(): ChampionResponse
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