# Implementation Plan: Screens and Navigation Overhaul

**Branch**: `002-screens-and-navigation` | **Date**: 2026-05-28 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-screens-and-navigation/spec.md`

---

## Summary

Restructure Day Counter around a **three-tab bottom-navigation shell** (Contadores ·
Estadísticas · Ajustes) and deliver the full functional surface that the design-system
rework assumes: an enriched counter list with progress rings and milestone badges, a
detail screen exposing the already-implemented Reset/Delete domain operations plus
next-milestone hints and an achieved-milestones list, Create/Edit refactored as bottom
sheets with two new counter fields (`category`, `goalMilestoneTarget`), a Stats tab,
a per-counter History/Calendar screen, a full-screen Milestone Celebration overlay, and
persistence of **past streaks** so prior attempts survive a reset.

This is a **behaviour-and-navigation** iteration on top of `001-day-counter-app`; the
visual design rework is a separate downstream workstream. The work reuses the existing
four-module Clean Architecture (`:domain` / `:data` / `:presentation` / `:app`), adds one
new domain entity (`PastStreakRecord`), extends two existing entities (`Counter`,
`MilestoneRecord`), and changes the milestone set from `{7, 30, 60, 90, 180, 365}` to
`{1, 7, 30, 100, 365, 1000}`.

This iteration also **migrates the app from Jetpack Navigation 2 (Navigation Compose) to
Jetpack Navigation 3** (`NavDisplay` + `entryProvider` + an explicit, developer-owned back
stack). The three-tab shell is built with the Navigation 3 multi-back-stack pattern
(`TopLevelBackStack`/`NavigationState`); Detail, History, and Celebration are full-screen
entries; Create/Edit/Reset become bottom-sheet entries via a copied-in `BottomSheetSceneStrategy`;
and the existing `daycounter://counter/{id}` deep link is rebuilt as a synthetic back stack.
**Toolchain consequence**: Navigation 3 requires `compileSdk = 36`, which forces an **AGP
upgrade** (current AGP 8.7.3 caps at compileSdk 35 → bump to an AGP version supporting
compileSdk 36, i.e. AGP 8.9+). String routes are replaced by type-safe `@Serializable … : NavKey`
keys (KotlinX Serialization is already on the classpath).

Per constitution Localization, the **default locale stays English**: base
`res/values/strings.xml` holds English copy and a new `res/values-es/strings.xml` variant
holds the verbatim Spanish copy required by FR-032 (milestone messages, informal "tú", no
emoji). No string literals are hardcoded in Composables/ViewModels.

Because the spec mandates a **fresh-install assumption** (no production data), the Room
schema version is bumped with a `fallbackToDestructiveMigration` rather than a written
migration — no backfill or preservation of the old milestone set is required.

---

## Technical Context

**Language/Version**: Kotlin 2.0.21 (Version Catalog `libs.versions.toml`) — constitution-mandated

**Primary Dependencies**: Jetpack Compose BOM 2024.12.01, **Jetpack Navigation 3**
(`androidx.navigation3:navigation3-runtime` + `navigation3-ui` `1.0.0`) **replacing
Navigation Compose 2.8.5**, plus the ViewModel add-on
`androidx.lifecycle:lifecycle-viewmodel-navigation3` `2.10.0-rc01` (for `NavEntry`-scoped
ViewModels), KotlinX Serialization (already present — for `@Serializable … : NavKey` routes),
Hilt 2.52 (ViewModel keys via assisted injection + `creationCallback`), Room 2.6.1,
DataStore Preferences 1.1.1, WorkManager 2.9.1 (existing milestone notifier — unchanged),
Coroutines + Flow 1.9.0, `compose-material-icons-extended` (tab + action icons), Kover 0.8.3,
Detekt 1.23.7. **No charting/calendar third-party dependency is introduced** — bottom
navigation, calendar grid, progress ring, and sparkline are built from Compose `material3` +
`foundation` primitives (`Canvas`, `LazyVerticalGrid`). The only **new** libraries are the
Navigation 3 artifacts; `navigation-compose` is removed. The `BottomSheetSceneStrategy` is
copied into the project (it is not yet part of core Navigation 3).

**Toolchain**: `compileSdk`/`targetSdk` raised **35 → 36** (Navigation 3 requirement); **AGP
upgraded** from 8.7.3 to a version supporting compileSdk 36 (AGP 8.9+); `minSdk` stays 26.

**Storage**: Room — `Counter` (+ `category`, `goal_milestone_target` columns),
`MilestoneRecord` (+ `celebration_shown` column; milestone set redefined),
`PastStreakRecord` (new table, FK→counter ON DELETE CASCADE), `WidgetBinding` (unchanged).
DataStore — onboarding-shown flag and milestone-notification toggle (both unchanged).

**Testing**: JUnit 4 + Mockk (domain use cases, ViewModels), Hilt Test + Room in-memory
(DAOs, repository impls, reset transaction atomicity), Compose Testing API (every acceptance
scenario across US1–US8), Turbine (StateFlow assertions), Kover (≥ 80 % `:domain` line coverage).

**Target Platform**: Android 8.0+ (API 26+), ~97 % of active devices.

**Project Type**: Multi-module Android application (existing 4 Gradle modules — no new module).

**Performance Goals**: Cold start to *Contadores* first paint < 2 s (SC-002); navigate to any
in-scope surface in ≤ 3 taps (SC-003); milestone celebration auto-launches 100 % of the time
on the next Detail open and never twice (SC-004); past streak visible in History same session
(SC-005); notification toggle effect < 1 s and survives cold restart (SC-007).

**Constraints**: Fully offline — no network permission (Principle VI network rules N/A);
release AAB ≤ 10 MB; streaks recomputed on `RESUMED`/`STARTED` lifecycle (no live clock
observer); History "Rachas anteriores" paginates client-side in batches of 50. Localization:
**English is the default locale** (`res/values/strings.xml`), with a **`res/values-es/strings.xml`**
variant carrying the verbatim Spanish copy (informal "tú", no emoji) mandated by FR-032; no
hardcoded string literals in Composables/ViewModels.

**Scale/Scope**: Single user; up to 50 counters; 3 top-level tabs + 3 full-screen
destinations + 3 bottom sheets; 6 milestone levels `{1, 7, 30, 100, 365, 1000}`; goal-target
choices `{7, 30, 100, 365}`; 32 functional requirements; 8 user stories.

**Migration posture**: Fresh-install only (spec Assumptions + Clarifications). Room version
bumped with `fallbackToDestructiveMigration(true)`; no written migration, no backfill.

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-evaluated after Phase 1 design.*

| Principle | Gate Condition | Status | Notes |
|-----------|---------------|--------|-------|
| I — UX First | MD3 theme; 48 dp touch targets; TalkBack content descriptions; dark mode + dynamic color; adaptive layouts; edge-to-edge; cold start < 2 s; **Localization (English default + externalized strings)** | ✅ PASS | Bottom nav, sheets, rings, calendar all MD3. Progress ring and "Hito alcanzado" badge convey state with text + shape, never color alone. Bottom sheets respect IME + navigation-bar insets. **Localization**: default locale stays English (`values/strings.xml`); Spanish copy lives in `values-es/strings.xml` (FR-032) — additive locale, no hardcoded literals. |
| II — TDD | Three test layers; ≥ 80 % `:domain` (Kover); UI test per acceptance scenario; new UI tests fail before screen exists | ✅ PASS | 31 acceptance scenarios across US1–US8 + 13 edge cases → failing tests first. Reset-archives-then-clears-milestones transaction and celebration-dedup are unit-tested at the use-case layer; calendar/sparkline/ring math unit-tested as pure functions. |
| III — Clean Architecture | `:domain` zero `android.*`; `presentation → domain ← data`; `StateFlow`/`SharedFlow`; single-responsibility use cases | ✅ PASS | New use cases (`ArchiveAndResetCounterUseCase`, `GetStatsSummaryUseCase`, `GetNextMilestoneUseCase`, `GetAchievedMilestonesUseCase`, `MarkCelebrationsShownUseCase`, `GetPastStreaksUseCase`) each single-purpose. `PastStreakRecord` is pure Kotlin. Calendar/sparkline are UI-only; their data comes from domain. |
| IV — MAD Stack | Kotlin 2.x, Compose, ViewModel+StateFlow (no LiveData/RxJava), Hilt, Coroutines+Flow, **Navigation 3 (`NavDisplay`/`entryProvider`)** (constitution v2.2.0), Room, DataStore, WorkManager, Version Catalogs | ✅ PASS | Constitution **amended to v2.2.0** to make Navigation 3 the mandated Navigation technology — so Nav3 is now explicitly sanctioned (no longer a deviation). Bottom nav = `NavigationBar` + `TopLevelBackStack`. Sheets = `ModalBottomSheet` via `BottomSheetSceneStrategy`. No charting/calendar lib — `Canvas`/`LazyVerticalGrid` only. **Documented exceptions** (Complexity Tracking, both sanctioned by constitution): `lifecycle-viewmodel-navigation3` pinned to RC `2.10.0-rc01` (Nav3 add-on RC exception, v2.2.0); `compileSdk 36` + **minimal AGP bump to 8.9+** (AGP 9 explicitly out of scope — separate workstream). |
| V — Code Quality | Lint zero errors; Detekt configured; R8 + resource shrinking; KDoc on public `:domain`/`:data`; no dead code | ✅ PASS | Old Home→Edit navigation and `EditCounterScreen` full-screen route are **removed** (not commented out) per no-dead-code rule when superseded by the Edit sheet. New public domain symbols get KDoc. |
| VI — Security & Privacy | `android:exported` explicit; no secrets; no PII logged in release; OWASP M4 input validation; deep-link sanitization | ✅ PASS | App stays offline → cleartext/cert-pinning N/A. No new exported components (no new Activity/Receiver). Existing `daycounter://counter/{id}` deep link retained; `counterId` parsed to `Long` and validated (missing → return to Contadores). Goal names / categories are user data — must not appear in release logs. Category free-text bounded to 0–50 chars (input validation). |

**Post-Phase-1 re-check**: No violations introduced. The reset operation is a single Room
`@Transaction` (archive past streak → delete milestone rows → set start_date=today),
preserving atomicity (FR-017). Celebration dedup uses the existing
`UNIQUE(counter_id, milestone_days)` index plus the new `celebration_shown` flag; no new
exported surface, no new permission. `fallbackToDestructiveMigration` is acceptable **only**
under the spec's explicit fresh-install assumption and is recorded in Complexity Tracking.

---

## Project Structure

### Documentation (this feature)

```text
specs/002-screens-and-navigation/
├── plan.md              # This file (/speckit-plan output)
├── research.md          # Phase 0 — decisions: nav shell, sheets, celebration dedup, calendar, migration
├── data-model.md        # Phase 1 — Counter/MilestoneRecord changes + PastStreakRecord, reset transaction
├── quickstart.md        # Phase 1 — how to build/run/test this iteration; manual verification scripts
├── contracts/
│   ├── navigation-contract.md   # Nav3 route keys, entryProvider, TopLevelBackStack, bottom-bar visibility, deep link
│   └── ui-contract.md           # Screen/sheet/overlay state + event contracts (ViewModel UiState/UiEvent)
├── checklists/
│   └── requirements.md          # (pre-existing) requirements quality checklist
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created by /speckit-plan)
```

### Source Code (repository root) — changes layered onto the existing modules

```text
domain/src/main/kotlin/com/daycounter/domain/
├── model/
│   ├── Counter.kt                    # + category: String?, goalMilestoneTarget: Int
│   ├── MilestoneRecord.kt            # MILESTONE_DAYS = {1,7,30,100,365,1000}; + celebrationShown: Boolean
│   └── PastStreakRecord.kt           # NEW — id, counterId, streakDays, reason, endDate, createdAt
├── repository/
│   ├── CounterRepository.kt          # (unchanged signature; impl handles new columns)
│   ├── MilestoneRepository.kt        # + getForCounter(), markAllShownForCounter()
│   └── PastStreakRepository.kt       # NEW — insert(), getForCounterPaged(), (cascade delete via FK)
└── usecase/
    ├── ResetCounterUseCase.kt        # REPLACED/renamed → ArchiveAndResetCounterUseCase (transactional)
    ├── GetStatsSummaryUseCase.kt     # NEW — total accumulated, best streak, active count
    ├── GetNextMilestoneUseCase.kt    # NEW — smallest milestone strictly > current streak
    ├── GetAchievedMilestonesUseCase.kt   # NEW — milestones ≤ current streak (current attempt)
    ├── GetMostRecentMilestoneUseCase.kt  # NEW — highest milestone ≤ current streak (Revivir)
    ├── MarkCelebrationsShownUseCase.kt   # NEW — set celebration_shown=true for all rows of a counter
    └── GetPastStreaksUseCase.kt          # NEW — paged past streaks, newest end_date first

data/src/main/kotlin/com/daycounter/data/
├── database/
│   ├── AppDatabase.kt                # version++, + PastStreakRecordEntity, fallbackToDestructiveMigration
│   ├── entity/
│   │   ├── CounterEntity.kt          # + category, goal_milestone_target columns
│   │   ├── MilestoneRecordEntity.kt  # + celebration_shown column
│   │   └── PastStreakRecordEntity.kt # NEW — FK(counter_id) ON DELETE CASCADE, index on (counter_id, end_date)
│   └── dao/
│       ├── CounterDao.kt
│       ├── MilestoneRecordDao.kt     # + selectForCounter, markAllShownForCounter
│       ├── PastStreakRecordDao.kt    # NEW — insert, pagedByCounter(limit, offset)
│       └── ResetDao.kt (or @Transaction in CounterDao) # archive+clear+reset in one transaction
├── repository/
│   ├── MilestoneRepositoryImpl.kt    # + new methods
│   └── PastStreakRepositoryImpl.kt   # NEW
└── di/DataModule.kt                  # + PastStreakRepository binding, PastStreakRecordDao provider

presentation/src/main/kotlin/com/daycounter/presentation/
├── navigation/
│   ├── NavKeys.kt                    # NEW — @Serializable NavKey routes (replaces string-based Screen.kt):
│   │                                 #   TopLevel: Contadores/Estadisticas/Ajustes; Detail/History/Celebration;
│   │                                 #   sheet keys: CreateCounter/EditCounter(id)/ResetConfirm(id)
│   ├── TopLevelBackStack.kt          # NEW — multi-back-stack holder (one stack per tab) + flattened back stack
│   ├── BottomSheetSceneStrategy.kt   # NEW — copied from Nav3 recipe (not yet in core); renders sheet entries
│   ├── DeepLinkResolver.kt           # NEW — parse daycounter://counter/{id} → synthetic back stack [Contadores, Detail]
│   ├── AppNavDisplay.kt              # NEW — NavDisplay(entryProvider, sceneStrategies, entryDecorators) (replaces AppNavGraph/NavHost)
│   └── MainScaffold.kt               # NEW — Scaffold; NavigationBar bottomBar shown only when visible entry is a tab key
├── home/
│   ├── HomeScreen.kt                 # summary cards + ring/badge cards; tap → Detail (NOT Edit); "+" → Create sheet
│   └── HomeViewModel.kt              # + summary state; lifecycle-resume streak recompute
├── stats/
│   ├── StatsScreen.kt                # NEW — Total acumulado hero + Mejor racha + Contadores activos; empty state
│   └── StatsViewModel.kt             # NEW — GetStatsSummaryUseCase
├── counter/
│   ├── CounterDetailScreen.kt        # hero ring, next-milestone hint, achieved chips, 4–5 actions
│   ├── CounterDetailViewModel.kt     # NEW (split from screen) — auto-launch celebration logic, recompute on resume
│   ├── CreateCounterSheet.kt         # NEW — bottom-sheet ENTRY (BottomSheetSceneStrategy.bottomSheet() metadata)
│   ├── EditCounterSheet.kt           # NEW — bottom-sheet entry, start-date read-only (replaces EditCounterScreen)
│   ├── ResetConfirmSheet.kt          # NEW — bottom-sheet entry: warning + Confirmar/Cancelar
│   ├── CreateCounterViewModel.kt     # + category, goalMilestoneTarget fields + validation
│   └── EditCounterViewModel.kt       # + category, goalMilestoneTarget; start date read-only
├── celebration/
│   ├── MilestoneCelebrationScreen.kt # NEW — full-screen overlay, animated ring, copy table, "Seguir así"/X
│   └── MilestoneCelebrationViewModel.kt # NEW — resolve milestone + copy; mark shown
├── history/
│   ├── HistoryScreen.kt              # NEW — header+sparkline, current-month calendar grid, paged "Rachas anteriores"
│   └── HistoryViewModel.kt           # NEW — calendar model, sparkline points, GetPastStreaksUseCase pagination
├── settings/
│   ├── SettingsScreen.kt             # unchanged behaviour; now hosted as the Ajustes TAB (not top-bar icon)
│   └── SettingsViewModel.kt          # unchanged
└── components/
    ├── ProgressRing.kt               # NEW — Canvas ring (fill = days/target), reused by Home/Detail/Celebration
    ├── Sparkline.kt                  # NEW — Canvas polyline of streak growth
    └── MonthCalendarGrid.kt          # NEW — LazyVerticalGrid current-month view with cell states

presentation/src/test/kotlin/com/daycounter/presentation/   # Compose UI tests per acceptance scenario
domain/src/test/kotlin/com/daycounter/domain/usecase/        # unit tests for all new use cases
data/src/test/kotlin/com/daycounter/data/                    # reset-transaction + pagination integration tests
```

**Navigation 3 wiring** (in `:app`/`:presentation`): `NavDisplay` receives `entryProvider`
(maps each `NavKey` → `NavEntry`), `sceneStrategies = [BottomSheetSceneStrategy(), …]`, and
`entryDecorators = [rememberSaveableStateHolderNavEntryDecorator(),
rememberViewModelStoreNavEntryDecorator()]` so each entry gets a correctly-scoped, saveable
ViewModel. Counter-id arguments reach ViewModels via Hilt `@AssistedInject` + `hiltViewModel(
creationCallback = { it.create(key) })`. Build dependency catalogs (`libs.versions.toml`) and
the `:app`/`:presentation` `build.gradle.kts` add the nav3 artifacts and remove
`navigation-compose`; `compileSdk`/`targetSdk` → 36 and the AGP version is bumped.

**Structure Decision**: Reuse the existing four-module layout from `001` unchanged — no new
Gradle module is justified for a UI/behaviour iteration. New screens are added as packages
under `:presentation`. Navigation moves to **Navigation 3**: a single `NavDisplay` renders a
developer-owned, flattened back stack produced by `TopLevelBackStack` (one stack per tab).
`MainScaffold` wraps `NavDisplay` and shows the `NavigationBar` **only when the currently
visible entry is one of the three tab keys** (bottom bar hidden on Detail/History/Celebration —
FR-001/FR-002). Reusable visual primitives (ring, sparkline, calendar) live in a `components/`
package so Home, Detail, Celebration, and History share one implementation each.
Create/Edit/Reset are modelled as **bottom-sheet entries** (NavKeys carrying
`BottomSheetSceneStrategy.bottomSheet()` metadata) rather than hoisted `ModalBottomSheet`
state — backdrop/back dismissal pops the entry (FR-030). The old string-based `Screen.kt`,
`AppNavGraph.kt` (NavHost), and the full-screen `CreateCounterScreen`/`EditCounterScreen`
are deleted (no-dead-code, Principle V).

---

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| `fallbackToDestructiveMigration` instead of a written Room migration | Spec explicitly assumes fresh install with no production data (Clarifications 2026-05-28 Q1; Assumptions). The milestone set and three schema changes would otherwise demand a multi-step migration + backfill. | A hand-written migration is more code and test surface for data that, per the spec, does not exist. Writing one would be speculative and untestable against real data. Recorded here so it is a conscious, spec-sanctioned exception — to be revisited if an installed base ever needs preserving. |
| `lifecycle-viewmodel-navigation3` pinned to `2.10.0-rc01` (a release candidate, not a stable release) — deviation from Principle IV / Technical-Standards "latest stable" rule | It is the only artifact that provides `NavEntry`-scoped ViewModels for Navigation 3; the user has requested the Navigation 3 migration and the app depends on per-destination ViewModels. No stable release of this add-on exists yet. | Hand-rolling `NavEntry` ViewModel scoping (custom decorator + factory) duplicates the add-on, is error-prone, and would itself be unsanctioned. The RC is pinned to an exact version in the catalog and will be moved to stable as soon as it ships. |
| `compileSdk`/`targetSdk` 35 → 36 and a **minimal AGP upgrade** (8.7.3 → 8.9+, the lowest AGP that targets compileSdk 36) | Navigation 3 requires compileSdk 36; the current AGP caps at 35. The constitution mandates "compileSdk = targetSdk = latest stable" and "AGP latest stable", so this is alignment, but it is a cross-cutting toolchain change worth surfacing. | **AGP 9 is explicitly out of scope for this feature** (decided with the user): it would force Kotlin 2.0.21→2.2.10 (built-in Kotlin), KSP/Hilt bumps, Gradle 9.1, the new DSL, and R8 keep-rule changes, plus an interactive AGP Upgrade Assistant step — a large, risky migration unrelated to this UI/behaviour feature. It is deferred to a separate maintenance workstream (its own spec/branch) using the `agp-9-upgrade` skill. Staying on Navigation 2 to avoid the bump entirely was rejected by the user's explicit request to adopt Navigation 3. The exact 8.x version (and Detekt/Kover compatibility) MUST be verified before pinning in `libs.versions.toml`. |
| `BottomSheetSceneStrategy` copied into the project (vendored source, not a dependency) | The bottom-sheet Scene is not yet part of core Navigation 3; the official path (Nav3 migration guide + recipe) is to copy the strategy class in. FR-013 requires sheet presentation. | A third-party bottom-sheet-nav library would add an unjustified dependency (Principle V) and diverge from the official guidance. The vendored file is small, attributed to its source, and removed once an equivalent ships in core. |
