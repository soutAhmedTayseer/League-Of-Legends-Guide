# Phase 6 — Champion Riddle (daily guessing game)

**Date:** 2026-08-28
**Branch:** `feat/phase-6-champion-riddle`
**Status:** parked — domain layer drafted, resumes after Phase 5

## Goal

A daily champion-guessing game, in the genre popularised by Wordle and applied
to League by several community sites. Requested by the owner on 2026-08-28.

Originally slated to displace Phase 2. The owner reordered on 2026-08-28: the
main roadmap (items, polish, player data, live service) finishes first and the
game ships last, as Phase 6. Everything here is keyless Data Dragon data.

The domain layer (`GameMode`, `Clue`, `RoundProgress`, `GameStats`) was drafted
before the reorder and is committed on this branch. Nothing is wired into the
app, so the mainline carries no dead code.

## Scope and originality

The **mechanic** — one answer per day, shared by all players, guesses scored
against attributes of the answer — is a genre, and is implemented here from
scratch.

What is **not** copied: any existing site's name, branding, visual identity, or
hand-authored content. That last point is also a hard technical constraint:

| Mode | Buildable | Why |
|------|-----------|-----|
| Classic — attribute clues per guess | yes | Riot publishes the attributes |
| Ability — guess from an ability icon | yes | Data Dragon hosts the icons |
| Splash — guess from a cropped splash | yes | Data Dragon hosts the art |
| Emoji | **no** | Emoji sets are authored by a person, not published by Riot |
| Quote | **no** | Voice lines are not in any Riot API (same blocker as feature 16) |

Three modes ship. Emoji and Quote are not deferred pending effort — they are
not derivable from any data source this app is allowed to use, and the only way
to build them would be to copy someone's authored work or hand-write 170+
entries.

## Clue attributes (Classic)

Data Dragon publishes no gender, region, or release year, which are the
attributes community sites typically use. The clue set is therefore built from
what Riot actually ships:

| Attribute | Source | Feedback |
|-----------|--------|----------|
| Role | `tags` | match / partial (shares one) / miss |
| Resource | `partype` | match / miss |
| Damage type | inferred from `info` | match / miss, flagged approximate |
| Difficulty | `info.difficulty` | match / higher / lower |
| Range | `stats.attackrange` | melee vs ranged, match / miss |

Damage type is inferred (see `AGENTS.md` §1 and `DamageType`), so the legend
marks it as an approximation rather than presenting it as a Riot fact.

## Daily answer

Deterministic from the day, so every player gets the same champion and the
answer is reproducible offline with no server:

```
index = stableHash(epochDay, mode) % pool.size
```

`epochDay` is computed from `System.currentTimeMillis()` plus the device's UTC
offset. `java.time.LocalDate` is not used: `minSdk` is 24 and `java.time`
needs API 26 or core library desugaring, and adding desugaring for one date
calculation is not worth the build cost.

**Known consequence:** the pool is the cached champion list, which grows when
Riot ships a champion. A new champion shifts the modulo, so a past day's answer
can differ after a patch. Acceptable for a local game with no shared
leaderboard; it would not be if scores were ever compared between players.

## Rules

- 6 guesses in Classic, 4 in Ability and Splash (fewer clues per guess).
- Guessing is autocomplete over the cached champion list, reusing
  `SearchChampionsUseCase` so aliases work here too.
- A champion cannot be guessed twice in one round.
- In Splash, each wrong guess zooms the crop out slightly — a wrong answer
  should buy information, not just cost a life.
- State persists per day: closing the app mid-round resumes it.
- Streaks per mode: current, best, games played, wins.

## Work breakdown

- **6.1** This plan.
- **6.2 `:domain`** — `GameMode`, `ClueState`, `ClueRow`, `GuessResult`,
  `DailyPuzzle`, `RoundProgress`; `PickDailyChampionUseCase`,
  `EvaluateGuessUseCase`; `GameProgressRepository`.
- **6.3 `:data`** — `GameProgressLocalDataSource` on DataStore,
  `GameProgressRepositoryImpl`, JSON-serialised per-mode round state.
- **6.4 `:presentation`** — game hub (mode picker with streaks), round screen
  with guess field, clue grid, splash/ability prompt, win/lose sheet.
- **6.5** — routes, nav entry, EN + AR strings, `assembleDebug` + `lint`.

## Out of scope

Emoji and Quote modes (above). No shared leaderboard, no server, no account —
progress is device-local, consistent with the app having no auth until Phase 5.
