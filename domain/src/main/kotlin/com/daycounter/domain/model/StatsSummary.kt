package com.daycounter.domain.model

/**
 * Aggregate statistics across all counters (FR-006 / FR-025/FR-026).
 *
 * Values are in effective (paused-excluded) days so they agree with the Contadores tab (SC-008).
 *
 * @property totalAccumulated Sum of every counter's effective streak days.
 * @property bestStreak The single longest effective streak (0 when there are no counters).
 * @property activeCounters Number of counters that are NOT paused.
 * @property milestonesReached Count, across all counters, of milestones from {1,7,30,100,365,1000}
 *   whose day value is ≤ that counter's effective streak (FR-026).
 * @property averageStreak Mean effective streak across all counters (rounded).
 */
data class StatsSummary(
    val totalAccumulated: Int,
    val bestStreak: Int,
    val activeCounters: Int,
    val milestonesReached: Int = 0,
    val averageStreak: Int = 0,
) {
    companion object {
        val EMPTY = StatsSummary(
            totalAccumulated = 0,
            bestStreak = 0,
            activeCounters = 0,
            milestonesReached = 0,
            averageStreak = 0,
        )

        /** Canonical milestone set used for "Hitos alcanzados" (FR-026). */
        val MILESTONES: List<Int> = listOf(1, 7, 30, 100, 365, 1000)
    }
}
