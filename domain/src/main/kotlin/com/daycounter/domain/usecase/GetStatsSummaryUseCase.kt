package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import com.daycounter.domain.model.StatsSummary
import javax.inject.Inject

/**
 * Computes the [StatsSummary] for a set of counters: total accumulated streak days, the best
 * (longest) current streak, and the number of active counters. Reused by Home (US2) and the
 * Statistics tab (US5). Streaks are computed with [CalculateStreakUseCase] so the result reflects
 * the current local date.
 */
class GetStatsSummaryUseCase @Inject constructor(
    private val calculateStreak: CalculateStreakUseCase,
) {
    operator fun invoke(counters: List<Counter>): StatsSummary {
        if (counters.isEmpty()) return StatsSummary.EMPTY
        val streaks = counters.map { calculateStreak(it.startDate) }
        return StatsSummary(
            totalAccumulated = streaks.sum(),
            bestStreak = streaks.max(),
            activeCounters = counters.size,
        )
    }
}
