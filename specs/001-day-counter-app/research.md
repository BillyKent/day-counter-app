# Research: Day Counter — Implementation Decisions

**Feature branch**: `001-day-counter-app` | **Date**: 2026-05-27

---

## Decision 1: Home-Screen Widget API — Jetpack Glance

**Decision**: Use `androidx.glance:glance-appwidget` (Jetpack Glance) for both widget sizes.

**Rationale**: Glance is the Compose-native AppWidget API endorsed by Google (stable since
2023). It wraps RemoteViews internally while exposing a Composable-like declarative API,
eliminating XML layout files for widgets. Compatible with minSdk 26. Directly aligns with
constitution Principle IV (Compose mandatory UI toolkit, no XML Views). `GlanceAppWidgetReceiver`
is the correct `AppWidgetProvider` subclass for the entry point.

Two concrete widget classes sharing a single `GlanceAppWidget` with layout branching:

| Size | Cells | `minWidth` | `minHeight` | Content |
|------|-------|-----------|------------|---------|
| Compact | 2×1 | 110 dp | 40 dp | Streak count + truncated goal name |
| Medium | 4×2 | 250 dp | 110 dp | Full goal name + streak count |

Widget state is modelled as a serializable `GlanceState` data class stored in
`GlanceStateDefinition` (DataStore-backed). State is refreshed by calling
`GlanceAppWidgetManager.updateIf<DayCounterWidget>` from `DailyUpdateWorker`.

**Alternatives considered**:
- **Plain RemoteViews**: No Compose integration; requires XML layouts and verbose
  `RemoteViews.setTextViewText` update code. Contradicts constitution's Compose-only UI
  mandate. Rejected.
- **Jetpack Compose for Wear OS Tiles**: Not applicable for App Widgets. Rejected.

---

## Decision 2: Day-Change Background Trigger — WorkManager PeriodicWorkRequest

**Decision**: `PeriodicWorkRequest` with a 1-day period and 15-minute flex window, scheduled
with an initial delay calculated to the next local midnight. A `BOOT_COMPLETED`
`BroadcastReceiver` re-enqueues the work uniquely (preventing duplicates) on device reboot.

**Rationale**:
- Constitution Principle IV prohibits bare `AlarmManager`; WorkManager is the mandated
  background scheduler.
- WorkManager natively re-enqueues overdue `PeriodicWorkRequest` after device reboot,
  satisfying the spec requirement that notifications arrive on next boot if the device
  was off at midnight (FR-010, SC-005).
- The ±15-minute flex window satisfies SC-004 ("within 1 hour of midnight") and SC-005
  ("within 1 hour of the milestone day").
- A single worker handles all counters atomically, keeping complexity O(1) with respect
  to counter count.

**`DailyUpdateWorker` execution order**:
1. Query all counters from Room.
2. For each counter: compute `streakDays = ChronoUnit.DAYS.between(startDate, LocalDate.now())`.
3. Check each milestone (7, 30, 60, 90, 180, 365) — if `streakDays == milestone` and no
   `MilestoneRecord` exists for `(counterId, milestone)`: post notification and insert record.
4. For each active `WidgetBinding`: call `GlanceAppWidgetManager.updateIf` to push fresh state.

**Alternatives considered**:
- **`AlarmManager.setExactAndAllowWhileIdle`**: More precise midnight delivery but
  explicitly prohibited by constitution Principle IV ("Bare AlarmManager" listed as
  prohibited alternative to WorkManager). Rejected.
- **Multiple `PeriodicWorkRequest` (one per counter)**: O(n) workers; complex cancellation
  on reset/delete; WorkManager has a practical limit on simultaneous enqueued work. Rejected.
- **`BroadcastReceiver` for `ACTION_TIME_TICK`**: Fires every minute; extreme battery drain;
  prohibited for production apps. Rejected.

---

## Decision 3: Milestone Notification Deduplication

**Decision**: `MilestoneRecord` table in Room with a `UNIQUE(counter_id, milestone_days)`
constraint. `DailyUpdateWorker` uses `INSERT OR IGNORE` semantics (`OnConflictStrategy.IGNORE`).
Before posting a notification, the worker attempts to insert; if insert succeeds (no prior
record), the notification fires; if insert is a no-op (duplicate), nothing fires.

When a counter is **reset**: all `MilestoneRecord` rows for that counter are explicitly deleted
by `ResetCounterUseCase` (not via `ON DELETE CASCADE`) so the same milestone can trigger a new
notification on the next streak.

When a counter is **deleted**: `ON DELETE CASCADE` on `MilestoneRecord.counter_id` removes all
associated records automatically, preventing orphaned milestone records.

**Rationale**: Idempotent insert-then-notify pattern prevents duplicates even under WorkManager
retries or manual clock adjustments. Satisfies SC-006 ("no duplicate milestone notification
ever sent for the same counter and milestone combination"). Reset/delete diverge intentionally:
reset wants future re-notification; deletion wants total cleanup.

**Alternatives considered**:
- **`DataStore` key per counter+milestone**: Non-relational; harder to query "all unsatisfied
  milestones for a counter"; doesn't cascade on counter deletion. Rejected.
- **`WorkManager OneTimeWorkRequest` per milestone per counter**: Combinatorial complexity;
  requires cancelling all outstanding requests on reset/delete with unique tag management.
  Rejected.

---

## Decision 4: Android Auto-Backup Configuration

**Decision**: Declare both `android:dataExtractionRules` (API 31+) and
`android:fullBackupContent` (API 26–30) in `AndroidManifest.xml`. Both XML rule files
explicitly **include** the Room database file and DataStore preferences files, and explicitly
**exclude** WorkManager's internal database (`androidx.work.workdb`).

Files to include:
```xml
<!-- data_extraction_rules.xml (API 31+) -->
<data-extraction-rules>
  <cloud-backup>
    <include domain="database" path="day_counter.db"/>
    <include domain="sharedpref" path="*.preferences_pb"/>
    <exclude domain="database" path="androidx.work.workdb"/>
  </cloud-backup>
</data-extraction-rules>
```

**Rationale**: WorkManager's internal database stores job IDs tied to the current install.
Restoring it after reinstall leaves WorkManager in an inconsistent state (old job IDs
reference non-existent workers). Explicitly excluding it and including only app data files
satisfies FR-015 / SC-008 (data survives reinstall/device transfer) without corrupting
WorkManager state.

**Alternatives considered**:
- **Default backup (no rules file)**: Includes WorkManager DB; WorkManager state corruption
  on restore is a known Android issue. Rejected.
- **`android:allowBackup="false"`**: Satisfies security but breaks FR-015. Rejected.

---

## Decision 5: Onboarding State and Notification Toggle — DataStore (Preferences)

**Decision**: `DataStore<Preferences>` with two keys:
- `onboarding_shown: Boolean` (in `OnboardingPreferencesDataStore`)
- `notifications_enabled: Boolean` (in `NotificationPreferencesDataStore`)

Both are non-sensitive user preferences; plain `DataStore` is appropriate per constitution
Principle VI ("plain DataStore is acceptable only for non-sensitive preferences").

`OnboardingViewModel` reads `onboarding_shown` on initialization. If false, the nav graph
starts at `onboarding`; on skip or final screen completion, the flag is written to `true`.
On subsequent launches, the graph starts at `home`.

`SettingsViewModel` reads and writes `notifications_enabled`. `DailyUpdateWorker` reads this
flag before posting any notification. Both OS-level `POST_NOTIFICATIONS` permission and
`notifications_enabled` must be true for a notification to fire (FR-012 + FR-017).

**Alternatives considered**:
- **Room boolean table**: Relational overhead for a single flag. Rejected.
- **`SharedPreferences`**: Prohibited by constitution Principle IV. Rejected.

---

## Decision 6: Notification Permission Flow

**Decision**: Request `POST_NOTIFICATIONS` permission (required on API 33+; implicit on
API 26–32) at the moment the user taps "Add Counter" for the first time — the "first
relevant interaction" as defined by FR-012. Implementation uses
`ActivityResultContracts.RequestPermission` from the `counter/create` screen.

Permission rationale dialog shown once before the system prompt. If the user denies:
- The denial is stored in `NotificationPreferencesDataStore`.
- The app continues to function without notifications (no crash, no error shown).
- No re-prompt on subsequent counter creations.

**Alternatives considered**:
- **Request on first app launch**: Requesting before the user understands the app
  significantly reduces grant rates (Android best-practice guidance). Rejected.
- **Request from Settings screen**: Too late — user may never visit settings. Rejected.

---

## Decision 7: Widget-Tap and Notification-Tap Deep Linking

**Decision**: Define Navigation Compose deep links using the URI scheme
`daycounter://counter/{counterId}`. Widget tap and notification tap each create a
`PendingIntent` targeting `MainActivity` with this URI as the intent data.

`MainActivity` starts the NavHost with `handleDeepLink = true`. The `counter/{counterId}`
destination receives the ID, calls `GetCounterByIdUseCase`, and navigates to the counter
detail view. If the counter no longer exists (deleted), the home screen is shown instead.

`PendingIntent` flags: `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE` (required on API 31+).

**Security**: The `counterId` URI parameter is validated against the Room database before
any navigation occurs — no implicit trust of external intent extras (constitution Principle VI,
deep-link input validation).

**Alternatives considered**:
- **Intent extras without deep links**: Cannot be handled declaratively by Navigation
  Compose; requires manual intent parsing in `onCreate`. More brittle. Rejected.
