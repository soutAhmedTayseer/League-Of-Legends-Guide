package com.example.lolguide.domain.common

import kotlin.coroutines.cancellation.CancellationException

/**
 * [runCatching] that does not swallow coroutine cancellation.
 *
 * Plain `runCatching` catches [CancellationException] too, which silently
 * breaks structured concurrency: a cancelled coroutine looks like a failed
 * one, cancellation stops propagating, and the caller waits forever. Every
 * repository in this project uses this instead (AGENTS.md §7.2).
 */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }

/**
 * [Result.map] equivalent that also keeps cancellation propagating when the
 * transform itself suspends or throws.
 */
inline fun <T, R> Result<T>.mapCatchingCancellable(transform: (T) -> R): Result<R> =
    fold(
        onSuccess = { runCatchingCancellable { transform(it) } },
        onFailure = { Result.failure(it) },
    )
