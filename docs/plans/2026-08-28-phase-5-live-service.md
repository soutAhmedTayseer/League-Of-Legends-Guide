# Phase 5 — Live Service (Firebase)

**Date:** 2026-08-28
**Branch:** `feat/phase-5-live-service`
**Status:** in progress
**Depends on:** Phase 0-4 (merged to `main` at `a1cb0aa`)

## Goal

Everything through Phase 4 is either keyless (Data Dragon) or a stateless
Riot API read. Phase 5 adds state that outlives a single request: account
sync across devices, background LP tracking, and push notifications when
something changes while the app isn't open. That requires a backend, which
is why this phase is the one gated on Firebase.

Firebase project: `lol-guide-c5ce3`, wired in commit `29cd0e2`
(`google-services.json` committed, Firestore created in production mode,
Cloud Messaging active by default once the Android app is registered).

## Scope

**Building this phase:**

1. **Anonymous auth** (`firebase-auth`) — every install gets a stable
   anonymous UID with no sign-up flow. This is the sync identity; there is no
   real-account/login system yet (owner decision, matches the "local now"
   pattern from Phase 4's followed-summoners call).
2. **Cross-device sync** (`firebase-firestore`) — favourited champions and
   followed summoners mirror to `/users/{uid}/favourites` and
   `/users/{uid}/followedSummoners`. Room stays the read model the UI binds
   to; Firestore is a sync target the repository pushes to and listens on,
   not a replacement for local storage (offline-first, per AGENTS.md §8).
3. **LP tracker** — a periodic WorkManager job polls ranked entries for
   followed summoners, stores each poll as a dated snapshot in a new Room
   table (`lp_snapshot`), and a screen shows the delta since the last poll
   per queue. This is explicitly a **derived, locally-recorded history**, not
   something Riot's API exposes directly (AGENTS.md §1) — it only knows what
   it has observed since tracking started.
4. **Push notifications** (`firebase-messaging`) — a local notification
   channel fires when the LP tracker worker detects a change past a small
   threshold for a followed summoner. This is client-triggered (the worker
   runs on-device and posts a local notification directly), not a server
   push — there is no Cloud Function in this phase, so FCM's role here is
   limited to registering a token for future use; today's notification path
   is `WorkManager → NotificationManager` entirely on-device.
5. **Duo stats** — derived from match history already cached by Phase 4's
   permanent match cache: for a followed summoner, group their cached
   matches by which other puuids appeared on their team, and show win rate
   played together. No new Riot endpoint — this is a client-side aggregation
   over data already fetched, carrying the same derived-value labelling
   discipline as `DerivedChampionStats`.
6. **Clash** — CLASH-V1 is a real, keyed Riot endpoint (team roster + next
   scheduled match for a summoner's registered Clash team, if any).

**Explicitly not built this phase:**

- **Esports schedule.** There is no official Riot API for esports schedules
  or results — that lives on a separate, differently-authenticated esports
  data source outside `developer.riotgames.com`. Rather than scrape or wire
  an unofficial API with no key-management story, this is deferred; it can
  be revisited if a supported source appears.
- **Real accounts / non-anonymous sign-in.** Anonymous auth is enough for
  device-bound sync. Email/Google sign-in is a bigger surface (account
  recovery, merge-on-sign-in) that isn't needed to satisfy "sync my
  favourites and followed summoners," so it's left for if that need
  actually shows up.
- **Cloud Functions / actual server-sent push.** Notifications this phase
  are on-device (WorkManager-triggered), which is honest about being a
  single-device background check rather than a real push infrastructure.

## Data model additions

- `lp_snapshot` Room table (new, Room v5→v6 migration): `puuid`, `queueType`,
  `tier`, `rank`, `leaguePoints`, `capturedAtEpochMillis`. Permanent history,
  same non-destructive-migration discipline as the match cache.
- Firestore: `/users/{uid}/favourites/{championId}`,
  `/users/{uid}/followedSummoners/{puuid}`. Minimal documents — the same
  fields already in the Room entities, not a redesigned schema.

## Verification

Same as every prior phase: `:app:assembleDebug` and `:app:lintDebug` clean
before each commit, EN+AR strings in the same commit as their screens, merge
to `main` via fast-forward when the phase is done.
