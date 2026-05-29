# Implementation Plan: Design System Migration + New Features (Pause, Language, Reminders, Stats, Data)

**Branch**: `003-design-migration-features` | **Date**: 2026-05-29 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/003-design-migration-features/spec.md`

---

## Summary

Bring the Day Counter app in line with the delivered **Claude Design** handoff and add the features
its screens assume. Two intertwined workstreams:

1. **Design-system migration (US1)** — replace the current mint-on-cool Material 3 theme with the
   handoff's warm brand: deep-teal `#0F5F6E` brand, cream `#FBF6EE` surfaces, white 24px squircle
   cards, pill buttons, the Outfit + Plus Jakarta Sans type pairing with a large tabular streak
   numeral, teal-tinted elevation, and the defined motion. Every existing surface (Contadores,
   Estadísticas, Ajustes, Detail, History, Create/Edit/Reset sheets, Celebration, Onboarding, Empty,
   Widget) is re-skinned with **no behavioral or accessibility regression**.

2. **New features** — **Pause/Resume** counters with *freeze & exclude* streak math plus Home filter
   chips (US2); an **in-app language picker** (English + Spanish, English default) (US3); a **daily
   reminder** with a time picker (US4); an **expanded Stats** tab with a Pausas card and weekly bars
   (US5); **Borrar todo** with undo (US6); and an **appearance** (dark-mode) control (US7).

This is a **theme + behaviour** iteration on top of `002-screens-and-navigation`. It reuses the
existing four-module Clean Architecture (`:domain` / `:data` / `:presentation` / `:app`) and the
Navigation 3 shell unchanged. It adds one new domain entity (`PausePeriod`), extends `Counter`
(`status`, `pausedSince`), bumps the Room schema (v2 → v3, `fallbackToDestructiveMigration` under the
spec's fresh-install assumption), introduces a `SettingsRepository` (language, appearance, reminder)
over DataStore, and adds a `DailyReminderWorker` (WorkManager). **No toolchain change is required** —
feature 002 already raised compileSdk 36 / AGP 8.9.1 and added Nav3 (stable), Glance 1.1.1, AppCompat
1.7.0, material-icons-extended, and KotlinX Serialization.

Per the resolved clarifications: **pause = freeze & exclude**; **language = English + Spanish only,
English default/fallback** (no constitution amendment for locale; pt/fr/de/it deferred).

> **✅ Constitution dynamic-color gate resolved.** Principle I was amended (**v2.2.0 → v2.3.0**,
> 2026-05-29): dynamic color (Material You) is now conditional — it MUST be supported *unless* the app
> adopts a defined brand design system, in which case the design system's palette is authoritative and
> dynamic color MAY be disabled (dark mode and "no hardcoded colors — all colors from the theme"
> remain mandatory). The Claude Design brand palette is therefore sanctioned; the deviation below is
> now an approved, constitution-backed choice.

---

## Technical Context

**Language/Version**: Kotlin 2.0.21 (Version Catalog `libs.versions.toml`) — constitution-mandated.

**Primary Dependencies**: Jetpack Compose BOM 2024.12.01, Jetpack Navigation 3 (`navigation3-runtime`
+ `navigation3-ui` 1.0.0, `lifecycle-viewmodel-navigation3` 2.10.0 — now **stable**, RC exception no
longer needed), Hilt 2.52, Room 2.6.1, DataStore Preferences 1.1.1, WorkManager 2.9.1, **Glance
1.1.1** (existing widget), **AppCompat 1.7.0** (per-app locale support), `compose-material-icons-
extended`, KotlinX Serialization 1.7.3, Coroutines/Flow 1.9.0, Kover 0.8.3, Detekt 1.23.7. **No new
libraries**: the two bundled fonts (Outfit, Plus Jakarta Sans) ship as `res/font` resources, not a
dependency; the progress ring, sparkline, calendar, weekly bars, and time-picker wheel are built from
Compose `material3` + `foundation` primitives (`Canvas`, `LazyVerticalGrid`, scrollable columns).

**Toolchain**: unchanged from 002 — `compileSdk`/`targetSdk` = 36, AGP 8.9.1, Gradle 8.11.1, `minSdk`
26. No bump in this feature.

**Storage**: Room — `Counter` (+ `status`, `paused_since` columns), **`PausePeriod`** (new table,
FK→counter ON DELETE CASCADE, index on `counter_id`), `MilestoneRecord`/`PastStreakRecord`/
`WidgetBinding` (unchanged). DataStore — existing onboarding flag + milestone toggle, **plus** a new
settings store: selected **language** (`en`/`es`), **appearance** (system/light/dark), **daily-
reminder enabled** + **time** (HH:mm). Schema bumped v2 → v3 with `fallbackToDestructiveMigration`
(fresh-install assumption, spec Assumptions).

**Testing**: JUnit 4 + Mockk (use cases, ViewModels), Hilt Test + Room in-memory (DAOs, repository
impls, pause/erase transaction atomicity, pause-period pagination), Compose Testing API (every
acceptance scenario across US1–US7), Turbine (StateFlow), Robolectric (locale-apply + reminder
scheduling where needed), WorkManager `work-testing` (DailyReminderWorker), Kover (≥ 80 % `:domain`
line coverage, must not decrease).

**Target Platform**: Android 8.0+ (API 26+), ~97 % of active devices.

**Project Type**: Multi-module Android application (existing 4 Gradle modules — no new module).

**Performance Goals**: Cold start to *Contadores* first paint < 2 s (locale + appearance applied
synchronously at startup like the existing onboarding read); language switch reflected within 2 s
(SC-004); paused day count stable across a date rollover (SC-003); daily reminder fires within 1 min
of the chosen local time (SC-009); 60 fps on ring/sparkline/weekly-bar animations.

**Constraints**: Fully offline (no network permission; Principle VI network rules N/A); release AAB
≤ 10 MB (two bundled font families add weight — verify against budget, subset if needed); streak/
effective-day math recomputed on `RESUMED`/`STARTED` lifecycle (no live clock observer); all colors
sourced from the theme (no hardcoded colors); **English is the default/base locale** (`res/values/
strings.xml`), Spanish in `res/values-es/strings.xml`; no hardcoded string literals in Composables/
ViewModels; `java.time` for all date math with explicit `ZoneId`.

**Scale/Scope**: Single user; up to 50 counters; 3 tabs + 3 full-screen destinations + sheets
(Create/Edit/Reset + new Language/ReminderTime/EraseAll); 6 milestone levels `{1,7,30,100,365,1000}`;
goal targets `{7,30,100,365}`; 2 languages; 32 functional requirements (+ addenda); 7 user stories.

**Migration posture**: Fresh-install only (spec Assumptions). Room v3 with
`fallbackToDestructiveMigration(true)`; no written migration, no backfill.

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-evaluated after Phase 1 design.*

| Principle | Gate Condition | Status | Notes |
|-----------|---------------|--------|-------|
| I — UX First | MD3; 48 dp targets; TalkBack labels; dark mode + **dynamic color (now conditional, v2.3.0)**; adaptive layouts; edge-to-edge; cold start < 2 s; localization (English default) | ✅ PASS | New brand theme is fully MD3-sourced (no hardcoded colors), dark mode supported (brand light+dark palettes), edge-to-edge retained, 48 dp targets, TalkBack labels for paused/milestone (text+shape, never color-only). **Dynamic color disabled in favor of the fixed brand palette — sanctioned by the v2.3.0 Principle I amendment** (brand-design-system allowance). English stays default locale (Q1) — no locale amendment. |
| II — TDD | Three test layers; ≥ 80 % `:domain` (Kover, non-decreasing); UI test per acceptance scenario; new UI tests fail first | ✅ PASS | Effective-streak math, pause/resume transaction, pause-stats, weekly-activity, erase/restore are unit-tested at use-case layer (pure functions where possible). Each US1–US7 acceptance scenario → a failing Compose test first. Locale apply and reminder scheduling get integration tests. |
| III — Clean Architecture | `:domain` zero `android.*`; `presentation → domain ← data`; `StateFlow`/`SharedFlow`; single-responsibility use cases | ✅ PASS | New use cases each single-purpose (`PauseCounterUseCase`, `ResumeCounterUseCase`, `CalculateEffectiveStreakUseCase`, `GetPauseStatsUseCase`, `GetWeeklyActivityUseCase`, `EraseAllDataUseCase`). `PausePeriod`, `CounterStatus`, settings models are pure Kotlin. `SettingsRepository` interface in `:domain`, impl in `:data`. Theme/locale/reminder infra lives in `:presentation`/`:data`/`:app` only. |
| IV — MAD Stack | Kotlin 2.x, Compose, ViewModel+StateFlow, Hilt, Coroutines+Flow, Navigation 3, Room, DataStore, **WorkManager**, Version Catalogs | ✅ PASS | Daily reminder uses **WorkManager** (`DailyReminderWorker`), not `AlarmManager`. New sheets are Nav3 bottom-sheet entries (existing `BottomSheetSceneStrategy`). Prefs via DataStore. No prohibited tech. Fonts are resources, not deps. |
| V — Code Quality | Lint zero errors; Detekt; R8 + resource shrinking; KDoc on public `:domain`/`:data`; no dead code | ✅ PASS | Old palette/type values **replaced** (not commented out). Superseded copy is removed. New public domain symbols get KDoc. AAB-size budget re-checked after fonts (subset Outfit/Jakarta to Latin if needed). |
| VI — Security & Privacy | `android:exported` explicit; no secrets; no PII in release logs; OWASP M4 input validation; deep-link sanitization | ✅ PASS | App stays offline → cleartext/cert-pinning N/A. No new exported components (reminder via WorkManager, no new Receiver/Activity beyond existing). Goal names/categories are user data — never in release logs; "Borrar todo" deletes locally. Category moves to a fixed chip set (bounded input). Existing deep link unchanged. |

**Result**: All gates pass. The former dynamic-color deviation is now **constitution-backed** by the
v2.3.0 Principle I amendment (2026-05-29); no open governance follow-up remains.

---

## Project Structure

### Documentation (this feature)

```text
specs/003-design-migration-features/
├── plan.md              # This file (/speckit-plan output)
├── research.md          # Phase 0 — theme tokens→MD3, dark-derivation, locale apply, pause math, reminder scheduling, undo
├── data-model.md        # Phase 1 — Counter/PausePeriod changes, effective-day formula, settings prefs, erase/restore
├── quickstart.md        # Phase 1 — build/run/test this iteration; manual verification scripts
├── contracts/
│   ├── ui-contract.md            # Per-screen UiState/UiEvent (Home filters, Detail pause, Settings sheets, Stats)
│   ├── domain-data-contract.md   # Use-case + repository signatures; DAO/transaction contracts
│   └── notifications-contract.md # Channels, daily-reminder scheduling, approaching-milestone
├── checklists/
│   └── requirements.md           # (pre-existing) requirements quality checklist
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created by /speckit-plan)
```

### Source Code (repository root) — changes layered onto the existing modules

```text
domain/src/main/kotlin/com/daycounter/domain/
├── model/
│   ├── Counter.kt                    # + status: CounterStatus, pausedSince: LocalDate?; + CATEGORIES set
│   ├── CounterStatus.kt              # NEW — enum ACTIVE / PAUSED
│   ├── PausePeriod.kt                # NEW — id, counterId, startDate, endDate; days = between(start,end)
│   ├── AppLanguage.kt                # NEW — enum EN, ES (+ default EN)
│   ├── AppearanceMode.kt             # NEW — enum SYSTEM / LIGHT / DARK
│   └── ReminderTime.kt               # NEW — hour:minute value object (5-min minute granularity)
├── repository/
│   ├── CounterRepository.kt          # + pause(id), resume(id), eraseAll(), restoreSnapshot(...)
│   ├── PausePeriodRepository.kt      # NEW — insert(), getForCounter(), totals (count, days)
│   └── SettingsRepository.kt         # NEW — language, appearance, dailyReminderEnabled, reminderTime flows + setters
└── usecase/
    ├── CalculateStreakUseCase.kt          # KEEP startDate overload; effective variant lives in use case below
    ├── CalculateEffectiveStreakUseCase.kt # NEW — elapsed(startDate→anchor) − completedPausedDays; anchor=pausedSince|today
    ├── PauseCounterUseCase.kt             # NEW — set status=PAUSED, pausedSince=today (idempotent)
    ├── ResumeCounterUseCase.kt            # NEW — bank PausePeriod(pausedSince→today), status=ACTIVE, clear pausedSince
    ├── GetPauseStatsUseCase.kt            # NEW — pausedNow, totalPausedDays, totalPauses
    ├── GetStatsSummaryUseCase.kt          # EXTEND — total (effective), best, active(excl paused), milestonesReached, avg
    ├── GetWeeklyActivityUseCase.kt        # NEW — 7-day "días cumplidos" bars (active, paused-excluded)
    └── EraseAllDataUseCase.kt             # NEW — snapshot + delete all; restore(snapshot) for undo

data/src/main/kotlin/com/daycounter/data/
├── database/
│   ├── AppDatabase.kt                # version 2→3, + PausePeriodEntity, fallbackToDestructiveMigration
│   ├── entity/
│   │   ├── CounterEntity.kt          # + status (String), paused_since (LocalDate?) columns + mappers
│   │   └── PausePeriodEntity.kt      # NEW — FK(counter_id) ON DELETE CASCADE, index(counter_id)
│   └── dao/
│       ├── CounterDao.kt             # + pause/resume @Transaction; eraseAll @Transaction; snapshot reads
│       └── PausePeriodDao.kt         # NEW — insert, selectForCounter, countAll, sumDaysAll
├── datastore/
│   └── SettingsPreferencesDataStore.kt   # NEW — language, appearance, dailyReminderEnabled, reminderTime
├── repository/
│   ├── CounterRepositoryImpl.kt      # + pause/resume/eraseAll/restore
│   ├── PausePeriodRepositoryImpl.kt  # NEW
│   └── SettingsRepositoryImpl.kt     # NEW (over SettingsPreferencesDataStore)
├── work/
│   ├── DailyReminderWorker.kt        # NEW — posts the daily reminder; reschedules next day
│   ├── DailyUpdateWorker.kt          # EXTEND — also evaluates approaching-milestone (FR-025b)
│   └── DailyReminderScheduler.kt     # NEW — enqueue/cancel based on enabled+time
└── di/DataModule.kt                  # + PausePeriod/Settings bindings, PausePeriodDao provider

presentation/src/main/kotlin/com/daycounter/presentation/
├── theme/
│   ├── Color.kt                      # REPLACE — brand light tokens + derived dark tokens
│   ├── DayCounterColors.kt           # NEW — extended semantic palette + LocalDayCounterColors CompositionLocal
│   ├── Shape.kt                      # NEW — Shapes(16/24/32) + pill helper
│   ├── Type.kt                       # REPLACE — Outfit (display/numeral) + Plus Jakarta Sans (body) + hero style
│   └── Theme.kt                      # wire brand scheme, drop dynamicColor default, appearance param, provide locals
├── home/                             # filter chips (Todos/Activos/Pausados), reskin, effective days
├── counter/                          # Detail pause/resume + paused ring/banner; Create/Edit category chips; reskin
├── stats/                            # Pausas card, weekly bars, racha media, hitos count; reskin
├── history/                          # reskin (pauses NOT archived)
├── settings/                         # grouped sections; Idioma sheet; ReminderTime sheet; dark-mode toggle; Borrar todo + undo toast
├── celebration/                      # reskin + "Compartir" action
├── onboarding/                       # reskin + updated copy
├── components/
│   ├── ProgressRing.kt               # + paused (dashed/muted) state
│   ├── Sparkline.kt / MonthCalendarGrid.kt  # reskin to tokens
│   ├── WeeklyBars.kt                 # NEW — 7-bar weekly activity
│   ├── TimePickerWheel.kt            # NEW — scrollable hour/minute + presets
│   └── (Chip/Sheet/Toast styling aligned to tokens)
├── navigation/
│   ├── NavKeys.kt                    # + LanguageSheet, ReminderTimeSheet, EraseAllSheet keys
│   └── MainScaffold.kt / AppNavDisplay.kt  # register new sheet entries
├── locale/
│   └── LocaleManager.kt              # NEW — apply selected AppLanguage (Configuration/ContextWrapper)
└── MainActivity.kt                   # attachBaseContext locale wrap + appearance read at startup; recreate on change

presentation/src/main/res/
├── font/                             # NEW — outfit_*.ttf, plus_jakarta_sans_*.ttf + font-family XML
├── values/strings.xml                # + new strings (pause, filters, language, reminder, stats, erase, appearance, share)
├── values-es/strings.xml             # + Spanish for all new strings (informal "tú", no emoji)
└── xml/locales_config.xml            # NEW — <locale-config> en, es (referenced from app manifest)

app/src/main/
├── AndroidManifest.xml               # android:localeConfig="@xml/locales_config"; daily-reminder channel
└── kotlin/com/daycounter/DayCounterApplication.kt  # + daily-reminder NotificationChannel; schedule reminder on init

# Tests (mirror per module)
domain/src/test/...   # effective-streak, pause/resume, pause-stats, weekly-activity, erase/restore
data/src/test/...     # PausePeriodDao, pause/resume + eraseAll transaction, settings datastore, DailyReminderWorker
presentation/src/test|androidTest # Compose tests per US1–US7 acceptance scenario; locale-apply; theme
```

**Structure Decision**: Reuse the existing four-module layout and the Navigation 3 shell from `002`
unchanged — a theme + behaviour iteration justifies no new module. New screens are additive packages/
sheets under `:presentation`; the theme package is overhauled in place. Pause introduces one new
`:domain` entity (`PausePeriod`) and a `Counter` extension; a `SettingsRepository` abstracts the new
preferences. The daily reminder uses WorkManager. Reusable visuals (ring, sparkline, calendar, weekly
bars, time wheel) live in `components/` so all screens share one implementation each.

---

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| **Dynamic color (Material You) disabled** in favor of a fixed brand palette — **sanctioned by Principle I v2.3.0** (no longer a deviation) | The Claude Design brand identity is a *fixed* warm teal + cream system; the handoff explicitly rejects the Material 3 mint/Material You surface ("its default surface color is Material 3 mint-green, which fights the cream/teal palette"). Brand cohesion is the entire point of the migration. | Keeping Material You as the default destroys the brand. Offering it as an opt-in toggle still risks an off-brand experience and doubles theming/QA surface for a single-user app whose identity is the brand palette. **Resolved: Principle I amended (v2.2.0 → v2.3.0, 2026-05-29)** to make dynamic color conditional when a defined brand design system is adopted — mirroring the v2.2.0 Navigation 3 amendment. No longer a tracked exception. |
| `fallbackToDestructiveMigration` (v2→v3) instead of a written Room migration | Spec assumes fresh install, no production data (Assumptions). Adding `status`/`paused_since` columns + a `PausePeriod` table would otherwise need a multi-step migration. | A hand-written migration is more code/test surface for data that, per the spec, does not exist; speculative and untestable. Consciously recorded; revisit if an installed base must be preserved. |
| Two bundled font families (Outfit, Plus Jakarta Sans) added to the AAB | The brand type pairing is core to the design (display/numeral vs. body). System fonts cannot reproduce it. | Downloadable Fonts adds a runtime provider dependency and first-paint risk for an offline app; bundling is simpler and offline-safe. Mitigation: subset to Latin glyphs to stay under the 10 MB AAB budget. |
