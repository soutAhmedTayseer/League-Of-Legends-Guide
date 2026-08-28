package com.example.lolguide.presentation.common

import com.example.lolguide.domain.common.AppError
import com.example.lolguide.presentation.R

/**
 * Turns a domain failure into something a user can act on (AGENTS.md §13).
 *
 * Every case maps to its own message. A single "something went wrong" string
 * would be easier to write and useless to the person reading it: "you are
 * offline" and "your API key expired" call for completely different actions.
 */
fun Throwable.toUiText(): UiText = when (this) {
    is AppError.Network -> uiText(R.string.error_network)
    is AppError.NoCachedData -> uiText(R.string.error_no_cached_data)
    is AppError.ApiKeyMissing -> uiText(R.string.error_api_key_missing)
    is AppError.ApiKeyExpired -> uiText(R.string.error_api_key_expired)
    is AppError.RateLimited -> retryAfterSeconds
        ?.let { uiText(R.string.error_rate_limited_retry_after, it) }
        ?: uiText(R.string.error_rate_limited)
    is AppError.NotFound -> uiText(R.string.error_not_found)
    is AppError.Serialization -> uiText(R.string.error_unexpected_response)
    is AppError.Http -> uiText(R.string.error_server, code)
    is AppError.Unknown -> uiText(R.string.error_unknown)
    else -> uiText(R.string.error_unknown)
}
