package com.example.lolguide.domain.common

/**
 * The only failure type allowed to cross a layer boundary (AGENTS.md §7.2).
 *
 * Raw [Throwable]s stay inside `:data`, which maps them here. That keeps
 * Retrofit, OkHttp and Room types out of `:domain` and `:presentation`, and it
 * forces every failure to be classified into something the UI can act on.
 */
sealed class AppError : Throwable() {

    /** No usable connection, DNS failure, or the request timed out. */
    data object Network : AppError()

    /** The server answered, but not with success. */
    data class Http(val code: Int, val body: String? = null) : AppError()

    /**
     * A Riot API key was required but is absent or empty. Phases 0-3 never
     * produce this; it exists so keyed features can degrade honestly rather
     * than surfacing a generic failure (AGENTS.md §8.2).
     */
    data object ApiKeyMissing : AppError()

    /**
     * The Riot API rejected the key. Development keys expire every 24 hours,
     * which is by far the most common cause, so it gets its own case rather
     * than hiding inside [Http].
     */
    data object ApiKeyExpired : AppError()

    /** Rate limited. [retryAfterSeconds] comes from the `Retry-After` header. */
    data class RateLimited(val retryAfterSeconds: Long?) : AppError()

    /** The response did not match the expected shape. */
    data class Serialization(val detail: String?) : AppError()

    /** The requested entity does not exist (e.g. an unknown champion id). */
    data class NotFound(val what: String) : AppError()

    /**
     * Nothing cached and nothing reachable. Distinct from [Network] because
     * the UI can offer "you are offline, showing nothing" only when the cache
     * is genuinely empty.
     */
    data object NoCachedData : AppError()

    data class Unknown(override val cause: Throwable?) : AppError()
}
