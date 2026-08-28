# LOL GUIDE — Android · AI Agent Rules & Architecture Contract

> **MANDATORY:** Read this file completely before writing a single line of code.
> Every rule below is non-negotiable. This document is the authoritative source
> of truth for engineering decisions on this project. If a suggestion conflicts
> with this file, this file wins.

---

## 0. Project Identity

| Field            | Value                                                                 |
|------------------|-----------------------------------------------------------------------|
| **App Name**     | LoL Guide                                                             |
| **Package**      | `com.example.lolguide` (see §0.1 — rename pending)                    |
| **Platform**     | Android (Kotlin + Jetpack Compose)                                    |
| **Domain**       | Gaming reference & companion app for League of Legends                |
| **Users**        | Anonymous by default; optional account in Phase 5                     |
| **Data sources** | Data Dragon + Community Dragon (keyless CDN) · Riot Games API (keyed) |
| **Languages**    | English + Arabic, both required from Phase 0                          |
| **Min SDK**      | 24 · **Target/Compile SDK** 37                                        |

### 0.1 Package rename

`com.example.*` is a placeholder and must not ship. The rename to a real
application id happens as a single dedicated commit at the end of Phase 0.
Do not half-rename packages across feature commits.

---

## ⚠️ 1. CRITICAL DOMAIN NOTICE — Patch Correctness

This app's entire value is telling a player something **true about the current
patch**. League changes every two weeks. Stale or mismatched data is this
project's equivalent of a safety bug.

### Non-negotiable data rules

- **Never hardcode a patch version.** The previous codebase pinned
  `cdn/12.6.1/` in the endpoint, so it served data years stale. The current
  patch is resolved at runtime from `api/versions.json` and cached. A
  hardcoded version string anywhere is an automatic rejection.
- **Never mix patches within one screen.** If champion data came from patch
  `15.4.1`, its abilities, items and images must come from `15.4.1` too. The
  resolved version is passed down explicitly, never re-fetched per widget.
- **Never invent, estimate, or interpolate game numbers.** Every stat,
  cooldown, cost and ratio shown must come from the source data. If a field is
  missing, show it as unavailable — do not compute a plausible-looking value.
- **Derived numbers must be labelled as derived.** The build simulator and
  stat calculator (Phase 2) compute values; they must be visibly marked as
  estimates and must state the formula assumptions they make.
- **Show the patch the user is looking at.** Every data-bearing screen surfaces
  its patch version. A user must always be able to tell how fresh the data is.
- **Cached data is shown with its own patch label**, never relabelled as
  current. Offline mode is honest about being offline.

---

## 2. Confirmed Technology Stack

> Do NOT introduce a technology not listed here without explicit approval.

### 2.1 Core

| Concern               | Technology                             | Notes                           |
|-----------------------|----------------------------------------|---------------------------------|
| Language              | Kotlin                                 | 100% Kotlin, zero Java          |
| UI                    | Jetpack Compose (BOM)                  | Zero XML layouts                |
| Architecture          | Clean Architecture + MVI               | See §3, §4                      |
| DI                    | Hilt                                   | Only DI framework allowed       |
| State                 | `StateFlow<State>` + `Channel<Effect>` | No LiveData                     |
| Async                 | Coroutines + Flow                      | No RxJava                       |
| Network               | Retrofit 2 + OkHttp                    | suspend functions only          |
| Serialization         | `kotlinx.serialization`                | No Gson, no Moshi               |
| Database              | Room                                   | DAOs return `suspend` or `Flow` |
| Preferences           | DataStore Preferences                  |                                 |
| Navigation            | Navigation Compose, type-safe          | String routes are BANNED        |
| Images                | Coil 3                                 | `AsyncImage`                    |
| Collections in State  | `kotlinx.collections.immutable`        | `ImmutableList`, not `List`     |
| Background            | WorkManager                            | Patch sync, trackers            |
| Logging               | Timber                                 | No `Log` / `println`            |
| Annotation processing | KSP                                    | **KAPT is BANNED**              |

### 2.2 Explicitly Forbidden — Zero Exceptions

| Forbidden                                     | Use instead                    |
|-----------------------------------------------|--------------------------------|
| `kapt`                                        | KSP                            |
| Gson / Moshi                                  | `kotlinx.serialization`        |
| String-based nav routes                       | Type-safe routes (§6)          |
| `List<T>` in State                            | `ImmutableList<T>`             |
| `MutableSharedFlow` for effects               | `Channel(BUFFERED)`            |
| `LiveData`, RxJava, Koin, XML layouts         | StateFlow, Flow, Hilt, Compose |
| `GlobalScope`                                 | `viewModelScope`               |
| `CoroutineDispatcher` injected into ViewModel | Inject into repositories only  |
| `mutableStateOf` for ViewModel-level state    | `StateFlow` in the ViewModel   |
| Hardcoded `Color(0xFF...)` in a Composable    | `AppTheme.colors.*`            |
| Raw `sp` / `dp` text styles                   | `AppTheme.typography.*`        |
| Hardcoded user-facing strings in `.kt`        | `stringResource(R.string.xxx)` |
| `android.util.Log` / `println`                | `Timber`                       |
| Global `object` holding mutable state         | Hilt-injected singleton        |
| A hardcoded patch version                     | Resolved at runtime (§1)       |
| The Riot API key in committed source          | `local.properties` (§8)        |
| Fully qualified inline class names            | Normal imports at the top      |

---

## 3. Module Structure — STRICTLY ENFORCED

```
root/
├── app/           Android: @HiltAndroidApp, MainActivity, NavHost, Hilt wiring
├── data/          Android: RepoImpl, Room, Retrofit services, DTOs, mappers
├── domain/        Pure Kotlin: UseCases, models, repository interfaces
└── presentation/  Android: Compose screens, ViewModels, State/Event/Effect
```

```
        app
       /   \
      v     v
presentation data
      \     /
       v   v
       domain      <- pure Kotlin, knows nothing
```

**Hard rules**

- `domain` imports nothing outside the Kotlin stdlib, coroutines and
  `javax.inject`. Zero Android imports. If `import android.` appears in
  `domain`, it is wrong.
- `data` imports `domain` only. Never `presentation`.
- `presentation` imports `domain` only. Never `data`.
- `app` imports both, and only to wire Hilt bindings and the NavHost.
- A DTO never leaves `data`. A Compose `State` never enters `domain`. Mappers
  live in `data` and convert DTO/Entity to domain model.

---

## 4. MVI Contract

One ViewModel per screen. Every screen defines exactly four types in
`presentation/<feature>/`:

```kotlin
data class ChampionListState(
    val isLoading: Boolean = false,
    val champions: ImmutableList<Champion> = persistentListOf(),
    val error: UiText? = null,
)

sealed interface ChampionListEvent {
    data object Retry : ChampionListEvent
    data class SearchQueryChanged(val query: String) : ChampionListEvent
    data class ChampionClicked(val id: String) : ChampionListEvent
}

sealed interface ChampionListEffect {
    data class NavigateToDetail(val id: String) : ChampionListEffect
    data class ShowSnackbar(val message: UiText) : ChampionListEffect
}
```

- State is exposed as `StateFlow<State>`; effects as `Channel(BUFFERED)`
  consumed via `receiveAsFlow()`.
- The UI sends exactly one thing upward: `onEvent(Event)`. No other callbacks.
- Naming is `Event`, never `Intent` or `Action`.
- A Composable screen never calls a UseCase. Only the ViewModel does.
- ViewModels are dispatcher-agnostic: no `withContext`, no injected dispatcher.
  Threading is the repository's job (§7).

---

## 5. Package Structure

Feature-first inside each module, never layer-first:

```
domain/champion/model/Champion.kt
domain/champion/repository/ChampionRepository.kt
domain/champion/usecase/GetChampionsUseCase.kt

data/champion/remote/ChampionApi.kt
data/champion/remote/dto/ChampionDto.kt
data/champion/local/ChampionDao.kt
data/champion/local/entity/ChampionEntity.kt
data/champion/mapper/ChampionMappers.kt
data/champion/repository/ChampionRepositoryImpl.kt

presentation/champion/list/ChampionListScreen.kt
presentation/champion/list/ChampionListViewModel.kt
presentation/champion/list/ChampionListContract.kt
presentation/common/components/...
presentation/theme/...
```

One public class per file. File name matches the class name.

---

## 6. Navigation

Type-safe routes only, using `@Serializable` route objects:

```kotlin
@Serializable data object ChampionListRoute
@Serializable data class ChampionDetailRoute(val championId: String)
```

- The NavHost lives in `app/navigation/NavGraph.kt`.
- Screens receive plain lambdas (`onNavigateToDetail: (String) -> Unit`), never
  a `NavController`.
- Deep links are declared on the route and mirrored in `AndroidManifest.xml`.

---

## 7. Data Layer Rules

### 7.1 Offline-first is the default

Every repository serves from Room first, then refreshes from network. The UI
must render usable content with the network off. This is not a nice-to-have — a
guide app that is blank on a bad connection has failed.

### 7.2 Error handling

All repository calls return `Result<T>` produced by `runCatchingCancellable`,
which rethrows `CancellationException` rather than swallowing it. Never let a
raw exception cross a layer boundary. Never catch and silently return an empty
list — an empty result and a failed result are different things and the UI
renders them differently.

### 7.3 Dispatchers

Injected into the data layer only, via qualifiers (`@IoDispatcher`,
`@DefaultDispatcher`). Never into a ViewModel or a UseCase.

---

## 8. Network & API Keys

### 8.1 Two very different sources

| Source              | Key | Base URL                                                                         |
|---------------------|-----|----------------------------------------------------------------------------------|
| Data Dragon         | No  | `https://ddragon.leagueoflegends.com/`                                           |
| Community Dragon    | No  | `https://raw.communitydragon.org/`                                               |
| Riot API (Phase 4+) | Yes | `https://<platform>.api.riotgames.com/` and `https://<region>.api.riotgames.com/` |

Static-CDN calls and keyed calls use **separate Retrofit instances and separate
OkHttp clients**. The auth interceptor must never be attached to the Data
Dragon client.

### 8.2 Key handling

- The key lives in `local.properties` as `RIOT_API_KEY`, which is gitignored.
  It is surfaced to the build as a `BuildConfig` field. **Never commit a key.**
- The build must succeed with an empty key. Keyless features (Phases 0-3) must
  keep working; keyed features degrade to a clear "not configured" state.
- Development keys expire every 24 hours. Expiry must surface as a specific,
  actionable error, not a generic failure.
- The base URL for keyed calls is a single injected config value so it can be
  repointed at a proxy later without touching call sites.

### 8.3 Rate limits

Riot dev keys allow 20 requests/second and 100 requests/2 minutes, shared
across the whole key. Therefore:

- Never fan out one screen into N per-item requests without batching.
- Respect `Retry-After` on HTTP 429. Back off; do not hammer.
- Cache aggressively. A match, once fetched, is immutable — store it.

---

## 9. Theming

A custom design system wrapping Material 3, exposed as `AppTheme`:

- `AppTheme.colors` · `AppTheme.typography` · `AppTheme.shapes` · `AppTheme.dimens`
- Light and dark palettes both defined. No hardcoded colors in Composables.
- The palette is League-flavoured (gold and hextech blue on deep navy), defined
  once in `presentation/theme/`.

---

## 10. Localization — English + Arabic

- Zero hardcoded user-facing strings. Every string is added to
  `values/strings.xml` **and** `values-ar/strings.xml` in the same commit.
- RTL must work: use `Start`/`End`, never `Left`/`Right`.
- Data Dragon serves localized content — request the user's locale (`en_US` /
  `ar_AE`) so champion lore and ability text localize too.
- Numbers, dates and durations go through locale-aware formatters.
- Champion proper names stay untranslated; only surrounding UI text localizes.

---

## 11. Testing

**This project does not maintain unit tests.** Do not add `*Test.kt` files, do
not create test source sets, and do not treat missing coverage as an incomplete
change. This is a deliberate owner decision, not an oversight.

Correctness is therefore enforced by the rules in this document rather than by
a suite -- in particular §1 (patch correctness) and §7.2 (never swallow an
error into an empty success). Verification is a real build plus running the
app:

- `./gradlew assembleDebug`
- `./gradlew lint`

## 12. Planning Protocol

Before writing code for any non-trivial feature, save an implementation plan to
`docs/plans/YYYY-MM-DD-feature-name.md` containing: goal, affected modules, new
files, data sources and endpoints, state/event/effect shape, test plan, and
open questions. Skipping this is a protocol violation, not a shortcut.

---

## 13. UX Conventions

- Every async screen handles four states explicitly: loading, empty, error,
  content. "Empty" and "error" are never the same UI.
- Errors are actionable: a message plus a retry affordance.
- Destructive actions (clearing cache, removing a favorite, deleting a saved
  rune page) require a confirmation dialog gated by a field in `State`.
- Long lists use `LazyColumn` with stable `key`s.
- Images always have a placeholder and an error fallback.
- Content descriptions on every meaningful icon and image.

---

## 14. Git

- Branch per phase: `feat/phase-N-short-name`.
- Conventional commit subjects (`feat:`, `fix:`, `build:`, `refactor:`, `docs:`).
- The body explains **why**, not what.
- **No `Co-Authored-By:` trailer for AI agents.**
- Never commit `local.properties`, keystores, or `google-services.json`.
