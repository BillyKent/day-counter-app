package com.daycounter.domain.model

/**
 * Aggregate statistics across all counters (FR-006 / FR-025/FR-026).
 *
 * @property totalAccumulated Sum of every counter's current streak days.
 * @property bestStreak The single longest current streak (0 when there are no counters).
 * @property activeCounters Number of counters.
 */
data class StatsSummary(
    val totalAccumulated: Int,
    val bestStreak: Int,
    val activeCounters: Int,
) {
    companion object {
        val EMPTY = StatsSummary(totalAccumulated = 0, bestStreak = 0, activeCounters = 0)
    }
}
