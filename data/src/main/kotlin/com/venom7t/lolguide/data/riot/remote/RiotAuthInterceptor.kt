package com.venom7t.lolguide.data.riot.remote

import com.venom7t.lolguide.data.common.di.RiotApiKey
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

/**
 * Attaches the Riot API key to every request on the keyed client only --
 * never on the Data Dragon client, which has no interceptor of this kind at
 * all (AGENTS.md §8.1).
 *
 * When the key is empty, this fails locally with [MissingApiKeyException]
 * rather than sending an unauthenticated request and waiting for Riot to
 * answer 401/403. That distinction matters because a locally-detected
 * missing key and a real 401 mean different things to the user: one says
 * "you have not configured a key" (`AppError.ApiKeyMissing`), the other says
 * "your key expired" (`AppError.ApiKeyExpired`) -- conflating them into one
 * generic auth failure would send the user to fix the wrong thing.
 */
class RiotAuthInterceptor @Inject constructor(
    @RiotApiKey private val apiKey: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (apiKey.isBlank()) {
            throw MissingApiKeyException()
        }

        val authenticated = chain.request().newBuilder()
            .addHeader("X-Riot-Token", apiKey)
            .build()
        return chain.proceed(authenticated)
    }
}

/** Thrown locally, never sent over the network. Mapped to [com.venom7t.lolguide.domain.common.AppError.ApiKeyMissing]. */
class MissingApiKeyException : IOException("Riot API key is not configured")
