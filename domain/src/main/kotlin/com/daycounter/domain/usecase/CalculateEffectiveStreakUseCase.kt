package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Computes a counter's **effective** streak day count, excluding paused time ("freeze & exclude").
 *
 * `effectiveDays = max(0, DAYS.between(startDate, anchor) − completedPausedDays)` where the anchor is
 * the counter's `pausedSince` while paused (the clock is frozen) or today while active. This is the
 * single source of truth shared by Contadores, Detalle, Estadísticas, History, and the Widget so the
 * tabs never disagree (SC-008). 0-based: same day = 0.
 *
 * Per constitution, timezone MUST be explicit at every call site that needs it.
 *
 * @see com.daycounter.domain.model.PausePeriod for how `completedPausedDays` is accumulated.
 */
class CalculateEffectiveStreakUseCase @Inject constructor(
    private val clock: Clock,
    private val zone: ZoneId,
) {
    /**
     * @param counter the counter whose streak is being measured.
     * @param completedPausedDays the sum of [com.daycounter.domain.model.PausePeriod.days] over the
     *   counter's completed pause intervals.
     */
    operator fun invoke(counter: Counter, completedPausedDays: Int): Int {
        val today = LocalDate.now(clock.withZone(zone))
        val anchor = if (counter.isPaused) (counter.pausedSince ?: today) else today
        val elapsed = ChronoUnit.DAYS.between(counter.startDate, anchor)
        return (elapsed - completedPausedDays).coerceAtLeast(0L).toInt()
    }
}
