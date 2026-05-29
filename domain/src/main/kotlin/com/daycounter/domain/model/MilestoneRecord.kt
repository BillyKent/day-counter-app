package com.daycounter.domain.model

import java.time.Instant

/**
 * Persistent record that a milestone was reached for a given counter.
 * Deduplicates within an attempt via a UNIQUE(counter_id, milestone_days) constraint at
 * the data layer. All rows for a counter are deleted in the reset transaction, so the rows
 * always describe the *current* attempt.
 *
 * @property id Row identifier; 0 for a not-yet-persisted instance.
 * @property counterId Owning counter's id; FK with ON DELETE CASCADE.
 * @property milestoneDays The milestone reached; must be a value in [MILESTONE_DAYS].
 * @property notifiedAt When the milestone was recorded / notified.
 * @property celebrationShown Whether the full-screen celebration has been auto-launched for
 *   this counter's milestones. Set to true (for all rows) after the first auto-launch (FR-021).
 */
data class MilestoneRecord(
    val id: Long = 0L,
    val counterId: Long,
    val milestoneDays: Int,
    val notifiedAt: Instant,
    val celebrationShown: Boolean = false,
) {
    companion object {
        /** The six milestone day counts that trigger a celebration / notification (FR-019). */
        val MILESTONE_DAYS: Set<Int> = setOf(1, 7, 30, 100, 365, 1000)
    }
}
