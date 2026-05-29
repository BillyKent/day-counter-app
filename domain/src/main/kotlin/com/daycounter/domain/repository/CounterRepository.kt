package com.daycounter.domain.repository

import com.daycounter.domain.model.Counter
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
}
