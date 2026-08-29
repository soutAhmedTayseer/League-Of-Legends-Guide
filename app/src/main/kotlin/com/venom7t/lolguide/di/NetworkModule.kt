package com.venom7t.lolguide.di

import com.venom7t.lolguide.BuildConfig
import com.venom7t.lolguide.data.champion.remote.DataDragonApi
import com.venom7t.lolguide.data.riot.remote.RiotApi
import com.venom7t.lolguide.data.riot.remote.RiotApiKeyProvider
import com.venom7t.lolguide.data.riot.remote.RiotAuthInterceptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val DATA_DRAGON_BASE_URL = "https://ddragon.leagueoflegends.com/"

/**
 * Placeholder base URL only -- every [RiotApi] call supplies its own
 * absolute `@Url`, since Riot's platform/regional host split cannot be
 * expressed as one fixed base (see [RiotApi]'s doc comment). Retrofit still
 * requires a syntactically valid base to construct.
 */
private const val RIOT_API_PLACEHOLDER_BASE_URL = "https://universal.api.riotgames.com/"

val DATA_DRAGON_RETROFIT = named("dataDragonRetrofit")
val RIOT_API_RETROFIT = named("riotApiRetrofit")
val RIOT_API_KEY = named("riotApiKey")

val networkModule = module {

    single {
        // Riot adds fields to Data Dragon payloads between patches without
        // warning. Failing to parse the whole champion list because one new
        // key appeared would break the app on patch day.
        Json {
            ignoreUnknownKeys = true
            // Missing fields fall back to the DTO defaults rather than throwing,
            // which matters for champions that legitimately lack some keys.
            explicitNulls = false
            coerceInputValues = true
        }
    }

    single {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    // The client for the keyless CDN. Deliberately has no auth interceptor.
    // The Riot API client is a separate instance so a key can never be
    // attached to a CDN request by accident (AGENTS.md §8.1).
    single(DATA_DRAGON_RETROFIT) {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single(DATA_DRAGON_RETROFIT) {
        Retrofit.Builder()
            .baseUrl(DATA_DRAGON_BASE_URL)
            .client(get(DATA_DRAGON_RETROFIT))
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single { get<Retrofit>(DATA_DRAGON_RETROFIT).create(DataDragonApi::class.java) }

    single(RIOT_API_KEY) { BuildConfig.RIOT_API_KEY }

    single { RiotApiKeyProvider(get(RIOT_API_KEY), get(), get(APPLICATION_SCOPE)) }

    single { RiotAuthInterceptor(get()) }

    // A separate OkHttpClient instance from the Data Dragon one, carrying
    // RiotAuthInterceptor -- the two clients never share an instance, so a
    // key can never end up attached to a CDN request (AGENTS.md §8.1).
    single(RIOT_API_RETROFIT) {
        OkHttpClient.Builder()
            .addInterceptor(get<RiotAuthInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single(RIOT_API_RETROFIT) {
        Retrofit.Builder()
            .baseUrl(RIOT_API_PLACEHOLDER_BASE_URL)
            .client(get(RIOT_API_RETROFIT))
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single { get<Retrofit>(RIOT_API_RETROFIT).create(RiotApi::class.java) }
}
