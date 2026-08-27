package com.example.lolguide.data.common.di

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
