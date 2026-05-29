# Quickstart: Screens and Navigation Overhaul

**Feature**: `002-screens-and-navigation` | **Date**: 2026-05-28

This iteration builds on the existing `001-day-counter-app` codebase — same four Gradle
modules, same toolchain. No new module, no new third-party dependency.

---

## Prerequisites

- JDK 17, Android Studio (latest stable) or command-line Gradle.
- **Android SDK with API 36 platform** (Navigation 3 requires `compileSdk = 36`); an
  emulator/device on **API 26+**.
- Toolchain changes this iteration introduces in `gradle/libs.versions.toml` /
  `build.gradle.kts`:
  - **AGP 8.7.3 → 8.9+** (needed to target compileSdk 36).
  - **`compileSdk`/`targetSdk` 35 → 36**; `minSdk` stays 26.
  - **Add** `androidx.navigation3:navigation3-runtime` + `navigation3-ui` `1.0.0` and
    `androidx.lifecycle:lifecycle-viewmodel-navigation3` `2.10.0-rc01`; **remove**
    `navigation-compose` (2.8.5).
- Unchanged pins: Kotlin 2.0.21, Compose BOM 2024.12.01, Hilt 2.52, Room 2.6.1,
  WorkManager 2.9.1, KotlinX Serialization (used for `@Serializable` NavKeys).

---

## Build & run

```powershell
# from repo root: C:\SpecKit\DayCounter\day-counter-app
.\gradlew.bat assembleDebug          # build the debug APK
.\gradlew.bat installDebug           # install on a connected device/emulator
```

> **Fresh install required.** This iteration bumps the Room schema and uses
> `fallbackToDestructiveMigration`. If a previous `001` build is installed, uninstall it first
> (`adb uninstall com.daycounter`) so the new schema is created cleanly — there is no migration
> path by design (spec Assumptions).

---

## Test (TDD — write failing tests first, Principle II)

```powershell
.\gradlew.bat :domain:test                       # unit: use cases (reset txn, stats, milestone math)
.\gradlew.bat :data:test                          # integration: DAOs, reset transaction, pagination
.\gradlew.bat :presentation:testDebugUnitTest     # ViewModel unit tests
.\gradlew.bat connectedDebugAndroidTest           # Compose UI tests (every acceptance scenario)
.\gradlew.bat :domain:koverVerify                 # coverage gate ≥ 80% on :domain
.\gradlew.bat detekt lintDebug                    # static analysis: zero errors
```

New UI tests for Stats, History, Celebration, the enriched Home/Detail, and the
Create/Edit/Reset sheets MUST fail against the unbuilt screens before implementation.

---

## What changes in this iteration (orientation map)

| Area | Change |
|------|--------|
| **Navigation lib** | **Migrate Nav 2 → Navigation 3**: `NavHost`/`NavController` → `NavDisplay` + `entryProvider`; string routes → `@Serializable … : NavKey`; tabs via `TopLevelBackStack` (one stack/tab); sheets via copied-in `BottomSheetSceneStrategy`; deep link via synthetic back stack; ViewModels via `rememberViewModelStoreNavEntryDecorator` + Hilt assisted injection. Forces compileSdk 36 + AGP bump. |
| Navigation UX | `MainScaffold` shows the `NavigationBar` only on tab entries (hidden on Detail/History/Celebration). Home→Edit nav **removed** (card → Detail). |
| Data model | `Counter` + `category`, `goalMilestoneTarget`; `MilestoneRecord` + `celebrationShown` and new set `{1,7,30,100,365,1000}`; new `PastStreakRecord` table. |
| Domain | New use cases: ArchiveAndReset, GetStatsSummary, GetNextMilestone, GetAchievedMilestones, GetMostRecentMilestone, MarkCelebrationsShown, GetPastStreaks. |
| UI — Home | Summary cards + per-card progress ring + "Hito alcanzado" badge; "+" opens Create sheet. |
| UI — Detail | Hero ring, next-milestone hint, achieved chips, Editar/Reiniciar/Eliminar/Historial/Revivir; auto-launch celebration on resume. |
| UI — new | Stats tab, History (current-month calendar + sparkline + paged past streaks), Milestone Celebration overlay. |
| UI — refactor | Create/Edit/Reset are bottom-sheet **entries** (old full-screen Create/Edit routes deleted). Settings becomes the Ajustes tab. |
| Localization | Default locale English (`values/strings.xml`); Spanish copy in **`values-es/strings.xml`** (additive); no hardcoded literals. |
| Reusable | `components/ProgressRing.kt`, `Sparkline.kt`, `MonthCalendarGrid.kt`. |

---

## Manual verification (maps to spec Independent Tests)

1. **US1 shell**: launch with ≥1 counter → three tabs visible; switch tabs; tap a card →
   bottom bar disappears; back → bar returns. Detail→History→back returns to Detail.
2. **US2 list**: create counters with streaks 3 / 15 / 102 (one past its goal target) →
   summary totals correct; rings proportional; the over-target card shows "Hito alcanzado".
3. **US3 detail**: open a 35-day counter → hero "35", "5 días para tu próximo hito", chips
   `1, 7, 30`; each action works; achieved chips not tappable.
4. **US4 celebration**: create a counter dated exactly 7 days ago → opening Detail auto-launches
   the 7-day copy; "Seguir así"; reopen Detail → no auto-launch; "Revivir" reopens it on demand.
5. **US5 stats**: counters 5/30/120 → Total 155, Mejor 120, Activos 3.
6. **US6 sheets**: "+" slides up the Create sheet; save "Dejar de fumar" / Salud / today / goal 30 →
   card with ring sized to 30; Edit → date read-only with Reiniciar hint.
7. **US7 reset**: counter dated 15 days ago → Reiniciar → Confirmar → streak 0, counter persists,
   15-day past streak appears in History "Rachas anteriores".
8. **US8 history**: 12-day counter reset twice (3 & 14 days) → current month calendar highlights
   streak days, today distinct, sparkline 12 points, both prior runs listed with reasons/dates.

---

## Definition of done (per task / pre-merge checklist)

- Failing tests written first; all tests green in CI (Principle II).
- `:domain` has **zero** `android.*` imports (Principle III).
- No prohibited stack tech; no new third-party dependency without justification (Principle IV/V).
- Lint zero errors; Detekt no new findings without justified suppression (Principle V).
- All strings externalized; **English base `values/`** + **Spanish `values-es/`** variant
  (informal "tú", no emoji) — no hardcoded literals (FR-032 / Localization standard).
- UI checked on one compact + one expanded window size; TalkBack smoke; dark-mode visual (Principle I).
- `android:exported` unchanged (no new components); deep-link `counterId` validated (Principle VI).
