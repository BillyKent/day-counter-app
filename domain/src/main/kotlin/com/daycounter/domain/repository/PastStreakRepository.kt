package com.daycounter.domain.repository

import com.daycounter.domain.model.PastStreakRecord

/**
 * Repository for [PastStreakRecord] persistence. Implementations live in `:data`.
 *
 * Records are created at reset time and are never deleted directly; they are removed via
 * the FK cascade when their parent counter is deleted.
 */
interface PastStreakRepository {
    /** Inserts an archived streak and returns the generated row ID. */
    suspend fun insert(record: PastStreakRecord): Long

    /**
     * Returns a page of archived streaks for the counter, ordered newest [PastStreakRecord.endDate]
     * first (`end_date DESC, id DESC`).
     *
     * @param limit Page size (the History screen uses batches of 50).
     * @param offset Number of rows to skip, advanced by "See more".
     */
    suspend fun getForCounterPaged(counterId: Long, limit: Int, offset: Int): List<PastStreakRecord>
}
