package com.daycounter.domain.model

import java.time.LocalDate

/**
 * One day's bar in the Estadísticas "Esta semana" view (FR-028).
 *
 * @property date The calendar day.
 * @property fulfilled Count of counters that were ACTIVE and in-streak on [date].
 */
data class DayBar(val date: LocalDate, val fulfilled: Int)

/**
 * The last 7 days of "días cumplidos" activity (spec Assumptions → Weekly activity).
 *
 * @property days 7 entries, oldest first; the last is today.
 * @property weekTotal Sum of [DayBar.fulfilled] across the week.
 * @property todayIndex Index of today's bar (the emphasized one), i.e. `days.lastIndex`.
 */
data class WeeklyActivity(
    val days: List<DayBar>,
    val weekTotal: Int,
    val todayIndex: Int,
) {
    companion object {
        val EMPTY = WeeklyActivity(days = emptyList(), weekTotal = 0, todayIndex = -1)
    }
}
