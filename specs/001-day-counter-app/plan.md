# Implementation Plan: Day Counter — Streak Habit Tracker

**Branch**: `001-day-counter-app` | **Date**: 2026-05-27 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-day-counter-app/spec.md`

---

## Summary

Build an Android application (minSdk 26) that lets users create, view, edit, reset, and
delete day-streak counters for personal goals. The app includes a one-time onboarding flow,
two home-screen widget sizes powered by Jetpack Glance, local milestone notifications via
WorkManager, an in-app notification toggle, and full Android auto-backup support. All data
is stored locally via Room and DataStore; no network access is required for any feature.

Architecture follows the constitution's three-layer Clean Architecture
(`:domain` / `:data` / `:presentation`) with a fourth `:app` entry-point module, Jetpack
Compose UI, Hilt DI, `StateFlow` state management, and WorkManager for all background
scheduling.

---

## Technical Context

**Language/Version**: Kotlin 2.x (latest stable) — constitution-mandated

**Primary Dependencies**: Jetpack Compose BOM (latest stable), Hilt, Room, Navigation
Compose, WorkManager, DataStore (Preferences), Glance AppWidget, Coroutines + Flow,
Kover (coverage), Detekt (static analysis)

**Storage**: Room — counters, milestone records, widget bindings; DataStore (Preferences)
— onboarding-shown flag and notification-enabled toggle

**Testing**: JUnit 4 + Mockk (unit), Hilt Test + Room in-memory DB (integration),
Compose Testing API (UI), Kover (coverage gate ≥ 80% on `:domain`)

**Target Platform**: Android 8.0+ (API 26+), covering ~97% of active Android devices

**Project Type**: Multi-module Android application (4 Gradle modules)

**Performance Goals**: Cold start < 2 s on mid-range device (Pixel 4a equivalent);
home screen list renders < 1 s with 50+ counters; widget count refreshes within 1 hour
of midnight

**Constraints**: Fully offline — no network permission required; release AAB ≤ 10 MB;
milestone notifications must survive device power-off and deliver on next boot; no expiry
window

**Scale/Scope**: Single-user; up to 50 simultaneous counters; 2 widget sizes (2×1 compact,
4×2 medium); 6 milestone levels (7, 30, 60, 90, 180, 365 days)

---

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-evaluated after Phase 1 design.*

| Principle | Gate Condition | Status | Notes |
|-----------|---------------|--------|-------|
| I — UX First | MD3 theme; 48 dp touch targets; TalkBack content descriptions; dark mode + dynamic color; adaptive layouts (compact/medium/expanded); edge-to-edge; cold start < 2 s | ✅ PASS | All UX success criteria captured in spec (SC-001, SC-002, SC-007). Widget exposes two sizes for adaptive home-screen placement. |
| II — TDD | Three test layers present; ≥ 80% :domain line coverage (Kover); UI test for every acceptance scenario in spec | ✅ PASS | 22 acceptance scenarios across 4 user stories + 6 edge cases require corresponding UI/unit tests before implementation begins. |
| III — Clean Architecture | `:domain` zero `android.*` imports; `presentation → domain ← data` dependency direction; `StateFlow`/`SharedFlow` state; single-responsibility use cases | ✅ PASS | Module boundaries enforced at compile time via Gradle module deps. WorkManager workers and Room DAOs live in `:data`, never in `:presentation`. |
| IV — MAD Stack | Kotlin 2.x, Compose, ViewModel+StateFlow (no LiveData/RxJava), Hilt, Coroutines+Flow, Navigation Compose, Room, DataStore (not SharedPrefs), WorkManager (not bare AlarmManager), Version Catalogs | ✅ PASS | Glance AppWidget is the Compose-native widget API — compliant. No prohibited alternatives in scope. |
| V — Code Quality | Lint zero errors; Detekt configured; R8 + resource shrinking in release; KDoc on all public `:domain`/`:data` symbols; no dead code | ✅ PASS | Enforced via CI gates and pre-merge checklist. |
| VI — Security & Privacy | `android:exported` explicit on all components; no secrets hardcoded; no PII logged in release; OWASP M4 input validation; no sensitive data in backup | ✅ PASS | App is fully offline — cleartext/cert-pinning rules N/A. `AppWidgetProvider`, notification `PendingIntent` Activity, and `BOOT_COMPLETED` receiver require `exported=true` with explicit permissions. Goal names are user data and must not appear in release logs. Counter IDs validated before navigation (widget tap, notification tap). |

**Post-Phase-1 re-check**: No violations introduced by data model or contracts. `PendingIntent`
uses `FLAG_IMMUTABLE`. Exported component declarations specified in `AndroidManifest.xml`.
Room `MilestoneRecord` deduplication prevents notification spam.

---

## Project Structure

### Documentation (this feature)

```text
specs/001-day-counter-app/
├── plan.md              # This file (/speckit-plan output)
├── research.md          # Phase 0 — architectural decisions and rationale
├── data-model.md        # Phase 1 — entities, relationships, validation rules
├── quickstart.md        # Phase 1 — dev environment setup and build guide
├── contracts/
│   ├── widget-contract.md       # GlanceAppWidget state model and PendingIntent actions
│   └── navigation-contract.md  # Navigation Compose route schema and deep link URIs
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created by /speckit-plan)
```

### Source Code

```text
app/                              # Android application module — entry point + Hilt component root
├── src/main/
│   ├── kotlin/com/daycounter/
│   │   └── DayCounterApplication.kt
│   ├── AndroidManifest.xml       # Permissions, all component declarations with android:exported
│   └── res/xml/
│       ├── backup_rules.xml      # fullBackupContent (API 26–30)
│       └── data_extraction_rules.xml  # dataExtractionRules (API 31+)
└── build.gradle.kts

domain/                           # Pure Kotlin module — zero android.* dependencies
├── src/main/kotlin/com/daycounter/domain/
│   ├── model/
│   │   ├── Counter.kt
│   │   ├── MilestoneRecord.kt
│   │   └── WidgetBinding.kt
│   ├── repository/
│   │   ├── CounterRepository.kt
│   │   ├── MilestoneRepository.kt
│   │   └── WidgetBindingRepository.kt
│   └── usecase/
│       ├── GetAllCountersUseCase.kt
│       ├── GetCounterByIdUseCase.kt
│       ├── CreateCounterUseCase.kt
│       ├── UpdateCounterUseCase.kt
│       ├── DeleteCounterUseCase.kt
│       ├── ResetCounterUseCase.kt
│       ├── CalculateStreakUseCase.kt
│       └── CheckMilestonesUseCase.kt
├── src/test/kotlin/com/daycounter/domain/  # Unit tests — Kover gate ≥ 80%
└── build.gradle.kts

data/                             # Implements domain interfaces; owns Room, DataStore, WorkManager
├── src/main/kotlin/com/daycounter/data/
│   ├── database/
│   │   ├── AppDatabase.kt
│   │   ├── entity/
│   │   │   ├── CounterEntity.kt
│   │   │   ├── MilestoneRecordEntity.kt
│   │   │   └── WidgetBindingEntity.kt
│   │   └── dao/
│   │       ├── CounterDao.kt
│   │       ├── MilestoneRecordDao.kt
│   │       └── WidgetBindingDao.kt
│   ├── datastore/
│   │   ├── NotificationPreferencesDataStore.kt
│   │   └── OnboardingPreferencesDataStore.kt
│   ├── repository/
│   │   ├── CounterRepositoryImpl.kt
│   │   ├── MilestoneRepositoryImpl.kt
│   │   └── WidgetBindingRepositoryImpl.kt
│   ├── work/
│   │   └── DailyUpdateWorker.kt  # Runs nightly: recalc streaks + milestone checks + widget refresh
│   ├── receiver/
│   │   └── BootReceiver.kt       # BOOT_COMPLETED → re-enqueue DailyUpdateWorker if needed
│   └── di/
│       └── DataModule.kt         # Hilt @Module bindings (repos, DAOs, DataStore instances)
├── src/test/kotlin/com/daycounter/data/  # Integration tests (Hilt + Room in-memory)
└── build.gradle.kts

presentation/                     # Jetpack Compose UI + ViewModels — depends on :domain only
├── src/main/kotlin/com/daycounter/presentation/
│   ├── MainActivity.kt
│   ├── navigation/
│   │   ├── AppNavGraph.kt        # NavHost with all routes and deep links
│   │   └── Screen.kt             # Sealed class of route strings
│   ├── onboarding/
│   │   ├── OnboardingScreen.kt
│   │   └── OnboardingViewModel.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt      # Sorted by streak desc; day-change recomposition via Flow
│   ├── counter/
│   │   ├── CreateCounterScreen.kt
│   │   ├── CreateCounterViewModel.kt
│   │   ├── EditCounterScreen.kt
│   │   └── EditCounterViewModel.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt  # Notification enabled/disabled toggle (FR-017)
│   ├── widget/
│   │   ├── DayCounterWidget.kt         # GlanceAppWidget implementation (compact + medium layouts)
│   │   ├── DayCounterWidgetReceiver.kt # GlanceAppWidgetReceiver (AppWidgetProvider)
│   │   └── CounterPickerActivity.kt    # Shown when adding a new widget instance (picker UI)
│   └── theme/
│       ├── Theme.kt              # MaterialTheme with dynamic color
│       ├── Color.kt
│       └── Type.kt
├── src/test/kotlin/com/daycounter/presentation/  # UI tests (Compose Testing API)
└── build.gradle.kts
```

**Structure Decision**: Four Gradle modules enforce constitution Principle III layer
boundaries at compile time. `:app` is the Android `application` plugin module that provides
the Hilt component root, the manifest, and backup configuration XML. `:domain` declares the
`java-library` plugin with zero Android SDK imports — the Gradle compile check is the
enforcer. `:data` and `:presentation` apply the `com.android.library` plugin and depend
on `:domain`.

---

## Complexity Tracking

> No constitution violations — table not required.
