package com.venom7t.lolguide.presentation.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * Phase 2 (Hilt -> Koin): KSP scans every `@KoinViewModel`/`@Single`
 * class in this module and generates the Koin bindings for it. See
 * `docs/plans/2026-08-29-compose-multiplatform-migration.md`.
 */
@Module
@ComponentScan("com.venom7t.lolguide.presentation")
class PresentationModule
