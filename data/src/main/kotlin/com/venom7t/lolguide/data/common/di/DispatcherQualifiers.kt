package com.venom7t.lolguide.data.common.di

import javax.inject.Qualifier

/**
 * Dispatchers are injected into the data layer only (AGENTS.md §7.3).
 *
 * ViewModels stay threading-agnostic: they call a suspend function and it is
 * the repository's business which thread the work lands on. That is what makes
 * ViewModel tests able to run entirely on the test scheduler.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

/** Retrofit instance for the keyless Data Dragon / Community Dragon CDN. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DataDragonRetrofit

/**
 * Retrofit instance for the keyed Riot API (Phase 4+).
 *
 * Kept separate from [DataDragonRetrofit] so the auth interceptor can never be
 * attached to CDN calls by accident (AGENTS.md §8.1).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RiotApiRetrofit

/**
 * The Riot API key, read from `local.properties` and surfaced as a
 * `BuildConfig` field in `:app` (AGENTS.md §8.2). Exposed to `:data` through
 * this qualifier rather than a direct `BuildConfig` reference, since `:data`
 * cannot import `:app`'s generated class -- the module dependency runs the
 * other way (`app` depends on `data`, never the reverse, AGENTS.md §3).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RiotApiKey

/**
 * A process-lifetime [kotlinx.coroutines.CoroutineScope], for fire-and-
 * forget work that must outlive the caller -- Phase 5's best-effort sync
 * pushes, specifically. Favouriting a champion should not make the UI wait
 * on a Firestore round trip, and should not be cancelled just because the
 * screen that triggered it was left before the push finished.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
