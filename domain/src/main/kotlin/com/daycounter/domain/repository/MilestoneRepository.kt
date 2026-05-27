package com.daycounter.domain.repository

import com.daycounter.domain.model.MilestoneRecord

/**
 * Repository for [MilestoneRecord] persistence. Implementations live in `:data`.
 */
interface MilestoneRepository {
    /**
     * Attempts to insert a milestone record. Uses INSERT OR IGNORE on the
     * UNIQUE(counter_id, milestone_days) index.
     *
     * @return The new row ID, or `-1L` if a record already existed (duplicate).
     */
    suspend fun insertOrIgnore(record: MilestoneRecord): Long

    /** Deletes all milestone records for the given counter (used during reset). */
    suspend fun deleteAllForCounter(counterId: Long)
}
