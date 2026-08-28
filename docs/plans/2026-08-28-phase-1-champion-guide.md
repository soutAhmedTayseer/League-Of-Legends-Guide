# Phase 1 — Champion Guide, Deepened

**Date:** 2026-08-28
**Branch:** `feat/phase-1-champion-guide`
**Status:** in progress
**Depends on:** Phase 0 (`docs/plans/2026-08-28-phase-0-architecture-migration.md`)

## Goal

Turn the two-screen port from Phase 0 into an actual champion guide. Every
feature here is served by the keyless Data Dragon / Community Dragon CDN, so
none of it needs a Riot API key.

## Features in scope

| # | Feature | Where |
|---|---------|-------|
| 14 | Skins gallery with splash carousel and chromas | detail |
| 17 | Filters: role, resource, difficulty, damage type | list |
| 18 | Fuzzy search with community aliases | list |
| 19 | Compare two champions side by side | new screen |
| 20 | Level slider 1-18 with computed stats | detail |
| 21 | Favourites | list + new tab |
| 22 | Random champion roulette, filterable | new screen |
| 40 | Splash-art-tinted detail theming | detail |

Feature 13 (ability panel) and 15 (lore) already landed in Phase 0.

Feature 16 (voice lines) is **deferred to Phase 3**: audio playback needs a
player dependency that is not on the approved stack in `AGENTS.md` §2, and
Community Dragon's audio paths are unversioned and undocumented. Adding it
here would mean approving a new dependency mid-phase.

## Work breakdown

- **1.1** This plan.
- **1.2 `:domain`** — `Skin` model; `ChampionAliases` lookup; `ChampionFilter`
  + `FilterOption` value types; `FavouritesRepository`; `ScaledStats`
  calculator; extend `SearchChampionsUseCase` with alias and fuzzy matching;
  `ObserveFavouritesUseCase`, `ToggleFavouriteUseCase`, `RandomChampionUseCase`,
  `CompareChampionsUseCase`.
- **1.3 `:data`** — parse `skins` from the existing champion detail payload
  (no new endpoint); `FavouriteChampionEntity` + DAO;
  **Room schema v1 to v2 with a real migration** — favourites are user-authored
  and must survive an upgrade, which is why destructive fallback is removed
  here (Phase 0 `DatabaseModule` flagged this).
- **1.4 `:presentation` list** — filter chip row, alias-aware search, favourite
  star on each row, entry points to compare and roulette.
- **1.5 `:presentation` detail** — skins carousel, level slider with derived
  stats, favourite toggle, splash-tinted accent.
- **1.6 `:presentation`** — compare screen.
- **1.7 `:presentation`** — roulette screen and favourites tab.
- **1.8** — EN + AR strings, nav wiring, `assembleDebug` + `lint`.

## Stat scaling — a derived number

The level slider computes stats Riot does not ship. League's growth formula is:

```
stat(level) = base + growth * (level - 1) * (0.7025 + 0.0175 * (level - 1))
```

Attack speed is different: `attackspeedperlevel` is a percentage applied to the
base, not a flat addition.

Per `AGENTS.md` §1 these are **derived values and must be labelled as such** in
the UI, with the level shown. At level 1 the formula must return exactly the
base stat — that is the sanity check on the implementation.

## Aliases

A static map in `:domain` (`mundo` to `DrMundo`, `asol` to `AurelionSol`,
`kata` to `Katarina`, `yi` to `MasterYi`, `j4` to `JarvanIV`, `tf` to
`TwistedFate`, `ww` to `Warwick`, `mf` to `MissFortune`, and so on). Data
Dragon ids are not what players type — `MonkeyKing` is Wukong — so id-only
matching fails on the most-searched champions.

Matching order: exact name, alias, prefix, then subsequence ("ktrn" to
Katarina). Ranked, not just filtered, so the best match sorts first.

## Filters

Derived from data already cached, so no new requests:

- **Role** — from `tags` (Assassin, Fighter, Mage, Marksman, Support, Tank)
- **Resource** — from `partype` (Mana, Energy, None, and the bespoke ones)
- **Difficulty** — from `info.difficulty`, bucketed low/medium/high
- **Damage type** — inferred from `info.attack` vs `info.magic`

Damage type is the one inferred field. It is a heuristic over Riot's own 0-10
bars, not a Riot classification, so it is labelled as an approximation in the
UI rather than presented as authoritative.

## Navigation additions

```kotlin
@Serializable data object FavouritesRoute
@Serializable data object RouletteRoute
@Serializable data class CompareRoute(val leftId: String?, val rightId: String?)
```

## Out of scope

Voice lines (deferred above), items, runes, and everything needing a Riot key.

## Open questions

- Chromas: Data Dragon exposes chroma ids but not chroma images; those live on
  Community Dragon. Phase 1 shows skins only, and chromas are listed as a count
  unless the Community Dragon path proves reliable.
