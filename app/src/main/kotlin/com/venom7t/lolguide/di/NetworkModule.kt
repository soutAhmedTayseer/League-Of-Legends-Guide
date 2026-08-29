package com.venom7t.lolguide.di

import com.venom7t.lolguide.BuildConfig
import com.venom7t.lolguide.data.champion.remote.DataDragonApi
import com.venom7t.lolguide.data.riot.remote.RiotApi
import com.venom7t.lolguide.data.riot.remote.RiotApiKeyProvider
import com.venom7t.lolguide.data.riot.remote.RiotAuthInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

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
    // attached to a CDN request by accident (AGENTS.md §8.1). Also used
    // directly (not through Ktor) by VoiceLineProbe's raw HEAD requests.
    single(DATA_DRAGON_RETROFIT) {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    single(DATA_DRAGON_RETROFIT) {
        HttpClient(OkHttp) {
            engine { preconfigured = get(DATA_DRAGON_RETROFIT) }
            expectSuccess = true
            install(ContentNegotiation) { json(get<Json>()) }
        }
    }

    single { DataDragonApi(get(DATA_DRAGON_RETROFIT)) }

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
        HttpClient(OkHttp) {
            engine { preconfigured = get(RIOT_API_RETROFIT) }
            expectSuccess = true
            install(ContentNegotiation) { json(get<Json>()) }
        }
    }

    single { RiotApi(get(RIOT_API_RETROFIT)) }
}
