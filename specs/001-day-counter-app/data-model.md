# Data Model: Day Counter

**Feature branch**: `001-day-counter-app` | **Date**: 2026-05-27

---

## Entity Overview

```
Counter  1 ──── * MilestoneRecord
Counter  1 ──── * WidgetBinding
```

Three persistent entities live in Room. One Glance widget state record exists per widget
instance in the Glance-managed DataStore (not Room).

---

## Counter

**Domain class**: `com.daycounter.domain.model.Counter`  
**Room entity**: `com.daycounter.data.database.entity.CounterEntity`  
**Table name**: `counters`

| Field | Kotlin Type | Room Column | Constraints |
|-------|-------------|-------------|-------------|
| `id` | `Long` | `id INTEGER` | PK, auto-generate |
| `goalName` | `String` | `goal_name TEXT` | NOT NULL; 1–100 chars |
| `startDate` | `LocalDate` | `start_date TEXT` | NOT NULL; ISO-8601 (YYYY-MM-DD); not in future |
| `createdAt` | `Instant` | `created_at INTEGER` | NOT NULL; epoch millis; set at creation, never updated |

**Derived (never stored)**:
- `streakDays: Int` — `ChronoUnit.DAYS.between(startDate, LocalDate.now(deviceZone))`
  computed by `CalculateStreakUseCase` on every read.

**Validation rules** (enforced in `CreateCounterUseCase` and `UpdateCounterUseCase`):
- `goalName.isNotBlank()` — error: "Goal name is required."
- `goalName.length <= 100` — error: "Goal name must be 100 characters or fewer."
- `startDate <= LocalDate.now()` — error: "Start date cannot be in the future."
- If `startDate` is null at creation time, default to `LocalDate.now()` (FR-001).

**Room type converters** (in `AppDatabase`):
- `LocalDate` ↔ `String` (ISO-8601)
- `Instant` ↔ `Long` (epoch millis)

**State transitions**:

```
[created] ──edit──▶ [updated name/date]
         ──reset──▶ startDate = today, all MilestoneRecords deleted
         ──delete─▶ [removed] (cascades to MilestoneRecord, WidgetBinding via ON DELETE CASCADE)
```

**List ordering** (FR-016): sorted by `streakDays DESC`, then `createdAt ASC` for tie-breaking.
`CounterDao.getAllCountersSortedByStreak()` returns a `Flow<List<CounterEntity>>` ordered
`start_date ASC` (oldest date = longest streak); secondary sort is `created_at ASC`.

---

## MilestoneRecord

**Domain class**: `com.daycounter.domain.model.MilestoneRecord`  
**Room entity**: `com.daycounter.data.database.entity.MilestoneRecordEntity`  
**Table name**: `milestone_records`

| Field | Kotlin Type | Room Column | Constraints |
|-------|-------------|-------------|-------------|
| `id` | `Long` | `id INTEGER` | PK, auto-generate |
| `counterId` | `Long` | `counter_id INTEGER` | NOT NULL; FK → `counters.id` ON DELETE CASCADE |
| `milestoneDays` | `Int` | `milestone_days INTEGER` | NOT NULL; must be one of {7, 30, 60, 90, 180, 365} |
| `notifiedAt` | `Instant` | `notified_at INTEGER` | NOT NULL; epoch millis; timestamp of notification dispatch |

**Unique constraint**: `UNIQUE(counter_id, milestone_days)` — prevents duplicate notifications.

**Write semantics**: `INSERT OR IGNORE` (`OnConflictStrategy.IGNORE`). A successful insert
means the notification fires; a no-op insert means it was already sent.

**Reset behavior**: `ResetCounterUseCase` explicitly calls
`MilestoneRepository.deleteAllForCounter(counterId)` before updating the counter's
`startDate`. This allows the same milestone to trigger a notification again on the new streak.

**Cascade behavior**: When the parent `Counter` is deleted, all associated `MilestoneRecord`
rows are deleted automatically via `ON DELETE CASCADE` on the foreign key.

**Valid milestone values**: `setOf(7, 30, 60, 90, 180, 365)` — defined as a constant in
`com.daycounter.domain.model.MilestoneRecord.MILESTONE_DAYS`.

---

## WidgetBinding

**Domain class**: `com.daycounter.domain.model.WidgetBinding`  
**Room entity**: `com.daycounter.data.database.entity.WidgetBindingEntity`  
**Table name**: `widget_bindings`

| Field | Kotlin Type | Room Column | Constraints |
|-------|-------------|-------------|-------------|
| `widgetId` | `Int` | `widget_id INTEGER` | PK; system-assigned App Widget ID |
| `counterId` | `Long` | `counter_id INTEGER` | NOT NULL; FK → `counters.id` ON DELETE SET NULL |

**`ON DELETE SET NULL`** on `counter_id`: when the linked counter is deleted, the binding
row is retained with `counter_id = NULL`. The widget then reads this null state and displays
a "Counter removed — tap to select a new one" placeholder (FR Req 6.6).

**Write lifecycle**:
- Inserted when the user completes `CounterPickerActivity` after placing a new widget.
- Updated when the user selects a different counter via the widget's reconfigure action.
- Deleted when the user removes the widget from the home screen (called from
  `DayCounterWidgetReceiver.onDeleted`).

---

## Glance Widget State (not Room)

Stored in Glance's internal DataStore (managed by `GlanceStateDefinition`). One record per
widget instance, keyed by Glance's `GlanceId`.

| Field | Kotlin Type | Description |
|-------|-------------|-------------|
| `counterId` | `Long?` | Bound counter ID; null if counter was deleted |
| `goalName` | `String` | Snapshot of goal name at last refresh |
| `streakDays` | `Int` | Streak count at last refresh |
| `isCounterDeleted` | `Boolean` | True if counter no longer exists in Room |

Updated by `DailyUpdateWorker` and immediately after any counter CRUD operation that
affects a bound counter.

---

## DataStore Keys

### `OnboardingPreferencesDataStore`

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `onboarding_shown` | `Boolean` | `false` | Set to `true` after onboarding is completed or skipped |

### `NotificationPreferencesDataStore`

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `notifications_enabled` | `Boolean` | `true` | In-app toggle (FR-017); `DailyUpdateWorker` reads this before posting any notification |
| `notification_permission_requested` | `Boolean` | `false` | Prevents re-prompting after first permission request |

---

## Room Database

**Class**: `com.daycounter.data.database.AppDatabase`  
**Version**: 1  
**Entities**: `CounterEntity`, `MilestoneRecordEntity`, `WidgetBindingEntity`  
**Export schema**: `true` (schema JSON committed to `data/schemas/`)

**Auto-backup**: Room DB file (`day_counter.db`) is explicitly included in both
`backup_rules.xml` (API 26–30) and `data_extraction_rules.xml` (API 31+). WorkManager's
internal DB (`androidx.work.workdb`) is explicitly excluded.

---

## ER Diagram (text)

```
counters
  id (PK)
  goal_name
  start_date
  created_at
     │
     ├─── 1:N ──▶ milestone_records
     │               id (PK)
     │               counter_id (FK → counters.id ON DELETE CASCADE)
     │               milestone_days
     │               notified_at
     │               UNIQUE(counter_id, milestone_days)
     │
     └─── 1:N ──▶ widget_bindings
                     widget_id (PK)
                     counter_id (FK → counters.id ON DELETE SET NULL)
```
