package com.venom7t.lolguide.data.common

import com.venom7t.lolguide.data.riot.remote.MissingApiKeyException
import com.venom7t.lolguide.domain.common.AppError
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Converts the exceptions Retrofit, OkHttp and kotlinx.serialization throw into
 * the single [AppError] type allowed to leave this module (AGENTS.md §7.2).
 *
 * Called at the repository boundary, never deeper, so that every failure is
 * classified exactly once.
 */
fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this

    is HttpException -> when (code()) {
        // 401/403 from the Riot API almost always means the development key
        // expired -- they last 24 hours -- so it gets an actionable case of
        // its own rather than a generic "HTTP 403".
        401, 403 -> AppError.ApiKeyExpired
        404 -> AppError.NotFound(response()?.raw()?.request?.url?.encodedPath.orEmpty())
        429 -> AppError.RateLimited(
            retryAfterSeconds = response()?.headers()?.get("Retry-After")?.toLongOrNull()
        )
        else -> AppError.Http(code = code(), body = message())
    }

    // Must be checked before the generic IOException case below, since this
    // is itself an IOException subtype (thrown from inside an OkHttp
    // interceptor, which only accepts IOException) but means something
    // different: "no key configured", not "network unreachable".
    is MissingApiKeyException -> AppError.ApiKeyMissing

    // OkHttp surfaces every connectivity problem as an IOException subclass:
    // no DNS, no route, timeout, connection reset.
    is IOException -> AppError.Network

    is SerializationException -> AppError.Serialization(message)

    else -> AppError.Unknown(this)
}
