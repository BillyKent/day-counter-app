package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import com.daycounter.domain.model.DayBar
import com.daycounter.domain.model.PausePeriod
import com.daycounter.domain.model.WeeklyActivity
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Builds the last-7-days "días cumplidos" view (FR-028 / spec Assumptions → Weekly activity).
 *
 * For each day (oldest first, ending today), counts counters that were ACTIVE and in-streak that day:
 * `startDate ≤ day`, the day is not inside any completed pause interval `[start, end)`, and — for a
 * currently-paused counter — `day < pausedSince`.
 */
class GetWeeklyActivityUseCase @Inject constructor(
    private val clock: Clock,
    private val zone: ZoneId,
) {
    operator fun invoke(counters: List<Counter>, completedPeriods: List<PausePeriod>): WeeklyActivity {
        if (counters.isEmpty()) return WeeklyActivity.EMPTY
        val today = LocalDate.now(clock.withZone(zone))
        val periodsByCounter = completedPeriods.groupBy { it.counterId }

        val days = (DAYS_BACK downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            val fulfilled = counters.count { counter -> counter.wasFulfilledOn(day, periodsByCounter[counter.id].orEmpty()) }
            DayBar(date = day, fulfilled = fulfilled)
        }
        return WeeklyActivity(
            days = days,
            weekTotal = days.sumOf { it.fulfilled },
            todayIndex = days.lastIndex,
        )
    }

    private fun Counter.wasFulfilledOn(day: LocalDate, periods: List<PausePeriod>): Boolean {
        if (day.isBefore(startDate)) return false
        // Inside a completed pause interval [start, end)?
        if (periods.any { !day.isBefore(it.startDate) && day.isBefore(it.endDate) }) return false
        // Currently paused: only days strictly before the pause began count.
        if (isPaused && pausedSince != null && !day.isBefore(pausedSince)) return false
        return true
    }

    private companion object {
        const val DAYS_BACK = 6 // today + 6 prior days = 7 bars
    }
}
