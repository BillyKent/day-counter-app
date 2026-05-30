package com.daycounter.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.daycounter.data.database.entity.CounterEntity
import com.daycounter.data.database.entity.MilestoneRecordEntity
import com.daycounter.data.database.entity.PastStreakRecordEntity
import com.daycounter.data.database.entity.PausePeriodEntity
import com.daycounter.domain.model.PastStreakRecord
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterDao {

    /**
     * Emits counters ordered by [CounterEntity.startDate] ASC (oldest start date =
     * longest streak), with [CounterEntity.createdAt] ASC as the stable tie-breaker.
     */
    @Query("SELECT * FROM counters ORDER BY start_date ASC, created_at ASC")
    fun getAllCountersSortedByStreak(): Flow<List<CounterEntity>>

    @Query("SELECT * FROM counters WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CounterEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(counter: CounterEntity): Long

    @Update
    suspend fun update(counter: CounterEntity)

    @Delete
    suspend fun delete(counter: CounterEntity)

    // ---- Reset transaction primitives (FR-017) ----

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPastStreak(record: PastStreakRecordEntity): Long

    @Query("DELETE FROM milestone_records WHERE counter_id = :counterId")
    suspend fun deleteMilestonesForCounter(counterId: Long)

    @Query("UPDATE counters SET start_date = :today WHERE id = :counterId")
    suspend fun updateStartDate(counterId: Long, today: LocalDate)

    /**
     * Atomically archives the current streak and resets the counter (FR-017):
     * 1. if [streakDaysAtReset] > 0, insert a [PastStreakRecord] (reason "Reiniciado");
     * 2. delete every milestone row for the counter (so milestones can re-fire);
     * 3. set the counter's `start_date` to [today].
     *
     * All steps run in one transaction — all-or-nothing.
     */
    @Transaction
    suspend fun archiveAndReset(
        counterId: Long,
        streakDaysAtReset: Int,
        today: LocalDate,
        now: Instant,
    ) {
        if (streakDaysAtReset > 0) {
            insertPastStreak(
                PastStreakRecordEntity(
                    counterId = counterId,
                    streakDays = streakDaysAtReset,
                    reason = PastStreakRecord.REASON_RESET,
                    endDate = today,
                    createdAt = now,
                ),
            )
        }
        deleteMilestonesForCounter(counterId)
        deletePausePeriodsForCounter(counterId)
        updateStartDate(counterId, today)
        setActive(counterId)
    }

    // ---- Pause / resume primitives (FR-007..FR-013) ----

    @Query("SELECT paused_since FROM counters WHERE id = :counterId")
    suspend fun pausedSince(counterId: Long): LocalDate?

    @Query("UPDATE counters SET status = 'PAUSED', paused_since = :today WHERE id = :counterId AND status = 'ACTIVE'")
    suspend fun pause(counterId: Long, today: LocalDate)

    @Query("UPDATE counters SET status = 'ACTIVE', paused_since = NULL WHERE id = :counterId")
    suspend fun setActive(counterId: Long)

    @Query("INSERT INTO pause_periods (counter_id, start_date, end_date) VALUES (:counterId, :start, :end)")
    suspend fun insertPausePeriod(counterId: Long, start: LocalDate, end: LocalDate)

    @Query("DELETE FROM pause_periods WHERE counter_id = :counterId")
    suspend fun deletePausePeriodsForCounter(counterId: Long)

    /**
     * Atomically resumes a paused counter: banks the open pause interval (pausedSince → [today]) and
     * flips the counter to ACTIVE. No-op if the counter is not currently paused.
     */
    @Transaction
    suspend fun resume(counterId: Long, today: LocalDate) {
        val since = pausedSince(counterId) ?: return
        insertPausePeriod(counterId, since, today)
        setActive(counterId)
    }

    // ---- Erase-all + restore (FR-030/FR-031) ----

    @Query("SELECT * FROM counters")
    suspend fun selectAllCounters(): List<CounterEntity>

    @Query("SELECT * FROM pause_periods")
    suspend fun selectAllPausePeriods(): List<PausePeriodEntity>

    @Query("SELECT * FROM milestone_records")
    suspend fun selectAllMilestones(): List<MilestoneRecordEntity>

    @Query("SELECT * FROM past_streak_records")
    suspend fun selectAllPastStreaks(): List<PastStreakRecordEntity>

    /** Deleting all counters cascades to pause periods, milestones, and past streaks (FK CASCADE). */
    @Query("DELETE FROM counters")
    suspend fun deleteAllCounters()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCounters(counters: List<CounterEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPausePeriods(periods: List<PausePeriodEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMilestones(records: List<MilestoneRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPastStreaks(records: List<PastStreakRecordEntity>)

    /** Re-inserts a snapshot in FK order (counters first), all-or-nothing. */
    @Transaction
    suspend fun restoreAll(
        counters: List<CounterEntity>,
        pausePeriods: List<PausePeriodEntity>,
        milestones: List<MilestoneRecordEntity>,
        pastStreaks: List<PastStreakRecordEntity>,
    ) {
        insertCounters(counters)
        insertPausePeriods(pausePeriods)
        insertMilestones(milestones)
        insertPastStreaks(pastStreaks)
    }
}
