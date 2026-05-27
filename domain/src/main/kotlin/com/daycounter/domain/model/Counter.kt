package com.daycounter.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * A user-defined streak counter for a personal goal.
 *
 * @property id Row identifier; 0 for a not-yet-persisted instance.
 * @property goalName Human-readable goal name; 1-100 characters.
 * @property startDate Day the streak begins; must not be in the future.
 * @property createdAt Wall-clock moment the counter was created; immutable after insert.
 *
 * The streak day count is derived (never stored). It is computed by
 * `CalculateStreakUseCase` as `ChronoUnit.DAYS.between(startDate, today)`.
 */
data class Counter(
    val id: Long = 0L,
    val goalName: String,
    val startDate: LocalDate,
    val createdAt: Instant,
)
