# Phase 1 Data Model: Design Migration + New Features

**Feature**: `003-design-migration-features` | **Date**: 2026-05-29

Layers per constitution: pure-Kotlin domain models in `:domain`; Room entities + mappers in `:data`;
non-sensitive preferences in DataStore. Room schema **v2 → v3**, `fallbackToDestructiveMigration`
(fresh-install assumption).

---

## 1. Domain models (`:domain`)

### 1.1 `Counter` (extended)

| Field | Type | Rules |
|-------|------|-------|
| id | `Long` | 0 = not persisted |
| goalName | `String` | 1–100 chars (unchanged) |
| startDate | `LocalDate` | not in future; editable only on Create (unchanged) |
| createdAt | `Instant` | immutable (unchanged) |
| category | `String?` | **now a key from `CATEGORIES`** (0–50 chars legacy tolerated) |
| goalMilestoneTarget | `Int` | one of `GOAL_TARGETS {7,30,100,365}`, default 30 (unchanged) |
| **status** | `CounterStatus` | NEW — `ACTIVE` (default) or `PAUSED` |
| **pausedSince** | `LocalDate?` | NEW — non-null **iff** `status == PAUSED`; the day the current pause began |

Companion: `GOAL_TARGETS`, `DEFAULT_GOAL_TARGET` (unchanged) + **`CATEGORIES: List<CategoryKey>`**
(stable keys; labels resolved from string resources).

**Invariants**: `status==PAUSED ⇔ pausedSince!=null`. `pausedSince in [startDate, today]`.

### 1.2 `CounterStatus` (NEW enum)
`ACTIVE`, `PAUSED`.

### 1.3 `PausePeriod` (NEW)

| Field | Type | Rules |
|-------|------|-------|
| id | `Long` | 0 = not persisted |
| counterId | `Long` | FK → Counter |
| startDate | `LocalDate` | pause start (inclusive) |
| endDate | `LocalDate` | resume day; `endDate >= startDate` |

Derived: `days = ChronoUnit.DAYS.between(startDate, endDate)` (0 allowed for same-day pause/resume).

### 1.4 `AppLanguage` (NEW)
`EN`, `ES`. `default = EN`. Maps to locale tag `en` / `es`.

### 1.5 `AppearanceMode` (NEW)
`SYSTEM` (default), `LIGHT`, `DARK`.

### 1.6 `ReminderTime` (NEW value object)
`hour: Int (0–23)`, `minute: Int (0,5,…,55)`. Serialized as `HH:mm`. Default `09:00`.
Presets: Mañana `08:00`, Mediodía `13:00`, Noche `21:00`.

### 1.7 Derived stats models
- `StatsSummary` (extended): `totalEffectiveDays`, `bestStreak`, `activeCount` (excludes paused),
  `milestonesReached` (Σ over counters of `|{ m ∈ {1,7,30,100,365,1000} : m ≤ effectiveDays }|`),
  `averageStreak` (totalEffective / counterCount).
- `PauseStats` (NEW): `pausedNow: Int`, `totalPausedDays: Int`, `totalPauses: Int`.
- `WeeklyActivity` (NEW): `days: List<DayBar>` (7 entries, `label` + `fulfilled: Int`), `weekTotal`,
  `todayIndex`. `fulfilled` per day = count of counters ACTIVE and in-streak that day (`startDate ≤
  day`, day not within any paused interval, and `day < pausedSince` if currently paused) — per spec
  Assumptions "Weekly activity".
- `DataSnapshot` (NEW, for undo): immutable copy of all counters, pause periods, milestone records,
  past streaks.

---

## 2. Effective-streak computation

Single source of truth used by Contadores, Detalle, Estadísticas, History, and Widget (SC-008):

```
fun effectiveDays(counter, completedPausedDays, today): Int {
    val anchor = if (counter.status == PAUSED) counter.pausedSince!! else today
    return max(0, DAYS.between(counter.startDate, anchor) - completedPausedDays)
}
```

`completedPausedDays = Σ PausePeriod.days` for the counter. Owned by
`CalculateEffectiveStreakUseCase` (pure; `Clock`/`ZoneId` injected, per constitution date rule).
Milestones evaluate against `effectiveDays` (FR-008..FR-011).

---

## 3. Room entities & schema (`:data`)

### 3.1 `CounterEntity` (+2 columns)
Add `@ColumnInfo(name="status", defaultValue="ACTIVE") status: String` and
`@ColumnInfo(name="paused_since") pausedSince: LocalDate?`. Mappers updated (`toDomain`/`toEntity`),
status stored as enum name via existing `Converters` (add `CounterStatus` converter).

### 3.2 `PausePeriodEntity` (NEW)
```
@Entity(
  tableName = "pause_periods",
  foreignKeys = [ForeignKey(CounterEntity, ["id"], ["counter_id"], onDelete = CASCADE)],
  indices = [Index("counter_id")]
)
id (PK autoGen), counter_id: Long, start_date: LocalDate, end_date: LocalDate
```

### 3.3 `AppDatabase`
`version = 3`; add `PausePeriodEntity` to `entities`, `pausePeriodDao()`; keep
`fallbackToDestructiveMigration(true)`; `exportSchema = true` (new schema JSON committed).

### 3.4 DAOs
- `PausePeriodDao` (NEW): `insert(p)`, `selectForCounter(counterId): List`, `countAll(): Int`,
  `sumDaysAll(): Int` (or compute in Kotlin), `sumDaysForCounter(counterId): Int`.
- `CounterDao` (extended):
  - `@Transaction pauseCounter(id, today)` → set `status=PAUSED, paused_since=today` where `status=ACTIVE`.
  - `@Transaction resumeCounter(id, today)` → insert `PausePeriod(paused_since, today)`; set
    `status=ACTIVE, paused_since=NULL` where `status=PAUSED`.
  - `@Transaction eraseAll()` → delete counters (CASCADE clears pause periods, milestones, past
    streaks via existing FKs) — verify all child FKs are CASCADE; otherwise delete explicitly in order.
  - snapshot reads: `selectAllCounters()`, plus child selects for `DataSnapshot`.

---

## 4. Preferences (DataStore) — `SettingsPreferencesDataStore` (NEW)

Store `settings_prefs`:

| Key | Type | Default |
|-----|------|---------|
| `language` | String (`en`/`es`) | `en` |
| `appearance` | String (`system`/`light`/`dark`) | `system` |
| `daily_reminder_enabled` | Boolean | `false` |
| `daily_reminder_time` | String `HH:mm` | `09:00` |

Exposed as `Flow`s + suspend setters via `SettingsRepository` (`:domain` interface,
`SettingsRepositoryImpl` in `:data`). Milestone-notifications toggle stays in the existing
`NotificationPreferencesDataStore`.

---

## 5. State transitions

**Counter pause lifecycle**
```
ACTIVE --pause--> PAUSED         (set pausedSince = today; count frozen at effectiveDays)
PAUSED --resume--> ACTIVE        (insert PausePeriod(pausedSince, today); clear pausedSince)
```
- Pause is idempotent (pausing a paused counter is a no-op). Resume is idempotent likewise.
- Pause/resume never touch `MilestoneRecord` or `PastStreakRecord` (FR-012/FR-013).
- `Reset` (existing) is unchanged and remains distinct: it archives a `PastStreakRecord` and resets
  startDate; on reset, **pause state clears** (status→ACTIVE, pausedSince→null, and the counter's
  pause periods are removed since the streak restarts).

**Erase/restore**
```
populated --eraseAll--> empty (+ snapshot held in memory)
empty --restore(snapshot) within window--> populated
empty --window elapses--> permanent (snapshot dropped)
```

---

## 6. Validation rules

- Pause requires `status==ACTIVE`; resume requires `status==PAUSED` (UI guards + DAO WHERE clauses).
- `ReminderTime.minute ∈ {0,5,…,55}`; `hour ∈ 0..23`.
- `AppLanguage` restricted to `{EN, ES}` (Q1); unknown persisted value → `EN`.
- Category selection limited to `Counter.CATEGORIES`; legacy free-text tolerated on read only.
- All date math via `java.time` with injected `ZoneId` (no implicit timezone).

---

## 7. Test data focus

- Effective-days: same-day (0), one completed pause (handoff example 1 Jan→pause 10→resume 20 = 9),
  currently-paused freeze across a date rollover, multiple pauses, pause on day 0.
- Stats: paused excluded from `activeCount` but counted in `totalEffectiveDays`; `milestonesReached`
  and `averageStreak` with mixed active/paused.
- Erase/restore round-trip equality (counters + children).
- Reminder: next-occurrence delay calc across midnight; toggle off cancels.
