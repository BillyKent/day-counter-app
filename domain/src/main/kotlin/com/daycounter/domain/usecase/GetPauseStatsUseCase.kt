package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import com.daycounter.domain.model.PausePeriod
import com.daycounter.domain.model.PauseStats
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Aggregates pause metrics for the Estadísticas "Pausas" card (FR-027): counters paused now, total
 * paused days (completed intervals + ongoing pauses), and total pauses.
 */
class GetPauseStatsUseCase @Inject constructor(
    private val clock: Clock,
    private val zone: ZoneId,
) {
    operator fun invoke(counters: List<Counter>, completedPeriods: List<PausePeriod>): PauseStats {
        val today = LocalDate.now(clock.withZone(zone))
        val pausedCounters = counters.filter { it.isPaused }
        val completedDays = completedPeriods.sumOf { it.days }
        val ongoingDays = pausedCounters.sumOf { c ->
            c.pausedSince?.let { ChronoUnit.DAYS.between(it, today).coerceAtLeast(0L).toInt() } ?: 0
        }
        return PauseStats(
            pausedNow = pausedCounters.size,
            totalPausedDays = completedDays + ongoingDays,
            totalPauses = completedPeriods.size + pausedCounters.size,
        )
    }
}
