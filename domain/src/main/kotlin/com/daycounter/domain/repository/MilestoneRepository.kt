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

    /** Returns all milestone records for the counter (drives the achieved list and dedup). */
    suspend fun getForCounter(counterId: Long): List<MilestoneRecord>

    /** Sets `celebrationShown = true` for every milestone row of the counter (FR-021). */
    suspend fun markAllShownForCounter(counterId: Long)
}
