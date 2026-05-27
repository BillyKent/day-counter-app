package com.daycounter.domain.usecase

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Computes the streak day count for a counter from its [startDate] and the current local date.
 *
 * Streak is `ChronoUnit.DAYS.between(startDate, today)`. Same-day = 0, yesterday = 1.
 * Per constitution: timezone MUST be explicit at every call site that needs it.
 */
class CalculateStreakUseCase @Inject constructor(
    private val clock: Clock,
    private val zone: ZoneId,
) {
    operator fun invoke(startDate: LocalDate): Int {
        val today = LocalDate.now(clock.withZone(zone))
        val between = ChronoUnit.DAYS.between(startDate, today)
        return between.coerceAtLeast(0L).toInt()
    }
}
