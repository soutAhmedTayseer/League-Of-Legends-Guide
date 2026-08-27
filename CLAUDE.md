# CLAUDE.md

This repo's actual rules live in `AGENTS.md`. Read it before touching code —
it is long and detailed on purpose. This file is just the pointer and the TL;DR.

## Commands

Multi-module Gradle project: `:app`, `:domain`, `:data`, `:presentation`.

- Build debug APK: `./gradlew assembleDebug`
- Compile check (fast): `./gradlew :app:compileDebugKotlin`
- Unit tests (all modules): `./gradlew test`
- Unit tests, single module: `./gradlew :presentation:test`
- Single test class: `./gradlew :presentation:test --tests "*.ChampionListViewModelTest"`
- Lint: `./gradlew lint`

The build requires `local.properties` with `sdk.dir` set. It is gitignored and
must be created per machine. Add `RIOT_API_KEY=` there too (Phase 4+); the
build succeeds with an empty value.

## Toolchain notes

- Gradle 9.6.1, AGP 9.2.1, Kotlin 2.2.0, compileSdk 37.1, minSdk 24.
- The daemon JVM is pinned to Java 17 by `gradle/gradle-daemon-jvm.properties`
  and auto-provisioned by the foojay resolver, because the only JDK installed
  on this machine is 25 and toolchains should not depend on `PATH`.
- AGP 9 has built-in Kotlin: do **not** apply `org.jetbrains.kotlin.android`,
  it collides with AGP's own `kotlin` extension.
- `android.disallowKotlinSourceSets=false` is required in `gradle.properties`
  for KSP to register generated sources under AGP 9.

## Non-negotiables (see `AGENTS.md` for full detail)

- **Never hardcode a patch version** (§1). Resolve it at runtime from
  `versions.json`. The original code pinned `12.6.1` and served stale data.
- **Plan first.** Non-trivial features get a plan in `docs/plans/` (§12).
- **Every ViewModel needs a test file** (§11).
- **Zero hardcoded user-facing strings** — English *and* Arabic, same commit (§10).
- **Strict layer boundaries**: `presentation -> domain <- data`. Domain is pure
  Kotlin with zero Android imports (§3).
- **`AppTheme.colors` / `.typography` / `.shapes` only** — never a raw
  `Color(0xFF...)` in a Composable (§9).
- **Never commit the Riot API key** (§8.2).
- **No AI co-author trailer on commits** (§14).

## Roadmap

Phase 0 foundation · 1 champion guide · 2 items & builds · 3 offline & polish ·
4 player data (needs Riot key) · 5 live service (needs Firebase). Phases 0-3 are
entirely keyless.

If anything here conflicts with `AGENTS.md`, `AGENTS.md` wins — update this file
to match, do not silently deviate.
