# Phase 2 — Items, Runes and Build Tools

**Date:** 2026-08-28
**Branch:** `feat/phase-2-items-builds`
**Status:** in progress
**Depends on:** Phase 1

## Goal

The reference half of the app: items, runes and summoner spells, plus the two
tools that make this more than a data dump — a gold-efficiency calculation and
a build simulator.

All keyless Data Dragon. No Riot API key needed.

## Features

| # | Feature |
|---|---------|
| 24 | Item browser with tag/price filters and a visual build-path tree |
| 25 | Build simulator: champion + level + 6 items, with computed totals |
| 26 | Rune explorer and page builder |
| 27 | Gold efficiency per item |
| 28 | Summoner spell reference |

## Data sources

| Purpose | Endpoint |
|---------|----------|
| Items | `GET cdn/{version}/data/{locale}/item.json` |
| Runes | `GET cdn/{version}/data/{locale}/runesReforged.json` |
| Summoner spells | `GET cdn/{version}/data/{locale}/summoner.json` |
| Item icon | `cdn/{version}/img/item/{image.full}` |
| Rune icon | `cdn/img/{icon}` — already a full relative path, **not** version-scoped |
| Spell icon | `cdn/{version}/img/spell/{image.full}` |

Rune icon paths are the one irregular case: `runesReforged.json` ships a path
already rooted at `cdn/img/`, so prefixing it with a version produces a 404.

## Gold efficiency — derived, and derived honestly

Gold efficiency is `(value of an item's stats) / (its cost)`. It needs a gold
value per unit of each stat, and Riot publishes no such table.

The usual approach is to hardcode community constants. This project will not:
those constants go stale silently every time Riot re-costs a basic item, and
`AGENTS.md` §1 forbids presenting a stale guess as fact.

Instead the per-stat gold values are **computed at runtime from the basic items
in the same payload**, each of which sells exactly one stat:

| Stat | Derived from |
|------|--------------|
| Attack damage | Long Sword (`1036`) |
| Ability power | Amplifying Tome (`1052`) |
| Health | Ruby Crystal (`1028`) |
| Armor | Cloth Armor (`1029`) |
| Magic resist | Null-Magic Mantle (`1033`) |
| Attack speed | Dagger (`1042`) |
| Mana | Sapphire Crystal (`1027`) |
| Crit chance | Cloak of Agility (`1018`) |
| Health regen | Rejuvenation Bead (`1006`) |

So the table re-derives itself on every patch. If a reference item is missing
from a payload, that stat is simply excluded from the calculation and the
result is reported as partial rather than silently under-counted.

Efficiency is still an **estimate** and is labelled as one: it cannot price
passives, actives, or item cooldowns, and an item can be worth buying at 80%
stat efficiency because its passive is the reason to buy it.

## Build simulator

Champion + level + up to six items produces:

- Flat totals per stat (base at level, from `ChampionStatCalculator`, plus items)
- **Effective HP** against physical and magic damage: `HP × (1 + resist / 100)`
- A rough auto-attack DPS figure

Every one of these is derived and labelled. The DPS number in particular
assumes an unarmoured target, no abilities, no item passives, and no crit
damage modifiers — stated in the UI, because a DPS number without its
assumptions is misinformation.

## Work breakdown

- **2.1** This plan.
- **2.2 `:domain`** — `Item`, `ItemGold`, `ItemStats`, `RuneTree`, `Rune`,
  `RunePage`, `SummonerSpell`; `ItemRepository`, `RuneRepository`,
  `SummonerSpellRepository`; `GoldEfficiencyCalculator`, `BuildSimulator`.
- **2.3 `:data`** — endpoints, DTOs, Room entities for items, mappers, repos.
- **2.4 `:presentation`** — item browser, item detail with build path, build
  simulator, rune explorer, spell reference.
- **2.5** — routes, nav, EN + AR strings, `assembleDebug` + `lint`.

## Out of scope

Recommended builds per champion (Data Dragon's `recommended` block has been
unmaintained for years and would be misleading), and anything needing a key.
