---
description: "Task list for 003-design-migration-features"
---

# Tasks: Design System Migration + New Features (Pause, Language, Reminders, Stats, Data)

**Input**: Design documents from `/specs/003-design-migration-features/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: REQUIRED — the constitution (Principle II, NON-NEGOTIABLE) mandates TDD with a UI test per
acceptance scenario and ≥ 80 % `:domain` coverage. Test tasks are written FIRST and must FAIL before
implementation.

**Organization**: By user story (US1–US7) for independent implementation/testing. Module roots:
`domain/`, `data/`, `presentation/`, `app/`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: parallelizable (different files, no incomplete-task dependency)
- **[Story]**: US1–US7 for story phases only

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Resources and config the theme + features rely on. No new Gradle dependencies.

- [ ] T001 [P] Add Outfit and Plus Jakarta Sans `.ttf` (Latin + Spanish-diacritic subset) under `presentation/src/main/res/font/` with font-family XML (`outfit.xml`, `plus_jakarta_sans.xml`)
- [X] T002 [P] Create `presentation/src/main/res/xml/locales_config.xml` listing `en` and `es`
- [X] T003 Reference `android:localeConfig="@xml/locales_config"` in `app/src/main/AndroidManifest.xml`
- [X] T004 [P] `androidx.appcompat` already a dependency of `:presentation` (verified)
- [X] T005 [P] Room v3 `exportSchema` verified — `:data` build/tests pass with `version = 3` (schema generation OK)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Theme system, Room v3 schema, effective-streak math, and shared preferences that MULTIPLE
stories depend on. **⚠️ No user story work begins until this phase is complete.**

### Theme system (blocks all UI stories, esp. US1)

- [X] T006 [P] Replace brand palette (light + derived dark tokens) in `presentation/src/main/kotlin/com/daycounter/presentation/theme/Color.kt` per research R1/R2
- [X] T007 [P] Create extended semantic palette + `LocalDayCounterColors` CompositionLocal in `presentation/src/main/kotlin/com/daycounter/presentation/theme/DayCounterColors.kt`
- [X] T008 [P] Create `presentation/src/main/kotlin/com/daycounter/presentation/theme/Shape.kt` (Shapes 8/12/16/24/32 + pill helper)
- [~] T009 Replace `Type.kt` with the brand type scale (display + tabular `HeroNumeralStyle` + body/label). **Scale done**; family swap to Outfit/Plus Jakarta Sans is a drop-in once font binaries (T001) are added — currently `FontFamily.Default` with a TODO
- [X] T010 Update `presentation/src/main/kotlin/com/daycounter/presentation/theme/Theme.kt`: wire brand `ColorScheme`, `dynamicColor=false`, provide `LocalDayCounterColors`/`Shapes`/typography (depends on T006–T009)
- [X] T011 [P] Add teal-tinted `Modifier.cardShadow()` helper in `presentation/src/main/kotlin/com/daycounter/presentation/components/Elevation.kt`

### Domain models & effective-streak math (blocks US2, US5)

- [X] T012 [P] Add `CounterStatus` enum in `domain/src/main/kotlin/com/daycounter/domain/model/CounterStatus.kt`
- [X] T013 [P] Add `PausePeriod` model in `domain/src/main/kotlin/com/daycounter/domain/model/PausePeriod.kt`
- [X] T014 [P] Add `AppLanguage`, `AppearanceMode`, `ReminderTime` models in `domain/src/main/kotlin/com/daycounter/domain/model/`
- [X] T015 Extend `Counter` with `status`, `pausedSince`, and `CATEGORIES` keys in `domain/src/main/kotlin/com/daycounter/domain/model/Counter.kt` (depends on T012)
- [X] T016 [P] Unit test (FAIL first) `CalculateEffectiveStreakUseCaseTest` in `domain/src/test/kotlin/com/daycounter/domain/usecase/` (same-day=0; 1 Jan→pause 10→resume 20 = 9; paused freeze across rollover; multiple pauses; pause day 0)
- [X] T017 Implement `CalculateEffectiveStreakUseCase` in `domain/src/main/kotlin/com/daycounter/domain/usecase/CalculateEffectiveStreakUseCase.kt` (depends on T015, T016)

### Room v3 schema (blocks US2, US5, US6)

- [X] T018 Add `CounterStatus` TypeConverter in `data/src/main/kotlin/com/daycounter/data/database/converter/Converters.kt`
- [X] T019 Extend `CounterEntity` with `status` + `paused_since` columns and update mappers in `data/src/main/kotlin/com/daycounter/data/database/entity/CounterEntity.kt` (depends on T015, T018)
- [X] T020 [P] Create `PausePeriodEntity` (FK→counter ON DELETE CASCADE, index on `counter_id`) + mappers in `data/src/main/kotlin/com/daycounter/data/database/entity/PausePeriodEntity.kt`
- [X] T021 Create `PausePeriodDao` (`insert`, `selectForCounter`, `selectAll`, cascade-delete) in `data/src/main/kotlin/com/daycounter/data/database/dao/PausePeriodDao.kt`
- [X] T022 Bump `AppDatabase` to `version = 3`, register `PausePeriodEntity` + `pausePeriodDao()`, keep `fallbackToDestructiveMigration(true)` in `data/src/main/kotlin/com/daycounter/data/database/AppDatabase.kt` (depends on T019–T021)
- [X] T023 [P] Integration test `PausePeriodDaoTest` in `data/src/test/kotlin/com/daycounter/data/database/` (insert/select/days; CASCADE delete; status round-trip)

### Shared preferences (blocks US3, US4, US7)

- [X] T024 [P] Add `SettingsRepository` interface (language, appearance, dailyReminderEnabled, reminderTime flows + setters) in `domain/src/main/kotlin/com/daycounter/domain/repository/SettingsRepository.kt`
- [X] T025 Create `SettingsPreferencesDataStore` (`settings_prefs`: language/appearance/daily_reminder_enabled/daily_reminder_time) in `data/src/main/kotlin/com/daycounter/data/datastore/SettingsPreferencesDataStore.kt`
- [X] T026 Implement `SettingsRepositoryImpl` + Hilt binding in `data/src/main/kotlin/com/daycounter/data/repository/SettingsRepositoryImpl.kt` and `data/src/main/kotlin/com/daycounter/data/di/DataModule.kt` (depends on T024, T025)
- [X] T027 [P] Integration test `SettingsPreferencesDataStoreTest` in `data/src/test/kotlin/com/daycounter/data/datastore/` (defaults: en/system/false/09:00; round-trip)

### Shared repository surface

- [X] T028 [P] Add `PausePeriodRepository` interface in `domain/.../repository/PausePeriodRepository.kt` and impl + DI in `data/.../repository/PausePeriodRepositoryImpl.kt`
- [X] T029 Extend `CounterRepository` interface — pause/resume **and** eraseAll/restore added (interface + `CounterDao` `@Transaction` + impl); completed with US6

**Checkpoint**: Theme, schema, effective-streak math, and preferences are ready — user stories can begin.

---

## Phase 3: User Story 1 - Refreshed visual design across the whole app (Priority: P1) 🎯 MVP

**Goal**: Every surface renders the new Claude Design (tokens from Phase 2), light + dark, with no
behavioral or accessibility regression.

**Independent Test**: Walk all surfaces in light/dark; confirm brand tokens applied everywhere and all
prior actions still work; TalkBack pass.

### Tests (write first, must FAIL)

- [ ] T030 [P] [US1] Compose test: theme tokens applied on Home (cream bg, brand, 24dp cards) in `presentation/src/test/kotlin/com/daycounter/presentation/home/HomeThemeTest.kt`
- [ ] T031 [P] [US1] Compose test: dark-appearance palette applied and contrast on key screens in `presentation/src/test/kotlin/com/daycounter/presentation/theme/DarkThemeTest.kt`
- [ ] T032 [P] [US1] Compose test: milestone celebration shows "Compartir" action in `presentation/src/test/kotlin/com/daycounter/presentation/celebration/CelebrationShareTest.kt`
- [ ] T033 [P] [US1] Regression: existing acceptance tests still pass after reskin (run `002` suite)

### Implementation (reskin to theme; behavior unchanged)

- [ ] T034 [P] [US1] Reskin Home/Contadores list, summary cards, streak numeral in `presentation/src/main/kotlin/com/daycounter/presentation/home/HomeScreen.kt`
- [ ] T035 [P] [US1] Reskin Counter Detail (hero ring, hints, action buttons) in `presentation/src/main/kotlin/com/daycounter/presentation/counter/CounterDetailScreen.kt`
- [ ] T036 [P] [US1] Reskin Create/Edit/Reset sheets + move category to localized chip set (`Counter.CATEGORIES`) in `presentation/src/main/kotlin/com/daycounter/presentation/counter/` (CreateCounterSheet, EditCounterSheet, ResetConfirmSheet)
- [ ] T037 [P] [US1] Reskin Stats screen shell in `presentation/src/main/kotlin/com/daycounter/presentation/stats/StatsScreen.kt`
- [ ] T038 [P] [US1] Reskin History/Calendar + Sparkline + MonthCalendarGrid in `presentation/src/main/kotlin/com/daycounter/presentation/history/HistoryScreen.kt` and `presentation/src/main/kotlin/com/daycounter/presentation/components/`
- [ ] T039 [P] [US1] Reskin Settings shell (grouped sections) in `presentation/src/main/kotlin/com/daycounter/presentation/settings/SettingsScreen.kt`
- [ ] T040 [P] [US1] Reskin Onboarding (updated copy) + Empty state in `presentation/src/main/kotlin/com/daycounter/presentation/onboarding/OnboardingScreen.kt`
- [ ] T041 [US1] Reskin Milestone Celebration + add "Compartir" share action in `presentation/src/main/kotlin/com/daycounter/presentation/celebration/MilestoneCelebrationScreen.kt` (depends on T032)
- [X] T042 [P] [US1] Update `ProgressRing` to token colors + milestone glow (+ `paused` dashed/muted state for US2) in `presentation/src/main/kotlin/com/daycounter/presentation/components/ProgressRing.kt`
- [ ] T043 [P] [US1] Reskin Glance widget to brand tokens in `presentation/src/main/kotlin/com/daycounter/presentation/widget/DayCounterWidget.kt`
- [ ] T044 [US1] Add share strings + new copy to `presentation/src/main/res/values/strings.xml` and `values-es/strings.xml` (no hardcoded literals)
- [ ] T045 [US1] Accessibility pass: TalkBack labels + non-color state cues across reskinned screens; verify 48dp targets
- [ ] T108 [US1] Verify adaptive layout across compact/medium/expanded window size classes on reskinned + new screens; add a Compose test exercising one compact and one expanded width in `presentation/src/test/kotlin/com/daycounter/presentation/layout/AdaptiveLayoutTest.kt` (Principle I + pre-merge checklist) — addresses C2
- [ ] T110 [US1] Implement the three widget layouts per FR-006b (featured-streak banner; single-counter with a 7-bar mini-week; multi-counter list) in `presentation/src/main/kotlin/com/daycounter/presentation/widget/DayCounterWidget.kt` + `DayCounterWidgetState.kt` — addresses G1

**Checkpoint**: App fully re-skinned, light/dark, no regression. MVP demoable.

---

## Phase 4: User Story 2 - Pause and resume a counter (Priority: P2)

**Goal**: Pause freezes the streak (exclude paused days); resume continues; Home filters Todos/Activos/
Pausados; paused counters clearly marked; history/milestones untouched.

**Independent Test**: Pause → count frozen + banner; resume → continues from frozen day; filter Pausados;
no milestone while paused; history unchanged; survives restart.

### Tests (write first, must FAIL)

- [X] T046 [P] [US2] Unit test `PauseResumeUseCaseTest` (pause/resume delegate to repo with today) in `domain/src/test/kotlin/com/daycounter/domain/usecase/`
- [ ] T047 [P] [US2] Integration test: pause/resume `@Transaction` (one PausePeriod inserted, status flip, no milestone/past-streak change) in `data/src/test/kotlin/com/daycounter/data/`
- [X] T048 [P] [US2] Compose test: Detail paused state (banner, Reanudar, hint hidden) + toggle callback in `CounterDetailScreenTest.kt`
- [X] T049 [P] [US2] Filter test: `HomeViewModelFilterTest` (counts + filter narrows list by paused/active)

### Implementation

- [X] T050 [P] [US2] Implement `PauseCounterUseCase` in `domain/src/main/kotlin/com/daycounter/domain/usecase/PauseCounterUseCase.kt`
- [X] T051 [P] [US2] Implement `ResumeCounterUseCase` in `domain/src/main/kotlin/com/daycounter/domain/usecase/ResumeCounterUseCase.kt`
- [X] T052 [US2] Add `pause`/`resume` `@Transaction` to `data/src/main/kotlin/com/daycounter/data/database/dao/CounterDao.kt` (done in Phase 2 shared surface)
- [X] T053 [US2] Implement pause/resume in `data/src/main/kotlin/com/daycounter/data/repository/CounterRepositoryImpl.kt` (done in Phase 2 shared surface)
- [X] T054 [US2] Wire pause/resume + paused state into `presentation/src/main/kotlin/com/daycounter/presentation/counter/CounterDetailViewModel.kt`
- [X] T055 [US2] Detail UI: dashed/muted paused ring, "En pausa" numeral label, banner, "Reanudar/Pausar" primary in `CounterDetailScreen.kt` + `ProgressRing.kt` `paused` param
- [X] T056 [US2] Home filter chips (Todos/Activos/Pausados + live counts + empty states) in `presentation/src/main/kotlin/com/daycounter/presentation/home/HomeViewModel.kt` and `HomeScreen.kt`
- [~] T057 [P] [US2] Effective days: **Detail + Home done** (CalculateEffectiveStreakUseCase + paused freeze + paused card indicator); Glance widget paused indicator pending (with T043/T110)
- [~] T058 [US2] **Celebration suppressed while paused (Detail VM) done**; milestone notifier worker status-awareness pending
- [X] T059 [US2] Reset clears pause state (status→ACTIVE, drop pause periods) — done in `CounterDao.archiveAndReset`
- [X] T060 [US2] Pause/resume + filter strings to `values/strings.xml` + `values-es/strings.xml`

**Checkpoint**: Pause/resume fully functional and independently testable.

---

## Phase 5: User Story 3 - Change the app language in-app (Priority: P2)

**Goal**: Ajustes → Idioma switches the whole UI between English/Spanish, persisted, English default/
fallback.

**Independent Test**: Select Spanish → UI switches; restart → persists; select English → switches back.

### Tests (write first, must FAIL)

- [X] T061 [P] [US3] `LocaleManagerTest` (wrap applies en/es to Configuration) in `presentation/src/test/kotlin/com/daycounter/presentation/locale/LocaleManagerTest.kt`
- [~] T062 [P] [US3] Compose test: Idioma sheet — deferred; needs a stateless `SettingsContent` extraction (sheet currently inline with `hiltViewModel`)

### Implementation

- [X] T063 [P] [US3] Create `LocaleManager` (Configuration context wrap) in `presentation/src/main/kotlin/com/daycounter/presentation/locale/LocaleManager.kt`
- [X] T064 [US3] Read language at startup (`attachBaseContext` wrap via Hilt EntryPoint) + `recreate()` on change in `MainActivity.kt`
- [~] T065 [US3] Idioma picker implemented as an **inline `ModalBottomSheet`** in Settings (functional) rather than a Nav3 sheet entry — revisit if a routed sheet is wanted
- [X] T066 [US3] Idioma sheet UI (native names + label + check) wired to `SettingsViewModel`
- [X] T067 [US3] Add `language` state + `setLanguage` (persist → emit `languageChanged`) in `SettingsViewModel.kt`
- [X] T068 [US3] Idioma strings to `values/strings.xml` + `values-es/strings.xml`
- [~] T111 [US3] Locale formatting: `LocaleManager.wrap` sets `Locale.setDefault` + Configuration locale, so localized `java.time` formatters and number formatting follow the selection on recreate (FR-019). Dedicated formatting test still TODO.

**Checkpoint**: Language switching works and persists.

---

## Phase 6: User Story 4 - Daily reminder at a chosen time (Priority: P3)

**Goal**: Toggle a daily reminder and pick its time (presets included); it fires daily; independent of
milestone notifications.

**Independent Test**: Enable + set time → reminder fires; disable → none; persists across restart.

### Tests (write first, must FAIL)

- [X] T069 [P] [US4] `DailyReminderSchedulerTest` — next-occurrence delay (later today / rolls to tomorrow / exactly now)
- [~] T070 [P] [US4] Worker behavior — scheduler delay tested (T069); full WorkManager `TestListenableWorkerBuilder` test deferred
- [~] T071 [P] [US4] Time-picker sheet built (steppers + presets, testTags); dedicated Compose test deferred (Settings uses `hiltViewModel`)

### Implementation

- [X] T072 [US4] Add `daily_reminder` NotificationChannel in `DayCounterApplication.kt`
- [X] T073 [P] [US4] Implement `DailyReminderWorker` (post + re-enqueue, honor toggle/permission) + `DailyReminderNotifier` impl in `:app`
- [X] T074 [US4] Implement `DailyReminderScheduler` (enqueue unique REPLACE / cancel, next-occurrence delay)
- [X] T075 [US4] Re-arm reminder on app start from `SettingsRepository` in `DayCounterApplication.kt`
- [X] T109 [US4] `BOOT_COMPLETED` re-arm — extended existing `BootReceiver` (exported, permission present) to re-arm the daily reminder via a Hilt EntryPoint (I1/SC-005)
- [X] T076 [P] [US4] Time picker UI (hour/min steppers in 5-min steps + Mañana/Mediodía/Noche presets) — inline in Settings
- [X] T077 [US4] Settings "Recordatorios diarios" toggle + "Hora del recordatorio" row + inline `ReminderTimeSheet` wired to `SettingsViewModel`
- [~] T078 [P] [US4] Approaching-milestone (FR-025b, SHOULD) — deferred to a follow-up
- [X] T079 [US4] Reminder + channel strings to `values/strings.xml` + `values-es/strings.xml`

**Checkpoint**: Daily reminder schedules, fires, persists; approaching-milestone fires.

---

## Phase 7: User Story 5 - Expanded statistics (Priority: P3)

**Goal**: Estadísticas shows effective totals, best/average/milestones/active, a Pausas card, and a
weekly activity view.

**Independent Test**: With mixed active/paused counters, every metric matches data; paused excluded from
day totals & active count.

### Tests (write first, must FAIL)

- [X] T080 [P] [US5] Extended `GetStatsSummaryUseCaseTest` (effective totals, active excludes paused, avg, milestones) + `PauseStatsAndWeeklyActivityTest` (pause stats)
- [X] T081 [P] [US5] `PauseStatsAndWeeklyActivityTest` weekly cases (active every day; paused only counts before pause)
- [X] T082 [P] [US5] Compose test in `StatsScreenTest`: Pausas card + weekly + racha media + hitos exist

### Implementation

- [X] T083 [P] [US5] Implement `GetPauseStatsUseCase` in `domain/src/main/kotlin/com/daycounter/domain/usecase/GetPauseStatsUseCase.kt`
- [X] T084 [P] [US5] Implement `GetWeeklyActivityUseCase` in `domain/src/main/kotlin/com/daycounter/domain/usecase/GetWeeklyActivityUseCase.kt`
- [X] T085 [US5] Extend `GetStatsSummaryUseCase` (effective total, active-excl-paused, milestonesReached, avg) + `StatsSummary` model
- [X] T086 [P] [US5] Create `WeeklyBars` component in `presentation/src/main/kotlin/com/daycounter/presentation/components/WeeklyBars.kt`
- [X] T087 [US5] Wire Pausas card, weekly bars, racha media, hitos into `StatsViewModel.kt` and `StatsScreen.kt`
- [X] T088 [US5] Stats strings to `values/strings.xml` + `values-es/strings.xml`

**Checkpoint**: Expanded Stats accurate and consistent with Contadores (SC-008).

---

## Phase 8: User Story 6 - Erase all data with undo (Priority: P3)

**Goal**: Borrar todo erases all data after confirmation, with a bounded "Deshacer" undo.

**Independent Test**: Confirm → all cleared + toast; Deshacer restores; let it dismiss → permanent;
cancel changes nothing.

### Tests (write first, must FAIL)

- [X] T089 [P] [US6] Integration test `EraseAllRestoreTest` — snapshot-then-clear, then restore round-trip (same ids) in `data/src/test/.../repository/`
- [~] T090 [P] [US6] Compose test for confirm sheet/undo — deferred (Settings uses `hiltViewModel`; needs a stateless `SettingsContent`). Round-trip covered by T089.

### Implementation

- [X] T091 [US6] Add snapshot selects + `deleteAllCounters()` (cascade) + `restoreAll()` `@Transaction` to `CounterDao.kt`
- [X] T092 [US6] Implement erase/restore in `CounterRepositoryImpl.kt`
- [X] T093 [P] [US6] Implement `EraseAllDataUseCase` + `RestoreAllDataUseCase` + `DataSnapshot` model
- [X] T094 [US6] Confirm sheet (inline `ModalBottomSheet`) + undo snackbar (Long duration, bounded window) in `SettingsScreen.kt`/`SettingsViewModel.kt`
- [X] T095 [US6] Erase/undo strings to `values/strings.xml` + `values-es/strings.xml`

**Checkpoint**: Erase-all with working undo.

---

## Phase 9: User Story 7 - Appearance control (dark mode) (Priority: P4)

**Goal**: Ajustes → Apariencia controls dark mode (system/light/dark), persisted, brand dark palette
applied.

**Independent Test**: Toggle → palette switches; cold-restart persists.

### Tests (write first, must FAIL)

- [X] T096 [P] [US7] `AppearanceTest` (resolveDarkTheme: System follows device, Light/Dark override) in `presentation/src/test/kotlin/com/daycounter/presentation/theme/AppearanceTest.kt`

### Implementation

- [X] T097 [US7] Read `appearance` and pass `darkTheme = appearance.resolveDarkTheme(isSystemInDarkTheme())` to `DayCounterTheme` in `MainActivity.kt` (reactive recompose)
- [X] T098 [US7] Apariencia section (System/Light/Dark chips) wired to `SettingsViewModel` in `SettingsScreen.kt`
- [X] T099 [US7] Appearance strings to `values/strings.xml` + `values-es/strings.xml`

**Checkpoint**: Appearance control works and persists.

---

## Phase 10: Polish & Cross-Cutting Concerns

- [ ] T100 [P] Add KDoc to all new public `:domain`/`:data` symbols (use cases, repositories, models)
- [ ] T101 Run `:domain:koverVerify` (≥ 80 %, non-decreasing); add tests to close gaps
- [ ] T102 Run `detekt` + `:app:lintDebug`; resolve to zero errors
- [ ] T103 Build release AAB; verify ≤ 10 MB; subset fonts to Latin if over budget (per Complexity Tracking)
- [ ] T104 Commit the Room v3 exported schema JSON under `data/schemas/`
- [ ] T105 [P] Run `quickstart.md` manual verification (SC-001…SC-010), including TalkBack + dark-mode passes
- [x] T106 Resolve the dynamic-color Constitution deviation — **DONE**: Principle I amended to v2.3.0 (2026-05-29), dynamic color conditional under a defined brand design system
- [ ] T107 Regenerate the Baseline Profile (Macrobenchmark) covering the new user flows (pause/resume, settings sheets — Idioma/Hora/Borrar todo, stats) and ship it with the release AAB; review startup/frame results before release (constitution Technical Standards) — addresses C1

---

## Dependencies & Execution Order

### Phase order
- **Setup (P1)** → **Foundational (P2)** → **US1 (P3)** → US2…US7 → **Polish (P10)**.
- Foundational BLOCKS all stories. US1 (theme application) should land first as the MVP and visual base.

### Story dependencies
- **US1 (P1)**: needs Phase 2 theme. The MVP.
- **US2 (P2)**: needs Phase 2 (models, Room v3, effective-streak). Independent of US3–US7.
- **US3 (P2)**: needs Phase 2 (SettingsRepository). Independent of US2.
- **US4 (P3)**: needs Phase 2 (SettingsRepository) + channel. Independent.
- **US5 (P3)**: needs Phase 2 (effective-streak, pause models). Stronger demo after US2 but testable alone with seeded data.
- **US6 (P3)**: needs Phase 2 (Room) + `CounterRepository` erase/restore. Independent.
- **US7 (P4)**: needs Phase 2 (theme + SettingsRepository). Independent.

### Within a story
- Tests first (must fail) → models → DAO/repository → use case/ViewModel → UI → strings.

### Parallel opportunities
- Setup: T001, T002, T004, T005 in parallel.
- Foundational: theme (T006–T008, T011) ∥ domain models (T012–T014) ∥ test stubs (T016, T023, T027).
- US1 reskin tasks T034–T040, T042, T043 are largely parallel (different files).
- After Phase 2, US2/US3/US4/US6/US7 can proceed in parallel by different developers.

---

## Parallel Example: User Story 2

```text
# Tests first (parallel):
T046 PauseCounterUseCaseTest / ResumeCounterUseCaseTest
T048 PauseDetailTest
T049 FilterChipsTest

# Then domain use cases in parallel:
T050 PauseCounterUseCase
T051 ResumeCounterUseCase
```

---

## Implementation Strategy

### MVP first
1. Phase 1 Setup → 2. Phase 2 Foundational → 3. Phase 3 US1 (full reskin) → **validate** light/dark +
   no regression → demo. This is the design migration the user asked for first.

### Incremental delivery
US1 (theme) → US2 (pause) → US3 (language) → US4 (reminder) → US5 (stats) → US6 (erase) → US7
(appearance). Each is an independently testable increment.

### Notes
- TDD is mandatory (Principle II): write failing tests before implementation.
- No hardcoded strings/colors; all from theme + `strings.xml`/`values-es`.
- `java.time` with explicit `ZoneId` for all date math.
- Commit per task or logical group, referencing the task ID (constitution workflow).
- **Before merge**: T106 dynamic-color amendment is done (v2.3.0); ensure T107 (Baseline Profile),
  T108 (adaptive layout), and T103 (AAB ≤ 10 MB) are complete.
