# Phase 1 — Data Model: Screens and Navigation Overhaul

**Feature**: `002-screens-and-navigation` | **Date**: 2026-05-28

Domain models live in `:domain` (pure Kotlin); Room entities mirror them in `:data`. This
iteration **extends** two existing entities, **adds** one, and **redefines** the milestone set.
Per the spec's fresh-install assumption, the Room version is bumped with a destructive fallback
(no written migration).

---

## Entity: Counter (extended)

Domain `Counter` (`:domain/model/Counter.kt`), table `counters`.

| Field | Type | Rules | Status |
|-------|------|-------|--------|
| `id` | `Long` | PK, autogenerate; 0 = unsaved | existing |
| `goalName` | `String` | 1–100 chars, required | existing |
| `startDate` | `LocalDate` | ≤ today; editable only on Create | existing |
| `createdAt` | `Instant` | immutable after insert | existing |
| `category` | `String?` | optional free text, 0–50 chars | **NEW** (FR-014) |
| `goalMilestoneTarget` | `Int` | one of `{7, 30, 100, 365}`; required; **default 30** | **NEW** (FR-014) |

**Column mapping**: `category` → `category TEXT` (nullable); `goalMilestoneTarget` →
`goal_milestone_target INTEGER NOT NULL DEFAULT 30`.

**Validation** (enforced in Create/Edit ViewModels and `CreateCounterUseCase`/`UpdateCounterUseCase`):
- `goalName` length 1–100.
- `category` length 0–50 (null/blank allowed).
- `goalMilestoneTarget ∈ {7, 30, 100, 365}`.
- `startDate ≤ today` (Create only; read-only on Edit).

**Derived (not stored)**:
- `streakDays = ChronoUnit.DAYS.between(startDate, today)` (existing `CalculateStreakUseCase`).
- `ringFillRatio = min(1.0, streakDays / goalMilestoneTarget)` (UI).
- `goalReached = streakDays ≥ goalMilestoneTarget` → "Hito alcanzado" badge (FR-008).

---

## Entity: MilestoneRecord (extended + redefined set)

Domain `MilestoneRecord` (`:domain/model/MilestoneRecord.kt`), table `milestone_records`.

| Field | Type | Rules | Status |
|-------|------|-------|--------|
| `id` | `Long` | PK, autogenerate | existing |
| `counterId` | `Long` | FK → counters.id, ON DELETE CASCADE | existing |
| `milestoneDays` | `Int` | one of `MILESTONE_DAYS` | existing (set redefined) |
| `notifiedAt` | `Instant` | when recorded/notified | existing |
| `celebrationShown` | `Boolean` | default `false`; true after first auto-launch | **NEW** (FR-021) |

- **`MILESTONE_DAYS` redefined**: `{7, 30, 60, 90, 180, 365}` → **`{1, 7, 30, 100, 365, 1000}`** (FR-019).
- **Column mapping**: `celebrationShown` → `celebration_shown INTEGER NOT NULL DEFAULT 0`.
- **Uniqueness**: existing `UNIQUE(counter_id, milestone_days)` index retained (dedup within attempt).
- **Lifecycle**: rows are **deleted in the Reset transaction** (FR-017) and **cascade-deleted**
  when the parent Counter is deleted (FR-012). Therefore all rows always describe the *current*
  attempt — the achieved-milestones list (FR-022) is just "all rows for this counter".

**New repository methods** (`MilestoneRepository`):
- `suspend fun getForCounter(counterId: Long): List<MilestoneRecord>` — drives achieved list + dedup.
- `suspend fun markAllShownForCounter(counterId: Long)` — sets `celebration_shown = true` for all rows.

---

## Entity: PastStreakRecord (NEW)

Domain `PastStreakRecord` (`:domain/model/PastStreakRecord.kt`), table `past_streak_records`.
A snapshot of a streak archived at reset time (FR-017, US7).

| Field | Type | Rules |
|-------|------|-------|
| `id` | `Long` | PK, autogenerate; 0 = unsaved |
| `counterId` | `Long` | FK → counters.id, ON DELETE CASCADE |
| `streakDays` | `Int` | streak length at reset; `> 0` (0-day resets create no record) |
| `reason` | `String` | initially only `"Reiniciado"` |
| `endDate` | `LocalDate` | day the streak ended (today at reset) |
| `createdAt` | `Instant` | wall-clock moment archived |

**Indexes**: FK index on `counter_id`; composite index `(counter_id, end_date)` to serve the
"most recent first by `end_date`" paginated query (FR-029).

**Retention**: none — kept indefinitely; pagination is UI-only (Clarification Q3).

**New repository** (`PastStreakRepository`):
- `suspend fun insert(record: PastStreakRecord): Long`
- `suspend fun getForCounterPaged(counterId: Long, limit: Int, offset: Int): List<PastStreakRecord>`
  — ordered `end_date DESC, id DESC`; `limit = 50` per batch, `offset` advanced by "Ver más".
- (deletion handled by FK cascade when the counter is deleted.)

---

## Reset transaction (FR-017) — atomic

Implemented as a single Room `@Transaction` invoked by `ArchiveAndResetCounterUseCase`:

```
@Transaction archiveAndReset(counterId, streakDaysAtReset, today, now):
  if streakDaysAtReset > 0:
      insert PastStreakRecord(counterId, streakDaysAtReset, "Reiniciado", endDate=today, createdAt=now)
  delete all MilestoneRecord where counter_id = counterId
  update counters set start_date = today where id = counterId
```

- All-or-nothing (FR-017). `streakDaysAtReset` is computed by `CalculateStreakUseCase` before
  the transaction (domain stays Android-free; the transaction just persists).
- `streakDaysAtReset == 0` → no `PastStreakRecord` (edge case "Reiniciar with a 0-day streak").

---

## Entity: WidgetBinding (unchanged)

No change. The widget continues to render existing data; `category` and
`goalMilestoneTarget` are intentionally **not** surfaced in the widget (spec Assumptions).

---

## Schema version & migration

- Bump `AppDatabase.version`.
- `fallbackToDestructiveMigration(true)` — fresh-install assumption (Clarification Q1;
  research Decision 6; plan Complexity Tracking). No written migration, no backfill.
- New table `past_streak_records`; new columns `counters.category`,
  `counters.goal_milestone_target`, `milestone_records.celebration_shown`.

---

## State transitions

**Counter streak lifecycle**:
```
Create(startDate ≤ today) → streak grows daily (derived) → [Reset] → archive PastStreakRecord
   → clear MilestoneRecords → startDate=today (streak=0) → grows again
[Delete] → cascade: MilestoneRecords + PastStreakRecords removed → return to Contadores
```

**Milestone/celebration lifecycle (per attempt)**:
```
streak first reaches N ∈ {1,7,30,100,365,1000}  → insert MilestoneRecord (celebration_shown=false)
   + dispatch notification if toggle on (FR-020)
open Detail, most-recent unseen milestone exists → auto-launch Celebration
   → markAllShownForCounter (celebration_shown=true for all rows) (FR-021)
"Revivir celebración" → open Celebration for highest milestone ≤ current streak (FR-024)
[Reset] → all MilestoneRecords deleted → milestones can be re-achieved & re-celebrated
```
