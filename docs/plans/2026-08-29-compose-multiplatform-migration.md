# Compose Multiplatform migration plan

**Status:** proposal, not approved. No code changes made.
**Written:** 2026-08-29
**Goal:** run this app on iOS as well as Android, from one shared codebase.

---

## 1. Verdict up front

This is a **rewrite of the plumbing, not a port of the app**. The UI and business
logic largely survive; almost every library underneath them does not.

The single most important fact from the survey: **`:domain` is already 100%
Android-free** — 97 files, 3,697 lines, and `grep` for `^import android(x)?`
returns **zero** files. That module was built to AGENTS.md §3 (`java-library`,
not `android-library`, specifically to make Android imports impossible) and it
moves to `commonMain` essentially as-is.

Everything else needs work in proportion to how much Android it touches.

| Module | Files | Lines | Portability |
|---|---|---|---|
| `:domain` | 97 | 3,697 | **Near-free.** 0 Android imports. Swap `javax.inject` annotations, done. |
| `:data` | 64 | 4,155 | **Heaviest rewrite.** Every I/O library is Android-only. |
| `:presentation` | 89 | 15,324 | **Wide but mechanical.** Compose API is ~identical; resources and ViewModels are the friction. |
| `:app` | 12 | 1,497 | **Splits in two.** Becomes `androidApp` + `iosApp` + shared nav. |

~24,700 lines of Kotlin across 262 source files.

---

## 2. What actually has to be replaced

Counts are files that reference each API, measured on the current tree.

| Concern | Today | KMP replacement | Files | Difficulty |
|---|---|---|---|---|
| **DI** | Hilt / Dagger | **Koin** | 133 | High — no KMP Hilt, ever |
| **Local DB** | Room (Android) | **Room 3.x KMP** | 37 | Medium — API survives, setup changes |
| **Network** | Retrofit + OkHttp | **Ktor client** | 6 | Low — only 15 endpoints |
| **Prefs** | DataStore | **DataStore (already KMP)** | 6 | Low |
| **Firebase** | Firebase Android SDK | **GitLive firebase-kotlin-sdk** | 4 | Medium — API differs |
| **Background work** | WorkManager | *No equivalent* — redesign | 6 | **High — see §6** |
| **Auth** | Credential Manager (Google) | Per-platform + **Sign in with Apple** | 1 | **High — see §6** |
| **Resources** | `res/values/strings.xml` | `composeResources` | 324 call sites | Medium — wide, mechanical |
| **ViewModel** | `androidx.lifecycle.ViewModel` | Same (now KMP) | 28 | Low |
| **WebView** (API-key portal) | `android.webkit.WebView` | `expect`/`actual` | 1 | Low — contained |
| **Locale switching** | `AppCompatDelegate` | `expect`/`actual` | 1 | Low |
| **Images** | Coil 3 | Coil 3 (already KMP) | — | **Free** |
| **Navigation** | Navigation Compose | Nav Compose (now KMP) | 1 | Low |
| **Voice lines** | Media3 / ExoPlayer | `expect`/`actual` player | 1 | Medium |
| **Palette** (colour extraction) | `androidx.palette` | Manual or `expect`/`actual` | 1 | Low |

### The DI number needs context

138 files match the DI grep, but they are not equal work:

- **68 in `:domain`** — only `javax.inject.@Inject` on constructors. Koin reads
  constructors directly; these become one-line registrations in a Koin module,
  or the annotation is swapped for a KMP-safe equivalent. Cheap.
- **31 in `:data`** — same shape, same cheapness.
- **27 `@HiltViewModel`** + **29 `hiltViewModel()` call sites** — real edits, but
  a mechanical find-and-replace to `koinViewModel()`.
- **7 `:app` Dagger modules** — these are the actual work. Every `@Binds` and
  `@Provides` is hand-translated into Koin `module { }` blocks.

### Resources are wider than they look

343 strings × 2 locales (en + ar), referenced from **324 `stringResource()` call
sites**, plus 2 variable font `.ttf` files. CMP has its own resource system
(`Res.string.foo`). The XML moves to `commonMain/composeResources/values/`, and
all 324 call sites change import + accessor. Mechanical, tedious, low-risk —
but it touches nearly every UI file, so it wants to be one focused pass.

---

## 3. Target module structure

```
:domain              → KMP (commonMain only). Almost unchanged.
:data                → KMP. commonMain + androidMain + iosMain
                        (SQLite driver, HTTP engine, Firebase actuals)
:presentation        → KMP + Compose Multiplatform. commonMain
                        + thin androidMain/iosMain for WebView, locale, audio
:composeApp          → shared nav graph, theme, DI wiring  (replaces most of :app)
:androidApp          → Activity, manifest, google-services.json, WorkManager
:iosApp              → Xcode project, SwiftUI shell, Firebase plist, BGTaskScheduler
```

---

## 4. Sequencing

Each phase should end green (builds + runs on Android). **Android must never
break** — that's the ship-blocking constraint while iOS is speculative.

**Phase 0 — Prove the toolchain (½ day)**
Add the KMP + CMP plugins, add an iOS target that builds nothing real, get an
empty "Hello" running in the iOS simulator. Do not touch app code. If this
fights back, stop and reassess before spending real effort.

**Phase 1 — `:domain` → KMP (1 day)**
`java-library` → `kotlin-multiplatform`. Move to `commonMain`. Swap the
`javax.inject` annotation dependency. Nothing else should change. Android app
must still build and run identically.

**Phase 2 — Hilt → Koin (3–4 days)**
The riskiest *non-optional* phase, done while still Android-only so failures
are obvious. Translate the 7 Dagger modules, swap 27 `@HiltViewModel` and
29 `hiltViewModel()` sites. Full regression pass at the end.

**Phase 3 — `:data` I/O swap (4–6 days)**
Retrofit → Ktor (15 endpoints). Room → Room KMP (8 entities, 8 DAOs, **6
migrations that must be preserved exactly** — existing installs depend on them).
DataStore config change. Firebase → GitLive.
*Highest chance of silent data bugs. Test reinstall + restore explicitly.*

**Status (2026-08-29, on throwaway branch `spike/cmp-phase0`):**
- ✅ **Retrofit → Ktor** — done. `DataDragonApi`/`RiotApi` are now plain
  classes wrapping an `HttpClient`; every repository call site was
  unaffected since method signatures didn't change. `ErrorMapper.kt` now
  classifies `ResponseException` instead of `HttpException`.
- ✅ **Firebase → GitLive** — done, verified against GitLive's actual SDK
  source (not assumed from memory) before writing call sites, since a
  wrong field mapping here means silent data loss, not a compile error.
  `:data` now compiles at JVM 17 (GitLive's reified `get<T>()`/`set<T>()`
  are inline functions built with JVM 17 bytecode). **Not independently
  verified**: an actual round-trip against a live Firestore project, or
  Google sign-in against a real account — no working emulator/device in
  this environment to exercise either.
- ⏸️ **Room → Room KMP — turned out to need no source changes at all**,
  and is not the real remaining task. Room 2.8.3+ already ships its
  multiplatform support under the *same* `androidx.room:room-runtime`
  coordinate this project already pins (2.8.4) — the Android-specific
  `Room.databaseBuilder(context, ...)` overload, all 8 entities/DAOs, and
  all 6 migrations work completely unchanged as long as `:data` stays an
  `android-library`. (The plan's original "Room 3.x" framing above was
  stale; 3.0 turned out to be a separate relocated artifact under
  `androidx.room3`, not the KMP-enabling release.)

  The actual blocker was converting `:data` itself into a KMP module,
  which is what would let Room's other platforms matter at all — and
  that conversion ran straight into a product decision (WorkManager has
  no iOS story). **Resolved 2026-08-29: see Phase 6's background-sync
  decision below** (foreground-only on iOS).

  **Attempted 2026-08-29, reverted: `:data` → KMP hits an unresolved
  upstream tooling bug, not a design question.** AGP 9 requires the new
  `com.android.kotlin.multiplatform.library` plugin for any KMP module
  with an Android target (`com.android.library` is flatly rejected
  alongside the Kotlin Multiplatform plugin as of AGP 9.0). KSP does not
  yet support that new plugin at all — every Android-target compilation
  it creates fails a class cast inside Kotlin's own Gradle tooling
  (`KotlinMultiplatformAndroidCompilationImpl` cannot be cast to
  `KotlinJvmAndroidCompilation`), tracked upstream as
  [google/ksp#2476](https://github.com/google/ksp/issues/2476), open and
  unresolved. Room's annotation processing for all 8 entities/DAOs runs
  through KSP, so this blocks the conversion outright — not a design
  choice to revisit, a dependency on Google fixing this upstream first.
  `:data` remains an `android-library`; re-attempt once that issue is
  closed and a KSP release ships with the fix.

**Phase 4 — `:presentation` → CMP (4–6 days)**
Jetpack Compose → Compose Multiplatform imports. Resource migration (the 324
call sites). `expect`/`actual` for WebView, locale switching, audio, palette.

**Phase 5 — App shells (3–5 days)**
Split `:app`. Stand up the Xcode project, Firebase iOS config, iOS entry point.
First real run on device.

**Phase 6 — iOS-specific work (open-ended)**
Sign in with Apple. Then a **full QA pass** — iOS gesture handling, scroll
physics, keyboard/IME behaviour, safe areas, back-swipe and lifecycle all
differ enough that "it compiled" is nowhere near "it works."

**Decision (2026-08-29): background sync degrades to foreground-only on
iOS.** `PatchSyncWorker` and `LpTrackerWorker` stay exactly as they are on
Android (WorkManager, unchanged). iOS gets no equivalent background job --
no periodic patch pre-warm, no LP-change notifications while the app isn't
open. This unblocks `:data`'s KMP conversion without new backend
infrastructure; it can be revisited later (e.g. a server-side scheduled job)
if the degraded iOS experience turns out to matter in practice.

**Realistic total: 3–5 focused weeks**, with Phase 6 genuinely open-ended.

---

## 5. Decisions you need to make before Phase 0

These are product calls, not technical ones. I should not pick them for you.

1. **Sign in with Apple — required, not optional.**
   App Store Guideline 4.8: an app that *exclusively* uses a third-party login
   (this app's Google gate is mandatory) must also offer an equivalent
   privacy-preserving option. In practice that means Sign in with Apple. This is
   new auth surface, new Firebase provider config, and a change to the
   currently-mandatory-Google sign-in gate.

2. **Background sync has no shared answer.**
   `LpTrackerWorker` and `PatchSyncWorker` rely on WorkManager. iOS background
   execution (`BGTaskScheduler`) is opportunistic — the OS decides if and when
   your task runs, and it may effectively never fire for a low-engagement app.
   LP-change notifications cannot work the same way on iOS. Options: accept
   degraded/foreground-only refresh on iOS, or move the tracker server-side.
   **This is a feature-behaviour decision, not a port.**

3. **Is Android allowed to regress at all?**
   Every phase above assumes no. That's the safe call and it's what makes the
   plan take 3–5 weeks instead of 2. Worth confirming.

4. **Room KMP vs SQLDelight.**
   Room 3.x now supports KMP natively, which preserves your 6 existing
   migrations and the DAO API — strongly preferred. SQLDelight is the more
   battle-tested KMP option but would mean rewriting the schema and migration
   history from scratch. Recommend Room; flag SQLDelight only if Room KMP
   fights the iOS build.

---

## 6. Risks, honestly

| Risk | Why it matters |
|---|---|
| **Firebase via a third-party SDK** | GitLive is community-maintained (last release Apr 2026) and lags the official SDK. A newer option, KFire, is still beta. You'd be trusting auth + sync to a non-Google wrapper. |
| **Room migrations** | 6 chained migrations carry real user data. A KMP mistake here corrupts existing installs on *Android*, not just iOS. Needs deliberate reinstall/restore testing. |
| **The Hilt→Koin blast radius** | Compile-time DI → runtime DI. Hilt catches wiring mistakes at build time; Koin surfaces them as runtime crashes. Wiring bugs will reach the emulator instead of the compiler. |
| **iOS ≠ "free platform"** | CMP gets you rendering, not correctness. Expect a real QA tail on gestures, keyboard, safe areas, lifecycle, and back-navigation. |
| **Feature freeze pressure** | Doing this alongside active feature work means merge pain in ~250 files. Best done as a dedicated branch with a quiet period. |

---

## 7. Recommendation

Do **Phase 0 only** first, as a timeboxed spike (half a day, throwaway branch).

It's the cheapest possible answer to "does this toolchain cooperate with *this*
project's Gradle/AGP/Kotlin versions?" — and if the answer is no, you've spent
half a day instead of a week finding out. Nothing in Phase 0 touches app code,
so it's fully reversible.

Only commit to Phases 1–6 after the spike is green **and** the four §5 decisions
are settled.

---

## Sources

- [Room 3.0: From Android-First ORM to True KMP](https://medium.com/@mhdwajeeh.95/room-3-0-from-android-first-orm-to-true-kotlin-multiplatform-e3f30dc0ccf1)
- [Jetpack libraries KMP support](https://www.kmpship.app/blog/jetpack-libraries-kmp-support-2025)
- [GitLive firebase-kotlin-sdk](https://github.com/GitLiveApp/firebase-kotlin-sdk)
- [Using Firebase with KMP in 2026](https://medium.com/@riadmahi/using-firebase-with-kotlin-multiplatform-in-2026-the-complete-guide-43a30042155c)
- [App Store Guideline 4.8 login services](https://ptkd.com/journal/app-store-rejection-4-8-sign-in-with-apple-requirement-fix)
