package com.daycounter.domain.model

/**
 * An in-memory snapshot of all user data, captured by an erase-all so it can be restored within the
 * undo window (FR-031 / research R8).
 */
data class DataSnapshot(
    val counters: List<Counter>,
    val pausePeriods: List<PausePeriod>,
    val milestones: List<MilestoneRecord>,
    val pastStreaks: List<PastStreakRecord>,
) {
    val isEmpty: Boolean
        get() = counters.isEmpty() && pausePeriods.isEmpty() &&
            milestones.isEmpty() && pastStreaks.isEmpty()

    companion object {
        val EMPTY = DataSnapshot(emptyList(), emptyList(), emptyList(), emptyList())
    }
}
