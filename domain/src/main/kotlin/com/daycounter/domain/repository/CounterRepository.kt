package com.daycounter.domain.repository

import com.daycounter.domain.model.Counter
import com.daycounter.domain.model.DataSnapshot
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/**
 * Repository for [Counter] persistence operations. Implementations live in `:data`.
 *
 * Sort order: `startDate ASC, createdAt ASC` — oldest start date (longest streak)
 * first; createdAt as a stable tie-breaker.
 */
interface CounterRepository {
    /** Emits the full counter list sorted by longest streak first, with createdAt as tie-breaker. */
    fun getAllSortedByStreak(): Flow<List<Counter>>

    suspend fun getById(id: Long): Counter?

    /** Inserts a new counter and returns the generated row ID. */
    suspend fun insert(counter: Counter): Long

    suspend fun update(counter: Counter)

    suspend fun delete(counter: Counter)

    /**
     * Atomically archives the current streak (if [streakDaysAtReset] > 0), clears the counter's
     * milestone rows, and sets its start date to [today] (FR-017). All-or-nothing.
     *
     * @param now Wall-clock moment used as the archived record's `createdAt`.
     */
    suspend fun archiveAndReset(
        counterId: Long,
        streakDaysAtReset: Int,
        today: LocalDate,
        now: Instant,
    )

    /** Pauses an active counter (sets status=PAUSED, pausedSince=[today]). No-op if already paused. */
    suspend fun pause(counterId: Long, today: LocalDate)

    /**
     * Resumes a paused counter: banks the current pause as a [com.daycounter.domain.model.PausePeriod]
     * (pausedSince → [today]) and sets status=ACTIVE. No-op if already active. One transaction.
     */
    suspend fun resume(counterId: Long, today: LocalDate)

    /**
     * Erases all counters and their children (pause periods, milestones, past streaks) and returns a
     * [DataSnapshot] of what was removed so it can be restored within the undo window (FR-030/FR-031).
     */
    suspend fun eraseAll(): DataSnapshot

    /** Re-inserts a [DataSnapshot] captured by [eraseAll], restoring the prior state. */
    suspend fun restore(snapshot: DataSnapshot)
}
