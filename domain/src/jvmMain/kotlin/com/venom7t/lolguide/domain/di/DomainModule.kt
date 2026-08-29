package com.venom7t.lolguide.domain.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * Phase 2 (Hilt -> Koin): KSP scans every `@Factory`-annotated use case
 * class in this module and generates the Koin bindings for it, the same
 * role Hilt's codegen played for any `@Inject`-constructor class. See
 * `docs/plans/2026-08-29-compose-multiplatform-migration.md`.
 */
@Module
@ComponentScan("com.venom7t.lolguide.domain")
class DomainModule
