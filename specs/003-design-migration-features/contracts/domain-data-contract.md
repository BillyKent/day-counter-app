# Contract: Domain & Data

**Feature**: `003-design-migration-features` | **Date**: 2026-05-29

Signatures are the contract (TDD targets). All dates use `java.time` with injected `Clock`/`ZoneId`.

---

## Use cases (`:domain`, single `operator fun invoke`)

```kotlin
// Effective streak (paused-excluded). Pure; Clock/ZoneId injected.
class CalculateEffectiveStreakUseCase(clock, zone) {
  operator fun invoke(counter: Counter, completedPausedDays: Int): Int
  // = max(0, DAYS.between(startDate, status==PAUSED ? pausedSince : today) - completedPausedDays)
}

class PauseCounterUseCase(counterRepository) {
  suspend operator fun invoke(counterId: Long)        // no-op if already PAUSED
}

class ResumeCounterUseCase(counterRepository) {
  suspend operator fun invoke(counterId: Long)        // no-op if already ACTIVE
}

class GetPauseStatsUseCase(counterRepository, pausePeriodRepository) {
  operator fun invoke(): Flow<PauseStats>             // pausedNow, totalPausedDays, totalPauses
}

class GetStatsSummaryUseCase(...) {                    // EXTENDED
  operator fun invoke(): Flow<StatsSummary>           // totalEffectiveDays, bestStreak, activeCount,
                                                       // milestonesReached, averageStreak
}

class GetWeeklyActivityUseCase(counterRepository, clock, zone) {
  operator fun invoke(): Flow<WeeklyActivity>         // 7 day bars, weekTotal, todayIndex
}

class EraseAllDataUseCase(counterRepository) {
  suspend operator fun invoke(): DataSnapshot         // snapshot then delete-all (transactional)
}
class RestoreAllDataUseCase(counterRepository) {
  suspend operator fun invoke(snapshot: DataSnapshot) // re-insert (transactional)
}
```

Existing `ResetCounterUseCase` / `ArchiveAndResetCounterUseCase` extended to also clear pause state
(status→ACTIVE, pausedSince→null, drop pause periods) — see data-model §5.

## Repositories (`:domain` interfaces; impls in `:data`)

```kotlin
interface CounterRepository {                          // additions
  suspend fun pause(counterId: Long, today: LocalDate)
  suspend fun resume(counterId: Long, today: LocalDate)
  suspend fun eraseAll(): DataSnapshot
  suspend fun restore(snapshot: DataSnapshot)
  // existing: observeAll(), getById(), create(), update(), delete(), reset()…
}

interface PausePeriodRepository {                      // NEW
  suspend fun insert(period: PausePeriod)
  fun observeForCounter(counterId: Long): Flow<List<PausePeriod>>
  suspend fun completedPausedDays(counterId: Long): Int
  fun observeTotals(): Flow<PauseTotals>               // totalPausedDays, totalPauses
}

interface SettingsRepository {                          // NEW
  val language: Flow<AppLanguage>;        suspend fun setLanguage(l: AppLanguage)
  val appearance: Flow<AppearanceMode>;   suspend fun setAppearance(m: AppearanceMode)
  val dailyReminderEnabled: Flow<Boolean>; suspend fun setDailyReminderEnabled(b: Boolean)
  val reminderTime: Flow<ReminderTime>;    suspend fun setReminderTime(t: ReminderTime)
}
```

## DAO / transaction contracts (`:data`)

- `CounterDao.pauseCounter(id, today)` — `UPDATE counters SET status='PAUSED', paused_since=:today
  WHERE id=:id AND status='ACTIVE'`.
- `CounterDao.resumeCounter(id, today)` — `@Transaction`: insert `PausePeriod(paused_since, today)`
  then `UPDATE … SET status='ACTIVE', paused_since=NULL WHERE id=:id AND status='PAUSED'`.
- `CounterDao.eraseAll()` — `@Transaction`: delete all counters; assert CASCADE removes pause periods,
  milestone records, past streaks (else delete children first).
- `PausePeriodDao` — `insert`, `selectForCounter`, `countAll`, `sumDaysForCounter`/`sumDaysAll`.
- Atomicity tests: resume inserts exactly one period and flips status in one transaction; eraseAll
  leaves all tables empty; restore round-trips to byte-equal domain objects.
