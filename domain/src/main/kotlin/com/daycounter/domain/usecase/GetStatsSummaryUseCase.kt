package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import com.daycounter.domain.model.StatsSummary
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Computes the [StatsSummary] across counters in **effective** (paused-excluded) days so the result
 * agrees with the Contadores tab (SC-008): total accumulated, best streak, active count (excluding
 * paused), milestones reached, and average streak. Used by the Statistics tab (US5).
 *
 * @see CalculateEffectiveStreakUseCase
 */
class GetStatsSummaryUseCase @Inject constructor(
    private val calculateEffectiveStreak: CalculateEffectiveStreakUseCase,
) {
    /**
     * @param pausedDaysByCounter completed paused days per counter id (default empty when unknown).
     */
    operator fun invoke(
        counters: List<Counter>,
        pausedDaysByCounter: Map<Long, Int> = emptyMap(),
    ): StatsSummary {
        if (counters.isEmpty()) return StatsSummary.EMPTY
        val streaks = counters.map { calculateEffectiveStreak(it, pausedDaysByCounter[it.id] ?: 0) }
        val milestonesReached = streaks.sumOf { streak ->
            StatsSummary.MILESTONES.count { it <= streak }
        }
        return StatsSummary(
            totalAccumulated = streaks.sum(),
            bestStreak = streaks.max(),
            activeCounters = counters.count { !it.isPaused },
            milestonesReached = milestonesReached,
            averageStreak = (streaks.sum().toDouble() / counters.size).roundToInt(),
        )
    }
}
