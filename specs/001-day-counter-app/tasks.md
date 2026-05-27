---
description: "Task list for Day Counter — Streak Habit Tracker"
---

# Tasks: Day Counter — Streak Habit Tracker

**Input**: Design documents from `/specs/001-day-counter-app/`

**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅

**Tests**: Included — constitution Principle II mandates TDD with ≥80% :domain line coverage (Kover gate) and three test layers (unit, integration, UI).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1–US4)
- Each task includes exact file paths

## Path Conventions

Four Gradle modules per plan.md:
- **:domain** → `domain/src/main/kotlin/com/daycounter/domain/`
- **:data** → `data/src/main/kotlin/com/daycounter/data/`
- **:presentation** → `presentation/src/main/kotlin/com/daycounter/presentation/`
- **:app** → `app/src/main/`

---

## Phase 1: Setup (Android Project Initialization)

**Purpose**: Create the 4-module Android project skeleton with all build tooling configured.

- [X] T001 Create root Android project with Gradle wrapper and settings.gradle.kts declaring :app, :domain, :data, :presentation modules
- [X] T002 Configure libs.versions.toml with all dependency versions: Kotlin 2.x, Compose BOM, Hilt, Room KSP, Navigation Compose, WorkManager, DataStore Preferences, Glance AppWidget, Kover, Detekt, JUnit 4, Mockk, Coroutines
- [X] T003 [P] Configure app/build.gradle.kts with com.android.application plugin, applicationId com.daycounter, minSdk 26, compileSdk 35, Hilt plugin, R8 + resource shrinking in release
- [X] T004 [P] Configure domain/build.gradle.kts with java-library plugin and Kotlin — zero android.* imports enforced at compile time
- [X] T005 [P] Configure data/build.gradle.kts with com.android.library plugin, Room KSP, Hilt, WorkManager, DataStore, Glance AppWidget dependencies
- [X] T006 [P] Configure presentation/build.gradle.kts with com.android.library plugin, Compose BOM, Hilt, Navigation Compose, Glance AppWidget, Compose Testing API dependencies
- [X] T007 Configure root build.gradle.kts with Kover plugin (coverage gate ≥80% line coverage on :domain module) and Detekt plugin

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T008 Create domain model Counter with fields id (Long), goalName (String), startDate (LocalDate), createdAt (Instant) — streakDays is derived, never stored — in domain/src/main/kotlin/com/daycounter/domain/model/Counter.kt
- [X] T009 [P] Create domain model MilestoneRecord with id, counterId, milestoneDays, notifiedAt and companion MILESTONE_DAYS = setOf(7,30,60,90,180,365) in domain/src/main/kotlin/com/daycounter/domain/model/MilestoneRecord.kt
- [X] T010 [P] Create domain model WidgetBinding with widgetId (Int), counterId (Long?) in domain/src/main/kotlin/com/daycounter/domain/model/WidgetBinding.kt
- [X] T011 Create repository interfaces CounterRepository (getAllSortedByStreak Flow, getById, insert, update, delete), MilestoneRepository (insertOrIgnore, deleteAllForCounter), WidgetBindingRepository (get, insert, update, delete, setCounterNull) in domain/src/main/kotlin/com/daycounter/domain/repository/
- [X] T012 [P] Create Room entities CounterEntity, MilestoneRecordEntity (UNIQUE counter_id+milestone_days, OnConflictStrategy.IGNORE), WidgetBindingEntity (counter_id ON DELETE SET NULL) with LocalDate↔String and Instant↔Long type converters in data/src/main/kotlin/com/daycounter/data/database/entity/
- [X] T013 [P] Create Room DAOs: CounterDao (getAllCountersSortedByStreak Flow ordered start_date ASC + created_at ASC), MilestoneRecordDao (insertOrIgnore, deleteAllForCounter), WidgetBindingDao (getByWidgetId, insert, update, delete, setCounterNull) in data/src/main/kotlin/com/daycounter/data/database/dao/
- [X] T014 Create AppDatabase (version 1, entities = CounterEntity + MilestoneRecordEntity + WidgetBindingEntity, exportSchema = true, type converters, schema JSON committed to data/schemas/) in data/src/main/kotlin/com/daycounter/data/database/AppDatabase.kt
- [X] T015 Create DayCounterApplication with @HiltAndroidApp annotation and notification channel registration placeholder in app/src/main/kotlin/com/daycounter/DayCounterApplication.kt
- [X] T016 Create Hilt DataModule with @Provides for AppDatabase, all DAOs, and @Binds for CounterRepositoryImpl, MilestoneRepositoryImpl, WidgetBindingRepositoryImpl in data/src/main/kotlin/com/daycounter/data/di/DataModule.kt
- [X] T017 Create MD3 MaterialTheme with dynamic color (API 31+), static fallback palette, dark/light mode support in presentation/src/main/kotlin/com/daycounter/presentation/theme/ (Theme.kt, Color.kt, Type.kt)
- [X] T018 Create Screen sealed class with route strings: Onboarding, Home, CreateCounter, EditCounter(counterId), CounterDetail(counterId), Settings in presentation/src/main/kotlin/com/daycounter/presentation/navigation/Screen.kt
- [X] T019 Create AppNavGraph with NavHost, start-destination logic reading OnboardingPreferencesDataStore.onboardingShown, daycounter://counter/{counterId} deep link registered on CounterDetail destination per navigation-contract.md in presentation/src/main/kotlin/com/daycounter/presentation/navigation/AppNavGraph.kt
- [X] T020 Create MainActivity with @AndroidEntryPoint, edge-to-edge enableEdgeToEdge(), singleTop launchMode, SplashScreen API holding until onboardingShown is read, NavHost, and deep link intent filter (daycounter scheme) in presentation/src/main/kotlin/com/daycounter/presentation/MainActivity.kt + app/src/main/AndroidManifest.xml
- [X] T021 Create auto-backup XML rules explicitly including day_counter.db + *.preferences_pb and excluding androidx.work.workdb: backup_rules.xml (API 26–30) and data_extraction_rules.xml (API 31+) in app/src/main/res/xml/ per research.md Decision 4

**Checkpoint**: Project compiles, app launches to blank scaffold. All user story work can now begin in parallel.

---

## Phase 3: User Story 1 — Create a Personal Streak Counter (Priority: P1) 🎯 MVP

**Goal**: User opens app for the first time, sees 2–3 screen onboarding (once only), creates a counter with a goal name and start date, and immediately sees the elapsed day count on the home screen. Data persists across restarts.

**Independent Test**: Launch app → see 2–3 screen onboarding → complete or skip → home screen shows empty state with "Add your first counter" CTA → tap "Add Counter" → enter "No alcohol", select start date 7 days ago → home screen shows "No alcohol — 7 days" → close and reopen app → counter still shows correct count.

### Unit Tests for User Story 1 ⚠️ Write FIRST — must FAIL before implementation

- [X] T022 [P] [US1] Unit test CalculateStreakUseCase: startDate=today→0, startDate=yesterday→1, startDate=7 days ago→7 in domain/src/test/kotlin/com/daycounter/domain/usecase/CalculateStreakUseCaseTest.kt
- [X] T023 [P] [US1] Unit test CreateCounterUseCase: blank name→ValidationError, name>100 chars→ValidationError, future startDate→ValidationError, null startDate→defaults to today, valid inputs→counter inserted via repository in domain/src/test/kotlin/com/daycounter/domain/usecase/CreateCounterUseCaseTest.kt
- [X] T024 [P] [US1] Unit test GetAllCountersUseCase: emitted Flow list is sorted startDate ASC (longest streak first) with createdAt ASC as tie-breaker in domain/src/test/kotlin/com/daycounter/domain/usecase/GetAllCountersUseCaseTest.kt

### Implementation for User Story 1

- [X] T025 [P] [US1] Implement CalculateStreakUseCase using ChronoUnit.DAYS.between(startDate, LocalDate.now(ZoneId.systemDefault())) in domain/src/main/kotlin/com/daycounter/domain/usecase/CalculateStreakUseCase.kt
- [X] T026 [P] [US1] Implement GetAllCountersUseCase (delegates to CounterRepository.getAllSortedByStreak Flow) and GetCounterByIdUseCase (returns Counter? for a given id) in domain/src/main/kotlin/com/daycounter/domain/usecase/
- [X] T027 [US1] Implement CreateCounterUseCase with goalName blank/length/future-date validation (sealed Result type) and CounterRepository.insert call in domain/src/main/kotlin/com/daycounter/domain/usecase/CreateCounterUseCase.kt
- [X] T028 [US1] Implement CounterRepositoryImpl mapping CounterEntity↔Counter, delegating to CounterDao (getAllCountersSortedByStreak Flow, getById, insert, update, delete) in data/src/main/kotlin/com/daycounter/data/repository/CounterRepositoryImpl.kt
- [X] T029 [US1] Implement OnboardingPreferencesDataStore with onboarding_shown Boolean key (default false), read as Flow<Boolean>, write suspend function in data/src/main/kotlin/com/daycounter/data/datastore/OnboardingPreferencesDataStore.kt
- [X] T030 [US1] Implement OnboardingViewModel reading onboardingShown from DataStore, writing true on skip or final screen completion, emitting NavigateToHome UiEvent in presentation/src/main/kotlin/com/daycounter/presentation/onboarding/OnboardingViewModel.kt
- [X] T031 [US1] Implement OnboardingScreen with HorizontalPager (2–3 pages), skip button on every page (48 dp touch target, TalkBack content description), marks shown on last page or skip tap, navigates to Home (popping onboarding from back stack) in presentation/src/main/kotlin/com/daycounter/presentation/onboarding/OnboardingScreen.kt
- [X] T032 [US1] Implement HomeViewModel collecting GetAllCountersUseCase Flow, mapping each Counter to UI state (id, goalName, streakDays via CalculateStreakUseCase) sorted by streakDays DESC + createdAt ASC, exposed as StateFlow<HomeUiState> in presentation/src/main/kotlin/com/daycounter/presentation/home/HomeViewModel.kt
- [X] T033 [US1] Implement HomeScreen with LazyColumn counter list, empty-state composable showing "Add your first counter" CTA when list is empty, FAB "Add Counter" (48 dp, TalkBack label) navigating to CreateCounter, Settings icon in TopAppBar in presentation/src/main/kotlin/com/daycounter/presentation/home/HomeScreen.kt
- [X] T034 [US1] Implement CreateCounterViewModel with goalName + startDate StateFlow, validation via CreateCounterUseCase, expose UiState (idle/loading/error) and UiEvent (NavigateBack, ShowValidationError) in presentation/src/main/kotlin/com/daycounter/presentation/counter/CreateCounterViewModel.kt
- [X] T035 [US1] Implement CreateCounterScreen with TextField for goal name (1–100 chars), MD3 DatePicker for startDate (default today, future dates disabled), inline validation error Text composables, Save button (disabled until inputs valid), POST_NOTIFICATIONS permission request via ActivityResultContracts.RequestPermission on first tap in presentation/src/main/kotlin/com/daycounter/presentation/counter/CreateCounterScreen.kt

**Checkpoint**: App launches → onboarding shown exactly once → home screen with empty state → create counter → correct streak displayed → persists after restart. User Story 1 is fully functional independently.

---

## Phase 4: User Story 2 — Manage Existing Counters (Priority: P2)

**Goal**: User can edit the name or start date of an existing counter, reset a counter back to day 0, and permanently delete a counter — all with confirmation dialogs where applicable.

**Independent Test**: With one counter on screen, tap Edit → change name → verify updated name on home screen and after restart. Tap Reset → confirmation dialog appears → confirm → streak shows 0. Tap Delete → confirmation dialog → confirm → counter gone from list. Other counters unaffected.

### Unit Tests for User Story 2 ⚠️ Write FIRST — must FAIL before implementation

- [X] T036 [P] [US2] Unit test UpdateCounterUseCase: blank name→ValidationError, name>100 chars→ValidationError, future startDate→ValidationError, valid inputs→CounterRepository.update called with updated fields in domain/src/test/kotlin/com/daycounter/domain/usecase/UpdateCounterUseCaseTest.kt
- [X] T037 [P] [US2] Unit test ResetCounterUseCase: calls MilestoneRepository.deleteAllForCounter then CounterRepository.update with startDate=today; resulting streakDays=0 in domain/src/test/kotlin/com/daycounter/domain/usecase/ResetCounterUseCaseTest.kt
- [X] T038 [P] [US2] Unit test DeleteCounterUseCase: calls CounterRepository.delete; Room ON DELETE CASCADE handles MilestoneRecord and WidgetBinding rows in domain/src/test/kotlin/com/daycounter/domain/usecase/DeleteCounterUseCaseTest.kt

### Implementation for User Story 2

- [X] T039 [P] [US2] Implement UpdateCounterUseCase with same goalName/startDate validation as CreateCounterUseCase, calling CounterRepository.update in domain/src/main/kotlin/com/daycounter/domain/usecase/UpdateCounterUseCase.kt
- [X] T040 [P] [US2] Implement DeleteCounterUseCase calling CounterRepository.delete (Room ON DELETE CASCADE removes MilestoneRecord and sets WidgetBinding.counterId = NULL) in domain/src/main/kotlin/com/daycounter/domain/usecase/DeleteCounterUseCase.kt
- [X] T041 [US2] Implement MilestoneRepositoryImpl mapping MilestoneRecordEntity↔MilestoneRecord, delegating to MilestoneRecordDao (insertOrIgnore, deleteAllForCounter) in data/src/main/kotlin/com/daycounter/data/repository/MilestoneRepositoryImpl.kt
- [X] T042 [US2] Implement ResetCounterUseCase: call MilestoneRepository.deleteAllForCounter(counterId) then CounterRepository.update with startDate = LocalDate.now() in domain/src/main/kotlin/com/daycounter/domain/usecase/ResetCounterUseCase.kt
- [X] T043 [US2] Implement EditCounterViewModel loading counter via GetCounterByIdUseCase, exposing pre-populated form state, calling UpdateCounterUseCase / ResetCounterUseCase / DeleteCounterUseCase, managing confirmation dialog show/hide state, emitting UiEvent (SaveSuccess, Deleted, ShowError) in presentation/src/main/kotlin/com/daycounter/presentation/counter/EditCounterViewModel.kt
- [X] T044 [US2] Implement EditCounterScreen with pre-populated TextField (goal name) and DatePicker (startDate), Save button, Reset button triggering AlertDialog confirmation before ResetCounterUseCase, Delete button triggering AlertDialog confirmation before DeleteCounterUseCase, 48 dp touch targets + TalkBack labels in presentation/src/main/kotlin/com/daycounter/presentation/counter/EditCounterScreen.kt
- [X] T045 [US2] Wire EditCounter navigation from counter card tap on HomeScreen (passing counterId argument) and add CounterDetail route composable for widget/notification deep links; validate counterId against Room before navigating in presentation/src/main/kotlin/com/daycounter/presentation/home/HomeScreen.kt + navigation/AppNavGraph.kt

**Checkpoint**: Edit, reset, and delete all work with confirmation dialogs. Remaining counters are unaffected. User Stories 1 and 2 independently functional.

---

## Phase 5: User Story 3 — Home Screen Widget (Priority: P3)

**Goal**: User places a compact (2×1) or medium (4×2) Day Counter widget on the home screen, maps it to one counter via a picker, widget updates daily without opening app, handles deleted counter with a graceful placeholder, and tapping widget opens the correct counter.

**Independent Test**: Create one counter. Add widget to home screen → CounterPickerActivity opens → select counter → widget shows correct streak. Without opening app, simulate day change and confirm widget count incremented. Delete counter in app → widget shows "Counter removed — tap to select a new one".

### Unit Tests for User Story 3 ⚠️ Write FIRST — must FAIL before implementation

- [X] T046 [P] [US3] Unit test DailyUpdateWorker widget refresh path: for each WidgetBinding with non-null counterId, updates Glance state with correct streakDays; for null counterId, sets isCounterDeleted=true in data/src/test/kotlin/com/daycounter/data/work/DailyRefresherTest.kt (delegates to WidgetRefresher; concrete Glance update is integration-tested)

### Implementation for User Story 3

- [X] T047 [P] [US3] Implement WidgetBindingRepositoryImpl mapping WidgetBindingEntity↔WidgetBinding, delegating to WidgetBindingDao (getByWidgetId, insert, update, delete, setCounterNull) in data/src/main/kotlin/com/daycounter/data/repository/WidgetBindingRepositoryImpl.kt
- [X] T048 [P] [US3] Create DayCounterWidgetState @Serializable data class (counterId: Long?, goalName: String, streakDays: Int, isCounterDeleted: Boolean) and GlanceStateDefinition in presentation/src/main/kotlin/com/daycounter/presentation/widget/DayCounterWidgetState.kt
- [X] T049 [US3] Implement DayCounterWidget GlanceAppWidget: compact layout (2×1 — streak count large + goal name 1-line ellipsis) and medium layout (4×2 — goal name 2 lines + streak count + "days" label) branching on LocalSize.current; isCounterDeleted=true shows "Counter removed" placeholder with "Select counter" button; tap PendingIntent with daycounter://counter/{counterId} URI (FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE) per widget-contract.md in presentation/src/main/kotlin/com/daycounter/presentation/widget/DayCounterWidget.kt
- [X] T050 [US3] Implement DayCounterWidgetReceiver extending GlanceAppWidgetReceiver; handle onDeleted calling WidgetBindingRepository.delete for removed widget IDs in presentation/src/main/kotlin/com/daycounter/presentation/widget/DayCounterWidgetReceiver.kt
- [X] T051 [US3] Implement CounterPickerActivity with @AndroidEntryPoint, counter list UI (LazyColumn), saves WidgetBinding via WidgetBindingRepository on counter selection, calls GlanceAppWidgetManager.getGlanceIdBy to refresh Glance state, returns RESULT_OK with appWidgetId in presentation/src/main/kotlin/com/daycounter/presentation/widget/CounterPickerActivity.kt
- [X] T052 [US3] Create day_counter_widget_info.xml AppWidget provider metadata (minWidth 110dp, minHeight 40dp, targetCellWidth 2, targetCellHeight 1, maxResizeWidth 250dp, maxResizeHeight 110dp, resizeMode horizontal|vertical, updatePeriodMillis 0, configure CounterPickerActivity, previewLayout widget_preview) in presentation/src/main/res/xml/day_counter_widget_info.xml
- [X] T053 [US3] Create widget_preview.xml RemoteViews layout showing placeholder streak count for AppWidget gallery preview in presentation/src/main/res/layout/widget_preview.xml
- [X] T054 [US3] Implement DailyUpdateWorker as CoroutineWorker: PeriodicWorkRequest with 1-day period and 15-minute flex window, initial delay to next local midnight; queries all counters + widget bindings; for each binding calls GlanceAppWidgetManager.updateIf<DayCounterWidget> with fresh DayCounterWidgetState in data/src/main/kotlin/com/daycounter/data/work/DailyUpdateWorker.kt
- [X] T055 [US3] Implement BootReceiver handling BOOT_COMPLETED: re-enqueues DailyUpdateWorker as unique PeriodicWork (ExistingPeriodicWorkPolicy.KEEP) if not already enqueued in data/src/main/kotlin/com/daycounter/data/receiver/BootReceiver.kt
- [X] T056 [US3] Declare DayCounterWidgetReceiver (exported=true, APPWIDGET_UPDATE intent-filter + appwidget-provider meta-data), CounterPickerActivity (exported=false, widget configure), BootReceiver (exported=true), RECEIVE_BOOT_COMPLETED permission in app/src/main/AndroidManifest.xml; add WidgetBindingRepositoryImpl Hilt binding to DataModule in data/src/main/kotlin/com/daycounter/data/di/DataModule.kt
- [X] T057 [US3] After each counter CRUD operation (create/edit/reset/delete) in ViewModels, trigger Glance state refresh for all widget bindings of the affected counter via WidgetBindingRepository + GlanceAppWidgetManager.updateIf call in presentation/src/main/kotlin/com/daycounter/presentation/ (CreateCounterViewModel, EditCounterViewModel)

**Checkpoint**: Widget places on home screen, picker selects counter, daily update increments count, deleted counter shows placeholder, tap opens counter detail. User Story 3 independently functional.

---

## Phase 6: User Story 4 — Milestone Notifications (Priority: P4)

**Goal**: App sends local notifications at 7/30/60/90/180/365-day milestones. Notifications include counter name and milestone. Tapping opens the correct counter. No duplicates — reset counter clears records, allowing re-notification. Respects OS permission and in-app toggle. If device was off at midnight, delivers on next boot.

**Independent Test**: Create counter with startDate = today-6 (streak=6). Advance device date by 1 day (streak=7). Manually trigger DailyUpdateWorker. Notification appears with counter name and "7-day milestone". Trigger again — no second notification. Reset counter → trigger after 7 more days → notification fires again. Disable in-app toggle → trigger → no notification.

### Unit Tests for User Story 4 ⚠️ Write FIRST — must FAIL before implementation

- [X] T058 [P] [US4] Unit test CheckMilestonesUseCase: streakDays == milestone in set → returns that milestone; streakDays != any milestone → returns null; all six milestone values tested in domain/src/test/kotlin/com/daycounter/domain/usecase/CheckMilestonesUseCaseTest.kt
- [X] T059 [P] [US4] Unit test DailyUpdateWorker milestone path: refresher invokes notifier once per counter (DailyRefresherTest); dedup semantics covered via :app androidTest of AndroidMilestoneNotifier

### Implementation for User Story 4

- [X] T060 [P] [US4] Implement CheckMilestonesUseCase: returns the milestone Int if streakDays equals any value in MILESTONE_DAYS set, else returns null in domain/src/main/kotlin/com/daycounter/domain/usecase/CheckMilestonesUseCase.kt
- [X] T061 [P] [US4] Implement NotificationPreferencesDataStore with notifications_enabled Boolean key (default true) and notification_permission_requested Boolean key (default false) in data/src/main/kotlin/com/daycounter/data/datastore/NotificationPreferencesDataStore.kt
- [X] T062 [US4] Milestone check step lives in :app/AndroidMilestoneNotifier (called from DailyRefresher per counter): streak via CalculateStreakUseCase → CheckMilestonesUseCase → MilestoneRepository.insertOrIgnore → notifications_enabled + OS permission gate → NotificationCompat.notify
- [X] T063 [US4] Register "milestone_notifications" NotificationChannel (IMPORTANCE_HIGH) in DayCounterApplication.onCreate; declare POST_NOTIFICATIONS permission in app/src/main/AndroidManifest.xml
- [X] T064 [US4] Tap PendingIntent uses daycounter://counter/{counterId} deep link with FLAG_IMMUTABLE; counterId.toInt() as notification ID in AndroidMilestoneNotifier
- [X] T065 [US4] Implement SettingsViewModel reading notifications_enabled from NotificationPreferencesDataStore as StateFlow<Boolean>; write on toggle in presentation/src/main/kotlin/com/daycounter/presentation/settings/SettingsViewModel.kt
- [X] T066 [US4] Implement SettingsScreen with MD3 Switch wired to SettingsViewModel notifications_enabled StateFlow, 48 dp touch target, TalkBack content description; Settings icon navigation from HomeScreen TopAppBar to Settings route
- [X] T067 [US4] POST_NOTIFICATIONS permission request triggered on first CreateCounter open via ActivityResultContracts.RequestPermission; permission_requested flag stored in NotificationPreferencesDataStore (re-prompt suppressed)

**Checkpoint**: Milestone notifications fire once per counter+milestone combination, respect in-app toggle and OS permission, no-expiry delivery on next boot, tap opens correct counter. User Story 4 independently functional.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories — accessibility, error handling, static analysis, performance.

- [X] T068 [P] TalkBack contentDescription added to all interactive elements: counter cards (state: name + streak), FAB, onboarding skip, settings switch, edit reset/delete actions, widget "Select counter"
- [X] T069 [P] FR-018 storage error handling: CreateCounterViewModel and EditCounterViewModel emit ShowStorageError on caught exceptions; both screens display a snackbar with counter_error_storage
- [X] T070 [P] HomeViewModelOrderingTest verifies tied streakDays preserve repository createdAt-ASC order
- [X] T071 detekt.yml configured at repo root with project-tuned rules; KDoc applied to public :domain and :data symbols (models, repositories, use cases, DAOs, DataStore, DailyRefresher, WidgetStateUpdater)
- [ ] T072 [P] Verify Kover coverage gate (`./gradlew koverHtmlReport`) — must be run on a build host with Gradle wrapper
- [ ] T073 Validate quickstart.md (`assembleDebug`, `test`, `lint`, `detekt`, `connectedAndroidTest`, `bundleRelease`, cold-start timing) — must be run on a build host

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 completion — BLOCKS all user stories
- **User Stories (Phase 3–6)**: All depend on Phase 2 completion
  - Can proceed in priority order (P1 → P2 → P3 → P4) or in parallel if staffed
- **Polish (Phase 7)**: Depends on all desired user stories being complete

### User Story Dependencies

- **US1 (P1)**: Can start after Phase 2 — no dependencies on other stories
- **US2 (P2)**: Can start after Phase 2 — requires CounterRepositoryImpl from US1 (T028); MilestoneRepositoryImpl is new (T041)
- **US3 (P3)**: Can start after Phase 2 — requires WidgetBindingRepositoryImpl (T047, new); DailyUpdateWorker initial widget path (T054) is new
- **US4 (P4)**: Can start after Phase 2 — extends DailyUpdateWorker (T062) built in US3; if US3 not yet done, DailyUpdateWorker can be implemented fresh here

### Within Each User Story

- Unit tests MUST be written first and MUST FAIL before implementation begins
- Domain models and use cases before repository implementations
- Repository implementations before ViewModels
- ViewModels before screens
- Story complete and independently testable before moving to next priority

### Parallel Opportunities

- All [P]-marked tasks within a phase can run in parallel
- T003–T007 (module build files) all run in parallel after T002
- T008–T010 (domain models) all run in parallel after T007
- T012–T013 (Room entities + DAOs) run in parallel after T011
- T022–T024 (US1 unit tests) all run in parallel
- T025–T026 (US1 use case implementations) run in parallel
- T036–T038 (US2 unit tests) all run in parallel
- T039–T040 (US2 use case implementations) run in parallel
- T047–T048 (US3 widget foundation) run in parallel after T021
- T058–T059 (US4 unit tests) run in parallel

---

## Parallel Example: User Story 1

```
# Write all US1 unit tests first (parallel):
T022 — CalculateStreakUseCaseTest
T023 — CreateCounterUseCaseTest
T024 — GetAllCountersUseCaseTest

# Then implement use cases in parallel:
T025 — CalculateStreakUseCase
T026 — GetAllCountersUseCase + GetCounterByIdUseCase

# Then sequentially (each depends on above):
T027 → T028 → T029 → T030 → T031 → T032 → T033 → T034 → T035
```

## Parallel Example: User Story 3

```
# Parallel foundation tasks:
T047 — WidgetBindingRepositoryImpl
T048 — DayCounterWidgetState + GlanceStateDefinition

# Sequential widget build (each depends on above):
T049 → T050 → T051 → T052 → T053 → T054 → T055 → T056 → T057
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL — blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test US1 independently (onboarding → create counter → streak persists)
5. Demo / release internal build

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. US1 → Test independently → **MVP: core habit tracking works**
3. US2 → Test independently → Users can manage their counters
4. US3 → Test independently → Widget passive motivation
5. US4 → Test independently → Milestone celebration notifications
6. Polish → Production-ready release

### Parallel Team Strategy

With multiple developers:
1. Team completes Phase 1 + Phase 2 together
2. Once Phase 2 is done:
   - Dev A: US1 (Counter CRUD + onboarding + home screen)
   - Dev B: US3 (Widget infrastructure — WidgetBinding, Glance, DailyUpdateWorker)
   - US2 and US4 follow after their respective prerequisites

---

## Notes

- [P] tasks = different files, no cross-task dependencies — safe to run in parallel
- [USn] label maps each task to a specific user story for traceability
- Each user story is independently completable and testable
- Unit tests (T022–T024, T036–T038, T046, T058–T059) MUST fail before implementation
- Commit after each task or logical group (pre-commit hook: `/speckit-git-commit`)
- Stop at any phase checkpoint to validate the story independently
- Android skills that may assist during implementation: `jetpack-compose-m3` (T017, T031–T035, T044, T049, T066), `edge-to-edge` (T020)
