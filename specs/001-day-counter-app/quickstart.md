# Developer Quickstart: Day Counter

**Feature branch**: `001-day-counter-app` | **Date**: 2026-05-27

---

## Prerequisites

| Tool | Minimum Version | Notes |
|------|----------------|-------|
| Android Studio | Ladybug (2024.2.1) or later | Required for Compose previews and Glance widget support |
| JDK | 17 | Bundled with Android Studio |
| Android SDK | API 35 (compileSdk) | Install via SDK Manager |
| Android Emulator or device | API 26+ (minSdk) | API 31+ recommended to test `dataExtractionRules` backup |
| Git | 2.x | Branch: `001-day-counter-app` |

---

## Repository Setup

```bash
git clone <repo-url>
cd day-counter-app
git checkout 001-day-counter-app
```

Open the root `build.gradle.kts` (or the folder) in Android Studio.
Gradle sync runs automatically.

---

## Module Structure

```
:app          # Android application module — Hilt root, manifest, backup config
:domain       # Pure Kotlin — entities, repository interfaces, use cases
:data         # Room, DataStore, WorkManager, repository implementations, Hilt bindings
:presentation # Jetpack Compose UI, ViewModels, navigation, Glance widget, theme
```

Dependency graph:
```
:app → :presentation → :domain ← :data ← :app
```

`:domain` declares the `java-library` Gradle plugin; importing any `android.*` class in
`:domain` will fail the build.

---

## Build Commands

```bash
# Assemble debug APK
./gradlew assembleDebug

# Assemble release AAB (requires keystore — see below)
./gradlew bundleRelease

# Run all unit tests
./gradlew test

# Run instrumentation tests (requires connected device or emulator)
./gradlew connectedAndroidTest

# Run Kover coverage report (:domain gate ≥ 80%)
./gradlew koverHtmlReport

# Run Android Lint (zero errors required)
./gradlew lint

# Run Detekt
./gradlew detekt
```

---

## Running the App

1. Start an AVD (API 26+) or connect a physical device.
2. In Android Studio: **Run > Run 'app'**, or via CLI:
   ```bash
   ./gradlew installDebug
   adb shell am start -n com.daycounter/.MainActivity
   ```

### Testing the Widget

1. Run the app and create at least one counter.
2. Long-press the device home screen → "Widgets" → find "Day Counter".
3. Drag the widget onto the home screen. `CounterPickerActivity` opens automatically.
4. Select a counter. The widget appears with the current streak count.
5. To test the 4×2 medium size, resize the widget by long-pressing it.

### Testing Milestone Notifications

1. Create a counter with a `startDate` = today minus 6 days (streak = 6).
2. Run the daily worker manually via adb:
   ```bash
   adb shell am broadcast -a androidx.work.diagnostics.REQUEST_DIAGNOSTICS \
       -p com.daycounter
   # Or trigger directly:
   ./gradlew :data:test --tests "*DailyUpdateWorkerTest*"
   ```
3. Advance the device date by 1 day (streak becomes 7). A notification should fire.

### Testing Auto-Backup / Restore

Requires API 31+ emulator:
```bash
# Create some counters, then trigger a backup:
adb shell bmgr backupnow com.daycounter

# Uninstall and reinstall:
adb uninstall com.daycounter
./gradlew installDebug

# Trigger restore:
adb shell bmgr restore com.daycounter
```

All counters should reappear with correct start dates. WorkManager state should be clean
(no stale job IDs).

---

## Key Configuration Files

| File | Purpose |
|------|---------|
| `libs.versions.toml` | All dependency versions; never hardcode elsewhere |
| `app/src/main/AndroidManifest.xml` | All component declarations with `android:exported` |
| `app/src/main/res/xml/backup_rules.xml` | Auto-backup include/exclude (API 26–30) |
| `app/src/main/res/xml/data_extraction_rules.xml` | Auto-backup include/exclude (API 31+) |
| `app/src/main/res/xml/day_counter_widget_info.xml` | AppWidget provider metadata |
| `domain/build.gradle.kts` | `java-library` plugin — enforces zero Android imports |

---

## Adding a New Use Case

1. Define the interface in `:domain/usecase/` as a `class` with a single `suspend operator fun invoke(...)`.
2. Write a failing unit test in `:domain/src/test/`.
3. Implement the use case (Green step).
4. Provide it via Hilt `@Inject constructor` — no Hilt module entry required for use cases.
5. Inject it into the relevant ViewModel in `:presentation`.

---

## CI Checklist (mirrors constitution pre-merge checklist)

- [ ] `./gradlew lint` — zero errors
- [ ] `./gradlew detekt` — no new findings without suppression comment
- [ ] `./gradlew test` — all unit tests pass
- [ ] `./gradlew connectedAndroidTest` — all UI tests pass
- [ ] `./gradlew koverHtmlReport` — `:domain` line coverage ≥ 80%
- [ ] Manual TalkBack smoke test on any new screen
- [ ] Manual dark mode visual check on any new screen
- [ ] Security review: any new `Activity`/`Service`/`Receiver` has explicit `android:exported`
