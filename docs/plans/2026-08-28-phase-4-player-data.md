# Phase 4 — Player Data (Riot API)

**Date:** 2026-08-28
**Branch:** `feat/phase-4-player-data`
**Status:** in progress
**Depends on:** Phase 0-3 (merged to `main` at `c32e416`)

## Goal

Everything in Phases 0-3 was keyless. This phase is the first to use the Riot
API proper: summoner lookup, match history, live-game scouting, mastery, and
the ladder. This is also the point where `AGENTS.md` §8 (network & keys) stops
being theoretical and starts being load-bearing.

## Riot key handling recap (already built, Phase 0 §8.2)

- `RIOT_API_KEY` lives in `local.properties`, surfaced via `BuildConfig`,
  never committed. Present on this machine (42 chars, `RGAPI-` format).
- **Development keys expire every 24 hours.** `AppError.ApiKeyExpired` and the
  401/403 → `ApiKeyExpired` mapping in `toAppError()` already exist (Phase 0).
  This phase is where that mapping actually gets exercised.
- Owner decision from the original brainstorm: **local now, proxy later**. A
  Cloudflare Worker proxy is not built this phase; the base URL for the keyed
  client is a single injected config value so repointing it later is a
  one-line change (already true from Phase 0's `NetworkModule` structure).
- The keyed client is a **separate Retrofit/OkHttp instance** from the
  Data Dragon client, with its own auth interceptor. This is already
  scaffolded as a qualifier (`@RiotApiRetrofit`, Phase 0 §8.1) but not yet
  built — this phase builds it for the first time.

## Rate limits (AGENTS.md §8.3)

Dev keys: 20 req/s, 100 req/2min, shared across the whole key. Consequences
for design:

- **Never fan out one screen into N per-champion or per-match requests
  without batching or caching.** Match history is the obvious trap: a match
  list screen must not issue one detail request per row on scroll.
- **Cache aggressively.** A finished match is immutable once fetched — Room
  stores every match ever looked up, keyed by match id, forever (unlike the
  champion/item caches, which are wholesale-replaced per patch).
- Respect `Retry-After` on HTTP 429 (`AppError.RateLimited` already models
  this from Phase 0).

## Region routing

Riot splits requests across two different hostnames that do **not** share a
region value:

- **Platform routing** (`na1`, `euw1`, `eun1`, ...) for SUMMONER-V4,
  MASTERY-V4, LEAGUE-V4 (ladder), SPECTATOR-V5, CHAMPION-V3 (rotation).
- **Regional routing** (`americas`, `europe`, `asia`, `sea`) for ACCOUNT-V1
  (Riot ID lookup) and MATCH-V5.

`Region` already exists in `:domain` (Phase 3, `onboarding.model.Region`) with
a `platformId`. It needs a second field mapping to the regional route, since
the two are not 1:1 derivable from each other in general (though for the
regions in scope here they follow predictable groupings).

## Endpoints in scope

| Feature | Endpoint | Routing |
|---|---|---|
| Riot ID → PUUID | `GET /riot/account/v1/accounts/by-riot-id/{name}/{tag}` | Regional |
| Summoner profile | `GET /lol/summoner/v4/summoners/by-puuid/{puuid}` | Platform |
| Ranked info | `GET /lol/league/v4/entries/by-summoner/{summonerId}` | Platform |
| Match id list | `GET /lol/match/v5/matches/by-puuid/{puuid}/ids` | Regional |
| Match detail | `GET /lol/match/v5/matches/{matchId}` | Regional |
| Match timeline | `GET /lol/match/v5/matches/{matchId}/timeline` | Regional |
| Live game | `GET /lol/spectator/v5/active-games/by-summoner/{puuid}` | Platform |
| Champion mastery | `GET /lol/champion-mastery/v4/champion-masteries/by-puuid/{puuid}` | Platform |
| Free rotation | `GET /lol/platform/v3/champion-rotations` | Platform |
| Challenger ladder | `GET /lol/league/v4/challengerleagues/by-queue/{queue}` | Platform |
| Server status | `GET /lol/status/v4/platform-data` | Platform |

## Features in scope

| # | Feature |
|---|---------|
| 1 | Riot ID search (Name#TAG → profile) |
| 2 | Match history |
| 3 | Match detail + timeline |
| 4 | Live game / scout-my-lobby |
| 5 | Champion mastery |
| 6 | Auto-computed champion stats (derived from match history, AGENTS.md §1 labelling applies) |
| 7 | Followed summoners |
| 12 | Shareable profile card |
| 23 | Free champion rotation (fills the Phase 3 home-dashboard placeholder) |
| 30 | Server status banner |
| 32 | Ladder browser |

## Work breakdown

- 4.1 This plan.
- 4.2 `:domain` — `Summoner`, `RankedEntry`, `MatchSummary`, `MatchDetail`,
  `LiveGame`, `ChampionMastery`, `LadderEntry`, `ServerStatus` models;
  repository interfaces; use cases (`SearchSummonerUseCase`,
  `GetMatchHistoryUseCase`, `GetLiveGameUseCase`, etc.); extend `Region` with
  regional routing; a `DerivedChampionStats` type for #6, explicitly labelled
  derived per §1.
- 4.3 `:data` — `@RiotApiRetrofit` client + auth interceptor (first real use
  of the Phase 0 scaffold), DTOs for every endpoint above, a permanent
  (non-patch-scoped) match cache in Room, repository implementations, 401/403
  → `ApiKeyExpired` and 429 → `RateLimited` verified against the real API.
- 4.4 `:presentation` — summoner search, profile screen, match history list,
  match detail, live-game screen, mastery grid, ladder browser, server status
  banner, followed summoners, share card. Home dashboard's rotation
  placeholder (Phase 3) gets real content.
- 4.5 EN + AR strings, `assembleDebug` + `lint`.

## Verification note

Live-API behavior (real 401 on key expiry, real 429 on rate limit, real
region routing) cannot be verified by a static build — it needs the app
actually running against the network with your key. `assembleDebug` + `lint`
confirm the code compiles and is internally consistent; a real run is the
owner's to do, same as Phase 0-3.

## Open questions for the owner

- Test Riot ID / region to use as the default in manual testing:
  `venom7T#EUNE`, platform `eun1`, regional route `europe` (from earlier in
  this conversation).
- Whether followed summoners (#7) is scoped to local-only (Room, no account)
  or expected to sync anywhere — no account system exists yet (Phase 5).
  Assumption: **local-only** for this phase, matching how favourites work.

## Explicitly out of scope

The Cloudflare proxy (deferred to whenever it's actually needed). Any
Firebase-backed feature (Phase 5). Push notifications on rank change (Phase
5, needs FCM).
