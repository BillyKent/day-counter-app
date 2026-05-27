package com.daycounter.domain.model

import java.time.Instant

/**
 * Persistent record that a milestone notification was dispatched for a given counter.
 * Deduplicates notifications via a UNIQUE(counter_id, milestone_days) constraint at
 * the data layer.
 *
 * @property id Row identifier; 0 for a not-yet-persisted instance.
 * @property counterId Owning counter's id; FK with ON DELETE CASCADE.
 * @property milestoneDays The milestone reached; must be a value in [MILESTONE_DAYS].
 * @property notifiedAt When the notification was dispatched.
 */
data class MilestoneRecord(
    val id: Long = 0L,
    val counterId: Long,
    val milestoneDays: Int,
    val notifiedAt: Instant,
) {
    companion object {
        /** The six milestone day counts that trigger notifications. */
        val MILESTONE_DAYS: Set<Int> = setOf(7, 30, 60, 90, 180, 365)
    }
}
