# Phase 0 — Clean Architecture Migration

**Date:** 2026-08-28
**Branch:** `feat/phase-0-clean-architecture`
**Status:** in progress

## Goal

Restructure the existing single-module app into the four-module Clean
Architecture + MVI shape defined in `AGENTS.md`, and port the two existing
screens (champion list, champion detail) onto it as the first vertical slice.

Phase 0 adds no user-visible features beyond two correctness fixes that fall
out of the migration:

1. The patch version is resolved at runtime instead of being pinned to
   `12.6.1`, so the app stops serving years-old data.
2. Arabic support and a real offline path exist from the start.

## Starting point

| Aspect        | Before                                                       |
|---------------|--------------------------------------------------------------|
| Modules       | `:app` only                                                  |
| Files         | 13 Kotlin files, all in `com.example.lolguide`               |
| UI            | `MainActivity.kt`, 821 lines, every Composable in one file   |
| State         | None — Composables call the repository directly              |
| DI            | None — `RetrofitClient` is a global `object`                 |
| Serialization | Gson                                                         |
| Navigation    | A `when` over a local `var currentScreen`                    |
| Patch version | Hardcoded `12.6.1` in the endpoint path                      |
| Tests         | Two generated stubs, no real coverage                        |
| Strings       | Hardcoded in Kotlin, English only                            |

## Target

```
:app            Application, MainActivity, NavGraph, Hilt wiring modules
:domain         Champion + patch models, repository interfaces, use cases
:data           Retrofit services, DTOs, Room entities/DAOs, mappers, repo impls
:presentation   Theme system, common components, champion list + detail (MVI)
```

## Work breakdown

- **0.1 Toolchain** — done, commit `daf7b85`. Gradle 9.6.1 / AGP 9.2.1 /
  Kotlin 2.2.0, daemon JVM pinned to 17.
- **0.2 Contract** — `AGENTS.md` + `CLAUDE.md`.
- **0.3 This plan.**
- **0.4 Module skeleton** — four `build.gradle.kts` files, `settings.gradle.kts`
  includes, Hilt/KSP/serialization plugins per module.
- **0.5 `:domain`** — `Champion`, `ChampionDetail`, `Spell`, `Passive`,
  `ChampionStats`, `PatchVersion`; `ChampionRepository`; use cases
  `GetChampionsUseCase`, `GetChampionDetailUseCase`, `GetCurrentPatchUseCase`,
  `SearchChampionsUseCase`. Plus `runCatchingCancellable` and the `AppError`
  type. Zero Android imports.
- **0.6 `:data`** — `DataDragonApi` (versions, champions, champion detail),
  `@Serializable` DTOs, `ChampionEntity` + `ChampionDao` + `LolGuideDatabase`,
  mappers, `ChampionRepositoryImpl` (Room-first, network refresh),
  `PatchVersionStore` on DataStore, dispatcher qualifiers.
- **0.7 `:app`** — `LolGuideApplication`, Hilt modules (`NetworkModule`,
  `DatabaseModule`, `RepositoryModule`, `DispatcherModule`, `DataStoreModule`),
  `MainActivity`, type-safe `NavGraph` + `Route`.
- **0.8 `:presentation`** — `AppTheme` (colors/typography/shapes/dimens, light
  and dark), common components (loading, error, empty), `ChampionListScreen` +
  `ChampionListViewModel` + contract, `ChampionDetailScreen` +
  `ChampionDetailViewModel` + contract.
- **0.9 Strings** — `values/strings.xml` and `values-ar/strings.xml`.
- **0.10 Verify** — `./gradlew assembleDebug lint` green, app runs.

  (A test step was planned here and removed: the owner decided on 2026-08-28
  that this project does not maintain unit tests. See `AGENTS.md` §11.)

## Data sources (all keyless in Phase 0)

| Purpose        | Endpoint                                               |
|----------------|--------------------------------------------------------|
| Patch versions | `GET api/versions.json` — array, index 0 is current    |
| Champion list  | `GET cdn/{version}/data/{locale}/champion.json`        |
| Champion detail| `GET cdn/{version}/data/{locale}/champion/{id}.json`   |
| Square icon    | `cdn/{version}/img/champion/{image.full}`              |
| Splash art     | `cdn/img/champion/splash/{id}_{skinNum}.jpg` (unversioned) |
| Ability icon   | `cdn/{version}/img/spell/{image.full}`                 |
| Passive icon   | `cdn/{version}/img/passive/{image.full}`               |

Base URL `https://ddragon.leagueoflegends.com/`. Locale is `en_US` or `ar_AE`
depending on app language.

## Patch resolution strategy

1. On app start, `GetCurrentPatchUseCase` reads the cached version from
   DataStore and returns it immediately so the UI never blocks.
2. In parallel, fetch `versions.json`. If index 0 differs from the cached
   value, store the new one and invalidate the champion cache.
3. Every champion request takes the resolved version as an explicit parameter.
   No layer may read a version from anywhere else. Screens display it.

## Contracts

**ChampionList** — State: `isLoading`, `champions: ImmutableList<Champion>`,
`query`, `patchVersion`, `error`. Events: `Retry`, `QueryChanged`,
`ChampionClicked`. Effects: `NavigateToDetail`, `ShowSnackbar`.

**ChampionDetail** — State: `isLoading`, `champion`, `detail`, `patchVersion`,
`error`. Events: `Retry`, `BackClicked`. Effects: `NavigateBack`,
`ShowSnackbar`.

## Deliberately out of scope for Phase 0

Skins, lore, filters, favorites, items, runes, and every Riot-API feature. They
are Phases 1-5. Phase 0 ships the same two screens on a foundation that can
carry them.

## Open questions

- Package rename from `com.example.lolguide` — deferred to a single commit at
  the end of Phase 0 (`AGENTS.md` §0.1). Final application id not yet chosen.
- Arabic translations in Phase 0 are structural; wording should get a native
  review pass before release.
