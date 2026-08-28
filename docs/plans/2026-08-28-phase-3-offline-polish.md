# Phase 3 — Offline, Polish & Voice Lines

**Date:** 2026-08-28
**Branch:** `feat/phase-3-offline-polish`
**Status:** in progress
**Depends on:** Phase 0-2 (merged to `main` at `62dc123`)

## Goal

Everything in this phase is still keyless. The theme is turning the guide from
"a set of screens" into an app that feels finished: a real home, offline
correctness, in-game usefulness, and the voice-line feature deferred from
Phase 1.

## Features in scope

| # | Feature | Notes |
|---|---------|-------|
| 29 | Patch diff engine | Diff patch N vs N-1 across champions and items |
| 34 | Onboarding + home dashboard | Region/role pick, personalized landing screen |
| 35 | Offline-first, properly | WorkManager sync on patch change, not on open |
| 37 | In-game companion timers | Baron/Dragon/Herald, ward, jungle camp respawns |
| 38 | Quiz / trivia mode | Guess champion from ability icon / splash crop / quote |
| 39 | Home-screen widgets | Free rotation widget (deferred detail below) |
| 41 | Deep links | `lolguide://champion/{id}`, share-to-app |
| 43 | Accessibility pass | TalkBack labels, font scaling, contrast |
| 16 | Voice lines | Deferred from Phase 1; addressed below |

## Voice lines (#16) — why it was deferred, and the plan now

Phase 1 deferred this because it needed an off-stack audio-player dependency
and Community Dragon's voice-line paths are undocumented. Both are resolved:

- **Dependency**: `androidx.media3:media3-exoplayer` +
  `androidx.media3:media3-ui`. This is the standard modern Android audio/video
  stack (successor to ExoPlayer standalone), actively maintained by Google, and
  the same family already implicitly endorsed by `androidx.*` elsewhere in
  `AGENTS.md` §2. Needs your sign-off before use, same as `androidx.palette`
  did in Phase 1 — see the question below.
- **Source**: Community Dragon does host voice lines, but not through a
  documented index. The reliable path is `raw.communitydragon.org/latest/
  game/data/characters/{championId}/skins/skin0/voiceover/{locale}/vo-
  audio.bin` style archives, which are binary sound banks, not individual
  playable files -- there is no per-line MP3 to point a player at.
  **Practical scope for Phase 3**: ship voice line playback for the small set
  of champions where Community Dragon *does* expose split OGG files under
  `.../voice-audio/`, treat it as best-effort per champion, and show "voice
  lines unavailable" honestly rather than a broken player when a champion
  isn't covered. This is a real feature with an honest gap, not a full
  guarantee across all 160+ champions.

## Patch diff engine (#29)

- `:domain` — `PatchDiff` model (champions added/removed/stat-changed, items
  added/removed/repriced), `ComputePatchDiffUseCase` comparing two cached
  snapshots.
- `:data` — the champion/item caches already key rows by id but replace
  wholesale per patch (Phase 0/2 `replaceAll`). Diffing needs the *previous*
  patch's data retained one generation back before the wholesale replace
  happens. Adds a `PreviousPatchSnapshotDao` that is populated right before a
  refresh overwrites the live cache.
- `:presentation` — "What's new" screen, reachable from the patch badge (the
  patch badge already exists everywhere per AGENTS.md §1; tapping it becomes
  the diff entry point rather than adding new chrome).

## Home dashboard (#34)

- Onboarding: region + primary role, stored in DataStore, one-time.
- Home replaces the champion list as `startDestination`; champion list moves
  to the Champions tab. Home shows: patch badge + "what's new" teaser, free
  champion rotation banner *(Phase 4 -- keyed; the home screen renders a
  placeholder card here until then, not a broken call)*, quick links to
  favourites/simulator/roulette.

## Offline-first, properly (#35)

Phase 0/2 already refresh only on patch change or cold cache. What's missing:
a WorkManager periodic job that checks `versions.json` in the background so
the cache is warm *before* the user opens the app on a new patch, not after.
`PatchSyncWorker` in `:data`, scheduled from `:app`, constrained to
unmetered-or-user-allowed network per standard WorkManager practice.

## In-game timers (#37)

New offline feature, no champion/item dependency: a stopwatch screen with
preset buttons (Baron up in 6:00, Dragon in 5:00, Herald in 6:00, ward 2:30)
that start a countdown and post a notification at zero. Pure `:presentation` +
a lightweight `:domain` timer model; reuses `NotificationChannels`-equivalent
setup patterned after nothing existing yet in this app -- new, since Phase 0-2
had no notification need.

## Quiz mode (#38)

`:domain` — `QuizQuestion` sealed type (AbilityIconGuess, SplashCropGuess,
QuoteGuess... quote guess needs voice-line text, which Data Dragon does not
ship as transcripts, so **quote mode is dropped from scope**, ability-icon and
splash-crop modes ship). Score tracked in-memory only for Phase 3; persistence
is a later nice-to-have, not blocking.

## Widgets (#39) — scoped down

Free rotation requires Riot's CHAMPION-V3 endpoint, which is **keyed** (Phase
4). Shipping a widget now would mean a widget with nothing to show. Phase 3
ships the widget *infrastructure* (a `GlanceAppWidget` shell showing the patch
badge) and defers the rotation content to Phase 4, noted explicitly rather
than silently built half-working.

## Deep links (#41)

`lolguide://champion/{championId}` declared on `ChampionDetailRoute` and
mirrored in `AndroidManifest.xml` per `AGENTS.md` §6. Share button on champion
detail builds this link via `Intent.ACTION_SEND`.

## Accessibility (#43)

Sweep of existing Phase 0-2 screens: every `AsyncImage` needs a real
`contentDescription` (several currently pass `null` for decorative-only
icons, which is correct, but interactive images were audited individually),
`fontScale` tested at 1.3x/2.0x on the list and detail screens, contrast
checked on `AppColors` against WCAG AA -- this is a review pass over existing
code, not new screens.

## Dependencies needing approval

| Dependency | For | Precedent |
|---|---|---|
| `androidx.media3:media3-exoplayer`, `media3-ui` | Voice lines | Same `androidx.*` family as `androidx.palette` (Phase 1, approved) |
| `androidx.glance:glance-appwidget` | Widget shell | Standard modern widget API, `androidx.*` |

## Work breakdown

- 3.1 This plan.
- 3.2 `:domain` — patch diff, quiz questions, in-game timer presets.
- 3.3 `:data` — previous-patch snapshot, `PatchSyncWorker`, voice-line source
  (best-effort), onboarding preferences.
- 3.4 `:presentation` — onboarding, home dashboard, what's-new screen, timers
  screen, quiz screen, widget shell, deep link wiring.
- 3.5 Accessibility sweep over Phase 0-2 screens.
- 3.6 EN + AR strings, `assembleDebug` + `lint`.

## Explicitly out of scope

Quote-guess quiz mode (no transcript source). Full voice-line coverage (best
-effort only, honestly labelled). Free-rotation widget content (Phase 4,
keyed). Anything needing a Riot key.
