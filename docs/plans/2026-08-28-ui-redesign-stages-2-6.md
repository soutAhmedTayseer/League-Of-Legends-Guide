# UI redesign — stages 2 to 6

Stage 1 (design system + Home) is done and committed on
`feat/hextech-forge-design` as `caf81a6`. This plan covers the rest.

Each stage is one branch and one or more commits. Work them in order: stage 6
assumes the components stages 2–5 introduce, and stage 3 is the largest.

---

## 0. Read this first

### The design language ("Hextech Forge")

Stage 1 established these. Do not reinvent them, do not add a second visual
idiom alongside them.

| Piece | Where | Rule |
| --- | --- | --- |
| Cut corners | `AppTheme.shapes` | Every corner in the app is a 45° chamfer (`CutCornerShape`). Never use `RoundedCornerShape` directly. |
| Cinzel | `AppTheme.typography.displayLarge / titleLarge / eyebrow / tileLabel` | Titles, section labels, tile labels. Never body text. |
| Inter | `AppTheme.typography.titleMedium / bodyLarge / bodyMedium / label / caption` | Everything read as prose. |
| Monospace | `AppTheme.typography.statValue` | Any digits that change while on screen — LP, countdowns, stat columns. Stops jitter. |
| `HextechFrame` | `presentation/common/components/Hextech.kt` | The image treatment: gold hairline, inset mount, chamfered art. Use it wherever champion/item art is *content*. Bare `AsyncImage` is only for icons. |
| `SectionRule` | same file | Cinzel eyebrow + fading gold rule. The standard band separator. Replaces bare section `Text`s. |
| `CutSurface` | same file | The one card primitive. `highlighted = true` gives the teal-tinted accent variant. Replaces ad-hoc `background + border` columns. |

Colour roles live in `theme/Color.kt`. Read them off `AppTheme.colors`; never
name a colour literal outside that file (AGENTS.md §9).

### Non-negotiables (from AGENTS.md)

- **§1 — never invent game data.** If Data Dragon or Riot does not publish a
  value, the app does not show it. This bites hardest in stage 3; read that
  stage's data note before writing any code.
- **§4 MVI** — `State` / `Event` / `Effect`, one `ViewModel` per screen,
  `hasStarted` guard on `ScreenOpened`.
- **§6** — type-safe routes only, declared in
  `presentation/navigation/Route.kt`, wired in `app/navigation/NavGraph.kt`.
- **§10** — every new user-facing string lands in **both**
  `values/strings.xml` and `values-ar/strings.xml`, in the same commit.
- **§13** — anything destructive is confirmed with a dialog first.
- **No unit tests anywhere in this project.** Verification is
  `:app:assembleDebug` + `:app:lintDebug` plus a look at the screen.

### Working rules for this repo

- Branch first, never commit to `main`. Ask before merging.
- Commit messages: no `Co-Authored-By` trailer.
- **Never create a source package called `build`.** `.gitignore` has a bare
  `build/`, which matches at any depth and silently swallows every file in
  e.g. `domain/.../domain/build/`. The saved-builds feature had to be renamed
  to `builds/` for this reason. Check `git status` shows your new files as
  `A` before committing.

### Windows / toolchain notes

- Run Gradle through PowerShell: `.\gradlew.bat :app:assembleDebug --console=plain`.
  The bash tool intermittently fails to spawn it.
- `bundleLibCompileToJarDebug FAILED ... classes.jar ... used by another
  process` is a file lock: `.\gradlew.bat --stop`, then retry.
- After renaming or moving any package, run `.\gradlew.bat clean` before
  building, or KSP/Hilt will keep emitting references to the old package.
- Screenshots on this emulator need an explicit display id, because it
  reports two:
  ```
  adb shell 'screencap -p -d 4619827259835644672 /sdcard/s.png'
  adb pull /sdcard/s.png <local path>
  ```
  From Git Bash, prefix both with `MSYS_NO_PATHCONV=1` or the `/sdcard/` path
  gets rewritten to a Windows path.
- The emulator AVD is `Resizable_Experimental`.

### Known state that affects the work

- **The Riot API key returns 401.** Anything behind CHAMPION-V3, SPECTATOR-V5,
  LEAGUE-V4 etc. will not return data right now. Every screen touching those
  must degrade to a clear, designed state — never an empty band (AGENTS.md
  §8.2). Home's champion-of-the-day fallback is the pattern to copy.
- Data Dragon (champions, items, spells, runes, splash art) works fine and is
  cached in Room. Build on that.

---

## Stage 2 — Settings

**Branch:** `feat/settings-screen`

The app has no settings at all today, and theme is currently whatever the
system says (`AppTheme(useDarkTheme = isSystemInDarkTheme())`).

### What to build

A `SettingsRoute` screen with four groups:

1. **Appearance** — theme: System / Light / Dark.
2. **Language** — English / العربية / follow system.
3. **Account** — shows the signed-in Google account (or anonymous), and a
   **Sign out** action, confirmed with a dialog (§13). Move the account
   *detail* here; Home keeps only the avatar shortcut into it.
4. **About** — patch version, app version, and the "not endorsed by Riot"
   line.

### Data layer

`OnboardingRepository` already owns a DataStore of preferences. Either extend
it or, cleaner, add a sibling:

```
domain/settings/model/ThemeMode.kt        enum SYSTEM, LIGHT, DARK
domain/settings/model/AppLanguage.kt      enum SYSTEM, ENGLISH, ARABIC
domain/settings/repository/SettingsRepository.kt
domain/settings/usecase/SettingsUseCases.kt
data/settings/repository/SettingsRepositoryImpl.kt   (DataStore<Preferences>)
```

Bind it in `app/di/AppModule.kt` next to the other `@Binds`.

### Applying the theme

`AppTheme(useDarkTheme = ...)` is called in `MainActivity`. Read the stored
`ThemeMode` there and resolve:

```
SYSTEM -> isSystemInDarkTheme()
LIGHT  -> false
DARK   -> true
```

The activity already has a Hilt entry point; collect the preference with
`collectAsStateWithLifecycle` around the existing `AppTheme { }` call so a
change applies instantly with no restart.

### Applying the language

Use `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))`
— per-app language, supported on API 24+ through AppCompat. `SYSTEM` maps to
`LocaleListCompat.getEmptyLocaleList()`. Note this recreates the activity,
which is why `LocaleModule.provideAppLocale` is deliberately not a singleton
— that comment in `AppModule.kt` explains it.

### UI

`SectionRule` per group, `CutSurface` rows. Theme and language are
single-choice: render as a row of chamfered segmented chips rather than a
Material `RadioButton` list, so the screen matches the rest of the app.

### Wiring

- Add `SettingsRoute` to `Route.kt`, a `composable<SettingsRoute>` in
  `NavGraph.kt`.
- Add a settings gear to Home's hero, beside the existing account avatar
  (`HomeHero` → the `Row` holding the wordmark). Home already passes
  `onNavigateToAccount`; add `onNavigateToSettings` the same way.

### Acceptance

- Switching theme repaints instantly, both directions, no restart.
- Switching to العربية flips the app to RTL Arabic; the Cinzel eyebrows lose
  their letter-spacing automatically (`appTypography(isRtl = true)` already
  handles this).
- Sign out asks first, then drops to a fresh anonymous session.

---

## Stage 3 — Champion riddle

**Branch:** `feat/riddle-grid`

The biggest stage. The domain is already right; the presentation is raw and
the guess limit has to go.

### What already exists (do not rebuild)

`domain/game/` has the whole scoring engine:

- `ClueState` — `MATCH`, `PARTIAL`, `MISS`, `HIGHER`, `LOWER`
- `ClueAttribute` — `ROLE`, `RESOURCE`, `DAMAGE_TYPE`, `DIFFICULTY`, `RANGE`
- `Clue(attribute, state, displayValues)` — `displayValues` holds the
  *guess's* values, coloured by how they scored
- `GuessResult(championId, championName, imageFileName, patchVersion, isCorrect, clues)`
- `EvaluateGuessUseCase`, `PickDailyChampionUseCase`, `GameProgressRepository`

`GameRoundViewModel` / `GameRoundContract` already track `guesses`,
`outcome`, `stats`, splash zoom step, and ability icon.

### ⚠️ Data note — read before copying LoLdle

LoLdle's columns are Gender, Position, Species, Resource, Range type, Region,
Release year. **Data Dragon publishes none of gender, species, region or
release year.** Adding them means inventing data, which AGENTS.md §1
forbids outright.

So: match LoLdle's *grid presentation* — the coloured attribute columns, the
higher/lower arrows, the running list of guesses — using the five attributes
this app can actually source. That is exactly what `ClueAttribute` already
enumerates. Do not add columns beyond it, and do not hardcode a champion
metadata table to fake them.

### Changes

**1. Unlimited guesses.** `GameMode` currently carries
`maxGuesses` (CLASSIC 6, ABILITY 4, SPLASH 4). Remove the cap:

- Delete `maxGuesses` from `GameMode`, or keep it only as the splash-zoom
  schedule for `SPLASH`.
- `GameRoundState.guessesRemaining` goes away; replace with
  `guessCount: Int` so the UI can show "7 guesses" instead of a countdown.
- A round now ends only on a correct answer (`RoundOutcome.WON`) or an
  explicit give-up. Add `GameRoundEvent.GiveUpClicked`, confirmed with a
  dialog (§13), which reveals the answer.
- `SPLASH` zoom already keys off `guesses.size`; clamp it so it stops
  widening once fully revealed rather than going negative.

**2. The clue grid.** Replace the current guess list in `GameRoundScreen`
with a real grid:

- A sticky header row of Cinzel `eyebrow` column labels: Champion, Role,
  Resource, Damage, Difficulty, Range.
- One row per guess, newest first, each cell a `CutSurface`-style chamfered
  tile coloured by `ClueState`:
  - `MATCH` → `AppTheme.colors.success`
  - `PARTIAL` → `AppTheme.colors.warning`
  - `MISS` → `AppTheme.colors.error`
  - `HIGHER` / `LOWER` → `error` background plus an ▲ / ▼ icon
- First cell of each row is the champion's icon in a `HextechFrame`.
- The row must scroll horizontally as a unit with the header — put header and
  rows in one `Column` inside a shared `horizontalScroll` state, so columns
  stay aligned. Do **not** give each row its own scroll state.
- Add a colour-key legend (LoLdle has one) as a dismissible `CutSurface`.

Cells are small and coloured: state must not be conveyed by colour alone.
Keep the text value in every cell, and give higher/lower an arrow glyph.

**3. Ability and splash modes** use the same grid — that is the point. They
differ only in the prompt above it: `ABILITY` shows the ability icon in a
large `HextechFrame`; `SPLASH` shows the progressively-widening splash crop.

**4. Game hub.** `GameHubScreen` is currently three plain cards. Re-skin with
`CutSurface`, a Cinzel `tileLabel` per mode, and the mode's own art as a
`HextechFrame` thumbnail. Show each mode's streak from `ObserveGameStatsUseCase`.

### Acceptance

- A round accepts guess 7, 8, 20 without ending.
- Grid columns stay aligned while scrolling sideways.
- Give up asks first, then reveals.
- Colour + text + arrow all agree in every cell.

---

## Stage 4 — Timers

**Branch:** `feat/timers-summoner-spells`

Today `GameTimersScreen` has four objective presets (Baron, Dragon, Herald,
Ward) and nothing else. Add enemy summoner-spell tracking.

### Domain

`GameTimerPreset` is a hardcoded enum, and its doc comment explains why that
is correct here: objective timers are rules of the game, not patch data.
**Summoner spell cooldowns are different** — Data Dragon publishes them, and
`SummonerSpellRepository` already caches them with
`SummonerSpell.cooldownSeconds` and `cooldownWithHaste()`. Use the repository;
do not hardcode 300 for Flash.

Add:

```
domain/timer/model/EnemyLane.kt        enum TOP, JUNGLE, MID, BOTTOM, SUPPORT
domain/timer/model/SpellTimer.kt       lane + spellId + startedAt + durationSeconds
```

Extend `GameTimersState` with the five lanes, each holding two spell slots.

### UI

A "Enemy team" section under the objective timers:

- Five rows, one per lane, labelled with the lane name in Cinzel `tileLabel`.
- Each row has two spell slots. An empty slot opens a picker of Summoner's
  Rift spells (`SummonerSpell.isSummonersRift`) shown as `HextechFrame`
  icons — Flash, Ignite, Teleport, Heal, Exhaust, Barrier, Cleanse, Ghost,
  Smite.
- Tapping a filled slot starts its countdown; the icon desaturates and shows
  remaining time in `statValue` mono. Tapping again cancels (confirm, §13, since
  it destroys a running timer).
- Long-press a slot to clear the assignment.

The existing 500 ms tick loop in `GameTimersViewModel` drives these too —
extend the same `while (isActive)` block rather than adding a second timer.

### Also fix

`PresetGrid` and `ItemGrid` were converted from `LazyVerticalGrid` to plain
`Row`/`Column` grids in `ded97d0` because a lazy grid nested in a `LazyColumn`
item crashes on infinite height constraints. **Keep using plain grids inside
`LazyColumn` items.** Do not reintroduce `LazyVerticalGrid` there.

### Acceptance

- Assign Flash to enemy JG, start it, watch it count down and clear itself.
- Cooldowns come from the cached Data Dragon values, not literals.
- No crash when the section renders inside the existing `LazyColumn`.

---

## Stage 5 — Roulette

**Branch:** `feat/roulette-splash`

Small stage. `RouletteScreen` currently shows a plain card.

- Result becomes a full-bleed splash of the rolled champion, same treatment
  as Home's hero: `ContentScale.Crop`, vertical scrim dissolving into
  `AppTheme.colors.background`, name in `displayLarge`, title in `bodyMedium`.
- Roll again is a chamfered primary button over the art.
- Add a short reveal: cross-fade the splash and slide the name up
  (`AnimatedContent` keyed on champion id, 220 ms). Respect
  `LocalAccessibilityManager` / reduced motion by skipping the slide.
- Keep the existing filter behaviour and the empty-pool message; re-skin the
  empty state as a `CutSurface` rather than leaving it bare.

### Acceptance

- Rolling repeatedly cross-fades cleanly with no flash of background.
- Empty pool still explains itself.

---

## Stage 6 — Remaining screens

**Branch:** `feat/reskin-remaining`

Mechanical but wide. Apply the stage 1 language to everything left. Suggested
commit split: one commit per screen group.

### Screens

| Screen | Work |
| --- | --- |
| Champion list | Rows → `CutSurface`; portrait → `HextechFrame`; search field chamfered; filter sheet chips chamfered; `SectionRule` for the count header. |
| Champion detail | Splash header full-bleed like Home's hero; skin thumbnails → `HextechFrame` (they already have a selected border — pass `isSelected`); ability icons → `HextechFrame`; every section header → `SectionRule`; the saved-builds section from `fff2725` gets item icons instead of a bare count. |
| Items list + detail | Item icons → `HextechFrame`; build-path tree rows → `CutSurface`. |
| Favourites | Same treatment as champion list; re-skin the empty state. |
| Summoner search / profile | Rank crest, match rows and mastery rows → `CutSurface`; **put the account entry here** (the user asked for account to live in the summoner area, not Home). |
| Build simulator | Item slots → `HextechFrame`; results block → `CutSurface`. |
| Compare, Runes, Spells, Quiz, What's new, Ladder, Followed summoners, Live game, LP history, Clash | Straight token swap: bare `background + border` → `CutSurface`, section `Text` → `SectionRule`, art → `HextechFrame`. |
| Onboarding + sign-in gate | Chamfered buttons, Cinzel headings, so first launch matches the rest. |

### Two global items to finish here

**1. Bottom navigation.** `NavGraph.kt` still uses a stock Material
`NavigationBar` with a pill indicator, which is the last obviously-Material
surface in the app. Give it a chamfered indicator or a custom bar matching the
design system.

**2. Edge-to-edge.** `MainActivity` already calls `enableEdgeToEdge()`, but
the outer `Scaffold` in `NavGraph.kt` applies its full window insets to the
`NavHost`, so Home's hero cannot run under the status bar. To fix:

- Set `contentWindowInsets = WindowInsets(0)` on that outer `Scaffold`.
- Apply only the bottom-bar padding to the `NavHost`.
- Give Home's hero content a `statusBarsPadding()` so the wordmark and
  account avatar clear the clock, while the splash itself runs to the top.
- Then check every other top-level screen still clears the status bar —
  screens with their own `Scaffold` + `TopAppBar` will handle it themselves,
  but any screen without one needs `statusBarsPadding()` adding.

Do this one last and screenshot each tab, since it touches every screen.

### Acceptance

- No `RoundedCornerShape` left in `presentation/` outside `theme/`.
- No bare `AsyncImage` left where art is presented as content.
- Every screen legible in both themes and in Arabic RTL.

---

## Verification, every stage

```powershell
cd D:\projects\League-Of-Legends-Guide
.\gradlew.bat :app:assembleDebug :app:lintDebug --console=plain
.\gradlew.bat :app:installDebug --console=plain
```

Then screenshot the screen you changed, in **both** themes, and once in
Arabic. Commit only when all three look right.
