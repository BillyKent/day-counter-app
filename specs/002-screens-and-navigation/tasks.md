---
description: "Task list for Screens and Navigation Overhaul (Navigation 3 migration + new surfaces)"
---

# Tasks: Screens and Navigation Overhaul

**Input**: Design documents from `/specs/002-screens-and-navigation/`

**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅ (navigation-contract, ui-contract), quickstart.md ✅

**Tests**: Included — constitution Principle II (NON-NEGOTIABLE) mandates TDD with three test layers (unit, integration, UI) and ≥80% `:domain` line coverage (Kover gate). UI tests MUST cover every acceptance scenario and MUST fail before the screen is built.

**Organization**: Tasks are grouped by user story (US1–US8) to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (US1–US8)
- Each task includes exact file paths

## Path Conventions

Four existing Gradle modules per plan.md (reused, no new module):
- **:domain** → `domain/src/main/kotlin/com/daycounter/domain/` (+ `domain/src/test/...`)
- **:data** → `data/src/main/kotlin/com/daycounter/data/` (+ `data/src/test/...`)
- **:presentation** → `presentation/src/main/kotlin/com/daycounter/presentation/` (+ `presentation/src/test/...`), resources under `presentation/src/main/res/`
- **:app** → `app/src/main/`

---

## Phase 1: Setup (Toolchain bump + Navigation 3 deps + localization scaffolding)

**Purpose**: Raise the toolchain to what Navigation 3 requires and wire the new dependencies. **AGP 9 is out of scope** — only the minimal AGP/compileSdk bump is performed here (see plan Complexity Tracking).

- [X] T001 Bump AGP `8.7.3 → 8.9+` (the minimal version that targets compileSdk 36) in `gradle/libs.versions.toml`; update the Gradle wrapper in `gradle/wrapper/gradle-wrapper.properties` to the version that AGP requires; verify Detekt `1.23.7` and Kover `0.8.3` still resolve against the new AGP/Gradle, recording the confirmed exact versions in the catalog
- [X] T002 Raise `compileSdk` and `targetSdk` `35 → 36` (keep `minSdk = 26`) in `app/build.gradle.kts`
- [X] T003 [P] Add Navigation 3 versions/libraries to `gradle/libs.versions.toml`: `nav3Core = "1.0.0"` (`androidx.navigation3:navigation3-runtime`, `androidx.navigation3:navigation3-ui`) and `lifecycleViewmodelNav3 = "2.10.0-rc01"` (`androidx.lifecycle:lifecycle-viewmodel-navigation3`); remove the `composeNavigation` / `compose-navigation` entries
- [X] T004 [P] Update `presentation/build.gradle.kts` and `app/build.gradle.kts`: add the nav3 runtime/ui + `lifecycle-viewmodel-navigation3` deps, ensure the `org.jetbrains.kotlin.plugin.serialization` plugin is applied where `NavKeys.kt` lives, ensure the Hilt-Compose integration version exposing `hiltViewModel(creationCallback=…)` is present, and remove the `compose-navigation` dependency
- [X] T005 [P] Create the base English string resources `presentation/src/main/res/values/strings.xml` and the Spanish variant `presentation/src/main/res/values-es/strings.xml`; seed the six milestone-celebration messages from ui-contract.md (1/7/30/100/365/1000) in `values-es` with English equivalents in `values`, plus shared UI labels (tab titles Contadores/Estadísticas/Ajustes). Confirm the app default locale is English (no `android:locale` override)

**Checkpoint**: Project builds (`./gradlew help`) against compileSdk 36 with nav3 on the classpath and `navigation-compose` removed.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Data-model migration, Navigation 3 non-UI primitives, and reusable visual components that ALL user stories depend on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

### Data model & persistence

- [X] T006 Extend `Counter` domain model with `category: String?` and `goalMilestoneTarget: Int` (default 30) + KDoc, and add `val GOAL_TARGETS = setOf(7, 30, 100, 365)` companion, in `domain/src/main/kotlin/com/daycounter/domain/model/Counter.kt`
- [X] T007 [P] Update `MilestoneRecord`: change `MILESTONE_DAYS` to `setOf(1, 7, 30, 100, 365, 1000)` and add `celebrationShown: Boolean = false` + KDoc in `domain/src/main/kotlin/com/daycounter/domain/model/MilestoneRecord.kt`
- [X] T008 [P] Create `PastStreakRecord` domain model (`id`, `counterId`, `streakDays`, `reason`, `endDate: LocalDate`, `createdAt: Instant`) + KDoc in `domain/src/main/kotlin/com/daycounter/domain/model/PastStreakRecord.kt`
- [X] T009 Create `PastStreakRepository` interface (`insert`, `getForCounterPaged(counterId, limit, offset)`) + KDoc in `domain/src/main/kotlin/com/daycounter/domain/repository/PastStreakRepository.kt`
- [X] T010 Add `getForCounter(counterId): List<MilestoneRecord>` and `markAllShownForCounter(counterId)` to `domain/src/main/kotlin/com/daycounter/domain/repository/MilestoneRepository.kt`
- [X] T011 Update `CounterEntity` with `category` (TEXT nullable) and `goal_milestone_target` (INTEGER NOT NULL DEFAULT 30) columns + domain↔entity mappers in `data/src/main/kotlin/com/daycounter/data/database/entity/CounterEntity.kt`
- [X] T012 [P] Update `MilestoneRecordEntity` with `celebration_shown` (INTEGER NOT NULL DEFAULT 0) column + mappers in `data/src/main/kotlin/com/daycounter/data/database/entity/MilestoneRecordEntity.kt`
- [X] T013 [P] Create `PastStreakRecordEntity` (FK `counter_id` → counters ON DELETE CASCADE, index `(counter_id, end_date)`) + mappers in `data/src/main/kotlin/com/daycounter/data/database/entity/PastStreakRecordEntity.kt`
- [X] T014 Create `PastStreakRecordDao` (`insert`, `pagedByCounter(counterId, limit, offset)` ordered `end_date DESC, id DESC`) in `data/src/main/kotlin/com/daycounter/data/database/dao/PastStreakRecordDao.kt`
- [X] T015 [P] Add `selectForCounter(counterId)` and `markAllShownForCounter(counterId)` to `data/src/main/kotlin/com/daycounter/data/database/dao/MilestoneRecordDao.kt`
- [X] T016 Add the atomic reset `@Transaction archiveAndReset(counterId, streakDaysAtReset, today, now)` (insert PastStreakRecord only when `streakDaysAtReset > 0`, delete all milestone rows for the counter, set `start_date = today`) in `data/src/main/kotlin/com/daycounter/data/database/dao/CounterDao.kt`
- [X] T017 Bump `AppDatabase` version, register `PastStreakRecordEntity`, and add `fallbackToDestructiveMigration(true)` in `data/src/main/kotlin/com/daycounter/data/database/AppDatabase.kt`
- [X] T018 Provide `PastStreakRecordDao` and bind `PastStreakRepositoryImpl` in `data/src/main/kotlin/com/daycounter/data/di/DataModule.kt`
- [X] T019 Implement `PastStreakRepositoryImpl` and add the new `MilestoneRepositoryImpl` methods in `data/src/main/kotlin/com/daycounter/data/repository/` (`PastStreakRepositoryImpl.kt`, `MilestoneRepositoryImpl.kt`)

### Navigation 3 primitives (non-UI)

- [X] T020 Create all `@Serializable … : NavKey` route keys (Onboarding, Contadores, Estadisticas, Ajustes, Detail(counterId), History(counterId), Celebration(counterId, milestone), CreateCounter, EditCounter(counterId), ResetConfirm(counterId)) in `presentation/src/main/kotlin/com/daycounter/presentation/navigation/NavKeys.kt`
- [X] T021 [P] Vendor the `BottomSheetSceneStrategy` (and `BottomSheetScene`) from the official Nav3 recipe, with source attribution, in `presentation/src/main/kotlin/com/daycounter/presentation/navigation/BottomSheetSceneStrategy.kt`
- [X] T022 [P] Create `TopLevelBackStack` (one saveable back stack per tab via `rememberNavBackStack`, `topLevelKey`, `addTopLevel`/`add`/`removeLast`, flattened `backStack`) in `presentation/src/main/kotlin/com/daycounter/presentation/navigation/TopLevelBackStack.kt`
- [X] T023 [P] Create `DeepLinkResolver` (parse `daycounter://counter/{id}` → validate `counterId: Long` → synthetic `[Contadores, Detail(id)]`; missing/invalid/non-existent → `[Contadores]`) in `presentation/src/main/kotlin/com/daycounter/presentation/navigation/DeepLinkResolver.kt`
- [X] T024 Delete the Navigation 2 artifacts `presentation/src/main/kotlin/com/daycounter/presentation/navigation/Screen.kt` and `AppNavGraph.kt`, and remove all `androidx.navigation.*` (`navigation-compose`) imports across `:presentation`

### Reusable components

- [X] T025 [P] Create `ProgressRing` composable (`Canvas`, fill = `min(1f, days/target)`, content-described, no color-only state) in `presentation/src/main/kotlin/com/daycounter/presentation/components/ProgressRing.kt`
- [X] T026 [P] Create `Sparkline` composable (`Canvas` polyline over per-day streak values; 1 point for same-day) in `presentation/src/main/kotlin/com/daycounter/presentation/components/Sparkline.kt`
- [X] T027 [P] Create `MonthCalendarGrid` composable (`LazyVerticalGrid(Fixed(7))`, current month only, cell states InStreak/Today/PreStreak/Future) in `presentation/src/main/kotlin/com/daycounter/presentation/components/MonthCalendarGrid.kt`

### Foundational tests (write first — must fail before T016–T019 are implemented)

- [X] T028 [P] Integration test for the reset `@Transaction` (archives when streak > 0, no record when streak == 0, deletes all milestone rows, sets `start_date = today`, all-or-nothing) using Room in-memory in `data/src/test/kotlin/com/daycounter/data/database/ResetTransactionTest.kt`
- [X] T029 [P] Integration test for `PastStreakRecordDao` pagination (`end_date DESC, id DESC`, limit/offset batches) and FK cascade-delete on counter deletion in `data/src/test/kotlin/com/daycounter/data/database/PastStreakRecordDaoTest.kt`
- [X] T030 [P] Integration test confirming `AppDatabase` opens with the new schema and `MilestoneRecordDao.markAllShownForCounter` / `selectForCounter` behave as specified in `data/src/test/kotlin/com/daycounter/data/database/MilestoneRecordDaoTest.kt`

**Checkpoint**: Foundation ready — migration, persistence, nav primitives, and components compile and their integration tests pass. User-story UI can now be built (each depends on the US1 shell — see Dependencies).

---

## Phase 3: User Story 1 - Three-tab navigation shell (Priority: P1) 🎯 MVP

**Goal**: A persistent three-tab bottom navigation (Contadores · Estadísticas · Ajustes); deep screens (Detail/History/Celebration) hide the bar; Ajustes hosts the existing notification toggle.

**Independent Test**: Launch with ≥1 counter → three tabs visible; switching tabs shows the right content and moves the selected indicator; tapping a counter card hides the bar; back restores it; Detail→History→back returns to Detail; Ajustes shows the working milestone-notification toggle.

### Tests for User Story 1 ⚠️ (write first, must fail)

- [X] T031 [P] [US1] Compose UI test covering US1 acceptance scenarios 1–7 (tabs visible, tab switching + indicator, card tap hides bar, back restores bar, History→back→Detail, Ajustes toggle present) in `presentation/src/test/kotlin/com/daycounter/presentation/navigation/NavShellTest.kt`

### Implementation for User Story 1

- [X] T032 [US1] Create `AppNavDisplay` — `NavDisplay(backStack = topLevelBackStack.backStack, onBack = { topLevelBackStack.removeLast() }, sceneStrategies = listOf(BottomSheetSceneStrategy()), entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()), entryProvider { … })` mapping every NavKey to its screen in `presentation/src/main/kotlin/com/daycounter/presentation/navigation/AppNavDisplay.kt`
- [X] T033 [US1] Create `MainScaffold` — `Scaffold` with a `NavigationBar` (3 tabs) rendered only when `backStack.last()` is a tab key (FR-001/FR-002), `NavigationBarItem.selected = key == topLevelBackStack.topLevelKey`, hosting `AppNavDisplay`, in `presentation/src/main/kotlin/com/daycounter/presentation/navigation/MainScaffold.kt`
- [X] T034 [US1] Update `MainActivity` to build `TopLevelBackStack` (start `Contadores`, or `Onboarding` when the onboarding-shown flag is false), apply `DeepLinkResolver` to the launch `Intent`, render `MainScaffold`, and keep edge-to-edge, in `presentation/src/main/kotlin/com/daycounter/presentation/MainActivity.kt`
- [X] T035 [US1] Wire the Onboarding entry + start-destination handoff (on finish/skip replace stack with `[Contadores]`) and register `SettingsScreen` as the Ajustes tab entry, in `presentation/src/main/kotlin/com/daycounter/presentation/navigation/AppNavDisplay.kt`
- [X] T036 [US1] Migrate the existing `HomeScreen` and `SettingsScreen` to render as tab entries (minimal wiring; enriched Home is US2) and confirm the existing notification toggle still functions in `presentation/src/main/kotlin/com/daycounter/presentation/home/HomeScreen.kt` and `presentation/src/main/kotlin/com/daycounter/presentation/settings/SettingsScreen.kt`

**Checkpoint**: The shell is navigable and independently testable; US2–US8 register their screens into `AppNavDisplay`.

---

## Phase 4: User Story 2 - Enriched counter list (Priority: P1)

**Goal**: Contadores tab shows a global summary (Total, Mejor racha) plus per-counter cards with a progress ring, streak, name, start date, and "Hito alcanzado" badge; "+" opens the Create sheet; tapping a card opens Detail.

**Independent Test**: Seed counters with streaks 3 / 15 / 102 (one past its goal target) → summary correct, rings proportional, the over-target card shows the badge, "+" opens the Create sheet, card tap opens Detail.

### Tests for User Story 2 ⚠️ (write first, must fail)

- [ ] T037 [P] [US2] Compose UI test covering US2 scenarios 1–6 (Empty state + CTA at 0; summary Total/Mejor racha; card ring/streak/name/date; badge when streak ≥ target; "+" → Create sheet; card tap → Detail) in `presentation/src/test/kotlin/com/daycounter/presentation/home/HomeScreenTest.kt`
- [ ] T038 [P] [US2] Unit test for `GetStatsSummaryUseCase` (total = Σ streak, best = max streak, active = count) in `domain/src/test/kotlin/com/daycounter/domain/usecase/GetStatsSummaryUseCaseTest.kt`

### Implementation for User Story 2

- [ ] T039 [P] [US2] Create `GetStatsSummaryUseCase` (total accumulated, best streak, active counters; reused by US5) in `domain/src/main/kotlin/com/daycounter/domain/usecase/GetStatsSummaryUseCase.kt`
- [ ] T040 [US2] Update `HomeViewModel`: expose summary state + `CounterCardUi` (ringFillRatio, goalReached), `createSheetVisible`, and lifecycle-resume streak recompute, in `presentation/src/main/kotlin/com/daycounter/presentation/home/HomeViewModel.kt`
- [ ] T041 [US2] Update `HomeScreen`: summary cards, per-card `ProgressRing` + badge + name + start date, "+" FAB → `CreateCounter`, Empty-state CTA, card tap → `Detail(id)` (remove any Home→Edit nav, FR-009), in `presentation/src/main/kotlin/com/daycounter/presentation/home/HomeScreen.kt`
- [ ] T042 [US2] Add Home/summary/badge string resources to `values/strings.xml` and `values-es/strings.xml`

**Checkpoint**: Home renders the enriched list independently (counters seedable via DB for the test).

---

## Phase 5: User Story 3 - Counter detail with full action set (Priority: P1)

**Goal**: Detail shows a hero ring, next-milestone hint, achieved-milestones chips (informational), and actions Editar/Reiniciar/Eliminar/Abrir historial/Revivir celebración.

**Independent Test**: Open a 35-day counter → hero "35", "5 días para tu próximo hito", chips `1, 7, 30` (non-interactive), and each action performs its navigation/operation.

### Tests for User Story 3 ⚠️ (write first, must fail)

- [ ] T043 [P] [US3] Compose UI test covering US3 scenarios 1–9 (hero ring denominator, next-milestone hint, ≥1000 replacement, achieved chips non-interactive, Editar/Reiniciar/Eliminar/Historial/Revivir) in `presentation/src/test/kotlin/com/daycounter/presentation/counter/CounterDetailScreenTest.kt`
- [ ] T044 [P] [US3] Unit tests for `GetNextMilestoneUseCase`, `GetAchievedMilestonesUseCase`, `GetMostRecentMilestoneUseCase` in `domain/src/test/kotlin/com/daycounter/domain/usecase/MilestoneQueriesTest.kt`

### Implementation for User Story 3

- [ ] T045 [P] [US3] Create `GetNextMilestoneUseCase` (smallest milestone strictly > streak; null when streak ≥ 1000) in `domain/src/main/kotlin/com/daycounter/domain/usecase/GetNextMilestoneUseCase.kt`
- [ ] T046 [P] [US3] Create `GetAchievedMilestonesUseCase` (milestones ≤ current streak) in `domain/src/main/kotlin/com/daycounter/domain/usecase/GetAchievedMilestonesUseCase.kt`
- [ ] T047 [P] [US3] Create `GetMostRecentMilestoneUseCase` (highest milestone ≤ current streak) in `domain/src/main/kotlin/com/daycounter/domain/usecase/GetMostRecentMilestoneUseCase.kt`
- [ ] T048 [US3] Create `CounterDetailViewModel` (state: streak/ring/hint/achieved/canRevive/sheet flags; `UiEvent` for delete + history nav; resume recompute; delete returns to Contadores) with Hilt `@AssistedInject(counterId)` + `@AssistedFactory` in `presentation/src/main/kotlin/com/daycounter/presentation/counter/CounterDetailViewModel.kt`
- [ ] T049 [US3] Rewrite `CounterDetailScreen`: hero `ProgressRing` (goal denominator), next-milestone hint, achieved chips (non-interactive), actions → Editar (`EditCounter`), Reiniciar (`ResetConfirm`), Eliminar (confirm → delete → Contadores), Abrir historial (`History`), Revivir (`Celebration`), in `presentation/src/main/kotlin/com/daycounter/presentation/counter/CounterDetailScreen.kt`
- [ ] T050 [US3] Register the `Detail` entry in `AppNavDisplay` with `hiltViewModel(creationCallback = { it.create(key) })` in `presentation/src/main/kotlin/com/daycounter/presentation/navigation/AppNavDisplay.kt`
- [ ] T051 [US3] Add Detail string resources (next-milestone hint, "Has alcanzado todos los hitos", action labels) to `values/` and `values-es/`

**Checkpoint**: Detail is fully navigable; Eliminar works end-to-end. Reiniciar/Revivir open the sheets/overlay delivered in US7/US4.

---

## Phase 6: User Story 4 - Milestone celebration overlay (Priority: P2)

**Goal**: Full-screen celebration that auto-launches the first time a milestone is reached, shows the milestone copy + counter name, and is re-openable via "Revivir celebración".

**Independent Test**: Counter dated exactly 7 days ago → opening Detail auto-launches the 7-day copy; "Seguir así" closes; reopening Detail does not re-launch; "Revivir" opens it on demand.

### Tests for User Story 4 ⚠️ (write first, must fail)

- [ ] T052 [P] [US4] Compose UI test covering US4 scenarios 1–5 (auto-launch once, copy + name, close returns to Detail, no second auto-launch, Revivir opens most-recent) in `presentation/src/test/kotlin/com/daycounter/presentation/celebration/MilestoneCelebrationTest.kt`
- [ ] T053 [P] [US4] Unit test for `MarkCelebrationsShownUseCase` and for `CheckMilestonesUseCase` with the new set + `celebrationShown=false` on insert in `domain/src/test/kotlin/com/daycounter/domain/usecase/CelebrationUseCasesTest.kt`

### Implementation for User Story 4

- [ ] T054 [P] [US4] Create `MarkCelebrationsShownUseCase` (set `celebration_shown = true` for all of a counter's rows) in `domain/src/main/kotlin/com/daycounter/domain/usecase/MarkCelebrationsShownUseCase.kt`
- [ ] T055 [US4] Update `CheckMilestonesUseCase` to use `MILESTONE_DAYS = {1,7,30,100,365,1000}` and insert new records with `celebrationShown = false` in `domain/src/main/kotlin/com/daycounter/domain/usecase/CheckMilestonesUseCase.kt`
- [ ] T056 [US4] Create `MilestoneCelebrationViewModel` (resolve milestone + copy from string resources; mark shown) with `@AssistedInject(Celebration key)` in `presentation/src/main/kotlin/com/daycounter/presentation/celebration/MilestoneCelebrationViewModel.kt`
- [ ] T057 [US4] Create `MilestoneCelebrationScreen` full-screen overlay (animated `ProgressRing`, milestone number, copy, counter name, "Seguir así" / close X → `removeLast()`) and register its entry in `AppNavDisplay`, in `presentation/src/main/kotlin/com/daycounter/presentation/celebration/MilestoneCelebrationScreen.kt`
- [ ] T058 [US4] Wire Detail auto-launch in `CounterDetailViewModel` (on resume, if the most-recent milestone has an unseen record → emit `AutoLaunchCelebration(N)`; on launch, call `MarkCelebrationsShownUseCase`) and the Revivir action, in `presentation/src/main/kotlin/com/daycounter/presentation/counter/CounterDetailViewModel.kt`

**Checkpoint**: Celebration auto-launches once and is re-openable; US3's Revivir/auto-launch now complete end-to-end.

---

## Phase 7: User Story 5 - Estadísticas tab (Priority: P2)

**Goal**: Estadísticas tab shows Total acumulado (hero) + Mejor racha + Contadores activos, with a neutral empty state at 0 counters.

**Independent Test**: Counters 5/30/120 → Total 155, Mejor 120, Activos 3; with 0 counters, the empty state invites creation.

### Tests for User Story 5 ⚠️ (write first, must fail)

- [ ] T059 [P] [US5] Compose UI test covering US5 scenarios 1–3 (hero total, two secondary metrics, empty state at 0) in `presentation/src/test/kotlin/com/daycounter/presentation/stats/StatsScreenTest.kt`

### Implementation for User Story 5

- [ ] T060 [US5] Create `StatsViewModel` consuming `GetStatsSummaryUseCase` (reused from US2) with resume recompute in `presentation/src/main/kotlin/com/daycounter/presentation/stats/StatsViewModel.kt`
- [ ] T061 [US5] Create `StatsScreen` (hero Total acumulado + Mejor racha + Contadores activos cards + empty state) and register the Estadisticas entry in `AppNavDisplay`, in `presentation/src/main/kotlin/com/daycounter/presentation/stats/StatsScreen.kt`
- [ ] T062 [US5] Add Stats string resources to `values/` and `values-es/`

**Checkpoint**: Estadísticas tab works independently.

---

## Phase 8: User Story 6 - Create/Edit bottom sheets + category + goal target (Priority: P2)

**Goal**: Create and Edit are bottom-sheet entries collecting Nombre, Categoría, Fecha de inicio, and Hito objetivo `{7,30,100,365}`; Edit's start date is read-only with a Reiniciar hint.

**Independent Test**: "+" slides up the Create sheet; save "Dejar de fumar" / Salud / today / goal 30 → a card with a ring sized to 30; Edit → prefilled, date read-only with the hint.

### Tests for User Story 6 ⚠️ (write first, must fail)

- [ ] T063 [P] [US6] Compose UI test covering US6 scenarios 1–7 (sheet fields, goal choice set, save persists + closes, first-create → list renders, backdrop/Cancelar no-op, Edit prefilled + date read-only hint, save → Detail) in `presentation/src/test/kotlin/com/daycounter/presentation/counter/CreateEditSheetTest.kt`
- [ ] T064 [P] [US6] Unit tests for `CreateCounterUseCase`/`UpdateCounterUseCase` validating `category` (0–50) and `goalMilestoneTarget ∈ {7,30,100,365}` in `domain/src/test/kotlin/com/daycounter/domain/usecase/CreateUpdateCounterValidationTest.kt`

### Implementation for User Story 6

- [ ] T065 [US6] Update `CreateCounterUseCase` and `UpdateCounterUseCase` for `category` + `goalMilestoneTarget` validation in `domain/src/main/kotlin/com/daycounter/domain/usecase/CreateCounterUseCase.kt` and `UpdateCounterUseCase.kt`
- [ ] T066 [US6] Update `CreateCounterViewModel` and `EditCounterViewModel` with the new fields, validation, and `saveEnabled` in `presentation/src/main/kotlin/com/daycounter/presentation/counter/CreateCounterViewModel.kt` and `EditCounterViewModel.kt`
- [ ] T067 [US6] Create `CreateCounterSheet` (form: Nombre/Categoría/Fecha de inicio/Hito objetivo) in `presentation/src/main/kotlin/com/daycounter/presentation/counter/CreateCounterSheet.kt`
- [ ] T068 [US6] Create `EditCounterSheet` (reuses the form; Fecha de inicio read-only + "Para empezar de cero, usa Reiniciar" hint) in `presentation/src/main/kotlin/com/daycounter/presentation/counter/EditCounterSheet.kt`
- [ ] T069 [US6] Register `CreateCounter`/`EditCounter` entries with `BottomSheetSceneStrategy.bottomSheet()` metadata in `AppNavDisplay`, and delete the obsolete `presentation/.../counter/CreateCounterScreen.kt` and `EditCounterScreen.kt`
- [ ] T070 [US6] Add Create/Edit string resources (field labels, goal-target chips, read-only hint) to `values/` and `values-es/`

**Checkpoint**: Create/Edit sheets work; US2's "+" and US3's Editar now complete end-to-end.

---

## Phase 9: User Story 7 - Reset confirmation + past-streak persistence (Priority: P2)

**Goal**: Reiniciar opens a confirmation sheet warning the streak will be archived; on confirm, the current streak is archived as a `PastStreakRecord`, milestone rows are cleared, and start date becomes today.

**Independent Test**: Counter dated 15 days ago → Reiniciar → Confirmar → streak 0, counter persists, a 15-day past-streak record exists and appears in History.

### Tests for User Story 7 ⚠️ (write first, must fail)

- [ ] T071 [P] [US7] Compose UI test covering US7 scenarios 1–4 (confirm sheet with archive warning, Cancelar no-op, Confirmar archives + resets → Detail, History shows "15 días · Reiniciado · date") in `presentation/src/test/kotlin/com/daycounter/presentation/counter/ResetConfirmTest.kt`
- [ ] T072 [P] [US7] Unit test for `ArchiveAndResetCounterUseCase` (archive when streak > 0, skip when 0, clears milestones, start_date = today) in `domain/src/test/kotlin/com/daycounter/domain/usecase/ArchiveAndResetCounterUseCaseTest.kt`

### Implementation for User Story 7

- [ ] T073 [US7] Create `ArchiveAndResetCounterUseCase` (compute streak via `CalculateStreakUseCase`, invoke the reset `@Transaction`), replacing the old `ResetCounterUseCase`, in `domain/src/main/kotlin/com/daycounter/domain/usecase/ArchiveAndResetCounterUseCase.kt`
- [ ] T074 [US7] Create `ResetConfirmSheet` (archive warning + Confirmar/Cancelar) in `presentation/src/main/kotlin/com/daycounter/presentation/counter/ResetConfirmSheet.kt`
- [ ] T075 [US7] Register the `ResetConfirm` bottom-sheet entry in `AppNavDisplay` and wire `CounterDetailViewModel.confirmReset()` to `ArchiveAndResetCounterUseCase` in `presentation/src/main/kotlin/com/daycounter/presentation/counter/CounterDetailViewModel.kt`
- [ ] T076 [US7] Add Reset string resources (warning copy) to `values/` and `values-es/`

**Checkpoint**: Reset archives + clears milestones atomically; US3's Reiniciar completes; History (US8) now has content.

---

## Phase 10: User Story 8 - History / Calendar per counter (Priority: P3)

**Goal**: History (reachable only from Detail) shows a header (name + current streak + sparkline), a current-month calendar grid with cell states, and a paginated "Rachas anteriores" list.

**Independent Test**: 12-day counter reset twice (3 & 14 days) → calendar highlights streak days, today distinct, sparkline 12 points, both prior runs listed with reasons/end dates and "Ver más" paginates.

### Tests for User Story 8 ⚠️ (write first, must fail)

- [ ] T077 [P] [US8] Compose UI test covering US8 scenarios 1–4 (back → Detail, header + sparkline, current-month cell states, "Rachas anteriores" pagination in batches of 50) in `presentation/src/test/kotlin/com/daycounter/presentation/history/HistoryScreenTest.kt`
- [ ] T078 [P] [US8] Unit tests for `GetPastStreaksUseCase` pagination and for the calendar-cell-state + sparkline-point pure functions in `domain/src/test/kotlin/com/daycounter/domain/usecase/HistoryComputationsTest.kt`

### Implementation for User Story 8

- [ ] T079 [P] [US8] Create `GetPastStreaksUseCase` (paged, newest `end_date` first, batch size 50) in `domain/src/main/kotlin/com/daycounter/domain/usecase/GetPastStreaksUseCase.kt`
- [ ] T080 [US8] Create `HistoryViewModel` (current-month calendar model, sparkline points, paged past streaks with `canLoadMore`/`onLoadMore`, resume recompute) with `@AssistedInject(History key)` in `presentation/src/main/kotlin/com/daycounter/presentation/history/HistoryViewModel.kt`
- [ ] T081 [US8] Create `HistoryScreen` (header + `Sparkline`, `MonthCalendarGrid`, "Rachas anteriores" list + "Ver más") in `presentation/src/main/kotlin/com/daycounter/presentation/history/HistoryScreen.kt`
- [ ] T082 [US8] Register the `History` entry in `AppNavDisplay` with `hiltViewModel(creationCallback)` and confirm back returns to Detail in `presentation/src/main/kotlin/com/daycounter/presentation/navigation/AppNavDisplay.kt`
- [ ] T083 [US8] Add History string resources (section titles, "Ver más", reason label) to `values/` and `values-es/`

**Checkpoint**: All user stories independently functional.

---

## Phase 11: Polish & Cross-Cutting Concerns

**Purpose**: Quality gates and cross-cutting concerns spanning all stories.

- [ ] T084 [P] Add TalkBack content descriptions to `ProgressRing`, the "Hito alcanzado" badge, `MonthCalendarGrid` cells, and `Sparkline`; verify no state is conveyed by color alone (Principle I) across the new components
- [ ] T085 [P] Dark-mode + dynamic-color visual pass on Home, Detail, Stats, History, Celebration, and the three sheets (Principle I)
- [ ] T086 [P] Verify edge-to-edge window-inset handling (IME + navigation bar) for the bottom sheets and full-screen overlays (Principle I)
- [ ] T087 Run Android Lint (zero errors) and Detekt (no new findings without justified suppression) including the new nav3/serialization code (Principle V)
- [ ] T088 Run `./gradlew :domain:koverVerify` and confirm ≥80% line coverage including the new use cases (Principle II)
- [ ] T089 Build the release variant and confirm R8 minification + resource shrinking succeed; add any required keep rules for `@Serializable` NavKeys and nav3 in `app/proguard-rules.pro` (Principle V / Technical Standards)
- [ ] T090 [P] Regenerate the Baseline Profile for the new navigation flows (Macrobenchmark) per Technical Standards
- [ ] T091 Remove dead code: confirm `ResetCounterUseCase`, `Screen.kt`, `AppNavGraph.kt`, `CreateCounterScreen.kt`, `EditCounterScreen.kt` are deleted and that no `navigation-compose` references remain (Principle V)
- [ ] T092 Run the quickstart.md manual verification for US1–US8 on one compact and one expanded window size class (Principle I / SC-001…SC-007)

---

## Dependencies & Execution Order

### Phase dependencies

- **Setup (Phase 1)**: no dependencies — start immediately.
- **Foundational (Phase 2)**: depends on Setup — BLOCKS all user stories.
- **US1 (Phase 3)**: depends on Foundational. It builds the nav shell (`AppNavDisplay`/`MainScaffold`) and is the **structural prerequisite** for US2–US8 (their screens register into `AppNavDisplay`).
- **US2–US8 (Phases 4–10)**: depend on Foundational **and** US1. After US1 they can largely proceed in parallel, with the cross-story references below.
- **Polish (Phase 11)**: depends on all targeted stories.

### Cross-story references (by design — each story is still independently testable for its own surface)

- **US2 → US6**: the Home "+" opens the Create sheet (US6). US2's own test asserts navigation to the `CreateCounter` key; counters are seeded via DB for list/summary assertions.
- **US3 → US4 / US6 / US7**: Detail's Revivir/auto-launch use the Celebration overlay (US4); Editar opens the Edit sheet (US6); Reiniciar opens the Reset sheet (US7). US3 tests assert navigation to those keys; Eliminar is fully delivered in US3.
- **US5 → US2**: reuses `GetStatsSummaryUseCase` (created in US2 / T039).
- **US8 → US7**: "Rachas anteriores" is populated by past-streak records produced by US7's reset.

### Within each user story

- Tests (UI + unit) are written FIRST and must FAIL before implementation.
- Domain use cases (models already in Foundational) → ViewModels → Composables/screens → entry registration in `AppNavDisplay`.
- Strings added alongside the screen that uses them.

### Parallel opportunities

- Setup: T003, T004, T005 in parallel after T001/T002.
- Foundational: T007/T008 (models), T012/T013 (entities), T015 (dao), T021/T022/T023 (nav primitives), T025/T026/T027 (components), and the test tasks T028/T029/T030 are all `[P]` (different files).
- Per story: the UI test + unit test + independent domain use cases marked `[P]` can run together; e.g. US3 T043/T044/T045/T046/T047.

---

## Parallel Example: User Story 3

```bash
# Write the failing tests first (parallel):
Task: "US3 Compose UI test in presentation/.../counter/CounterDetailScreenTest.kt"
Task: "US3 unit tests in domain/.../usecase/MilestoneQueriesTest.kt"

# Then the independent domain use cases (parallel):
Task: "GetNextMilestoneUseCase in domain/.../usecase/GetNextMilestoneUseCase.kt"
Task: "GetAchievedMilestonesUseCase in domain/.../usecase/GetAchievedMilestonesUseCase.kt"
Task: "GetMostRecentMilestoneUseCase in domain/.../usecase/GetMostRecentMilestoneUseCase.kt"
```

---

## Implementation Strategy

### MVP first (P1 stories)

1. Complete Phase 1 (Setup) + Phase 2 (Foundational).
2. Complete Phase 3 (US1 shell) → Phase 4 (US2 list) → Phase 5 (US3 detail).
3. **STOP and VALIDATE**: the three-tab app with an enriched list and a navigable detail (Eliminar working) is a coherent MVP. Note Reiniciar/Editar/Revivir open surfaces delivered in the P2 phases.

### Incremental delivery (P2 → P3)

4. US4 (celebration) + US6 (sheets) + US7 (reset) complete the Detail action set end-to-end; US5 (stats) adds the third tab.
5. US8 (history) lands last (P3), consuming US7's past streaks.
6. Finish with Phase 11 polish + quality gates.

---

## Notes

- `[P]` = different files, no dependency on incomplete tasks.
- `[Story]` label maps each task to its user story for traceability.
- The destructive Room migration (Foundational) is intentional and spec-sanctioned (fresh-install assumption) — uninstall any prior build before installing.
- `lifecycle-viewmodel-navigation3` is pinned to `2.10.0-rc01` as a documented, constitution-sanctioned RC exception (move to stable when available — TODO(NAV3_STABLE)).
- AGP 9 is explicitly out of scope; only the minimal AGP 8.9+/compileSdk 36 bump is performed.
- Commit after each task or logical group; stop at any checkpoint to validate a story independently.
