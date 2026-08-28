package com.example.lolguide.di

import com.example.lolguide.BuildConfig
import com.example.lolguide.data.champion.remote.DataDragonApi
import com.example.lolguide.data.common.di.DataDragonRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val DATA_DRAGON_BASE_URL = "https://ddragon.leagueoflegends.com/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // Riot adds fields to Data Dragon payloads between patches without
        // warning. Failing to parse the whole champion list because one new
        // key appeared would break the app on patch day.
        ignoreUnknownKeys = true
        // Missing fields fall back to the DTO defaults rather than throwing,
        // which matters for champions that legitimately lack some keys.
        explicitNulls = false
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    /**
     * The client for the keyless CDN.
     *
     * Deliberately has no auth interceptor. The Riot API client (Phase 4) is a
     * separate instance so a key can never be attached to a CDN request by
     * accident (AGENTS.md §8.1).
     */
    @Provides
    @Singleton
    @DataDragonRetrofit
    fun provideDataDragonOkHttp(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @DataDragonRetrofit
    fun provideDataDragonRetrofit(
        @DataDragonRetrofit client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(DATA_DRAGON_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideDataDragonApi(@DataDragonRetrofit retrofit: Retrofit): DataDragonApi =
        retrofit.create(DataDragonApi::class.java)
}
