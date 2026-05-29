package com.daycounter.domain.usecase

import java.time.LocalDate

/** The state of a single calendar day relative to the counter's streak (FR-028). */
enum class CalendarDayCategory { IN_STREAK, TODAY, PRE_STREAK, FUTURE }

/**
 * Pure history computations for the per-counter calendar and sparkline (Decision 5). Kept in
 * `:domain` so they are unit-testable without a device; the presentation layer maps the results to
 * Compose cell states and the [com.daycounter.presentation] sparkline.
 */
object HistoryComputations {

    /**
     * Categorises a [date] within the current month given the counter's [startDate] and [today]:
     * the streak runs `[startDate, today)`, [today] itself is distinct, days before the streak are
     * dimmed, and future days are neutral.
     */
    fun calendarDayCategory(date: LocalDate, startDate: LocalDate, today: LocalDate): CalendarDayCategory = when {
        date.isEqual(today) -> CalendarDayCategory.TODAY
        date.isAfter(today) -> CalendarDayCategory.FUTURE
        date.isBefore(startDate) -> CalendarDayCategory.PRE_STREAK
        else -> CalendarDayCategory.IN_STREAK
    }

    /**
     * Per-day streak growth values for the sparkline: a same-day counter (streak 0) yields a single
     * point `[0]`; a streak of N yields `[1, 2, …, N]` (N points).
     */
    fun sparklinePoints(streakDays: Int): List<Int> =
        if (streakDays <= 0) listOf(0) else (1..streakDays).toList()
}
