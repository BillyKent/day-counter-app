package com.daycounter.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * A user-defined streak counter for a personal goal.
 *
 * @property id Row identifier; 0 for a not-yet-persisted instance.
 * @property goalName Human-readable goal name; 1-100 characters.
 * @property startDate Day the streak begins; must not be in the future. Editable only on Create.
 * @property createdAt Wall-clock moment the counter was created; immutable after insert.
 * @property category Optional free-text category; 0-50 characters (null/blank allowed).
 * @property goalMilestoneTarget The milestone the user is aiming for; one of [GOAL_TARGETS].
 *   Drives the progress-ring denominator and the "goal reached" badge. Defaults to 30.
 *
 * The streak day count is derived (never stored). It is computed by
 * `CalculateStreakUseCase` as `ChronoUnit.DAYS.between(startDate, today)`.
 */
data class Counter(
    val id: Long = 0L,
    val goalName: String,
    val startDate: LocalDate,
    val createdAt: Instant,
    val category: String? = null,
    val goalMilestoneTarget: Int = DEFAULT_GOAL_TARGET,
) {
    companion object {
        /** Selectable goal-milestone targets offered on Create/Edit (FR-014). */
        val GOAL_TARGETS: Set<Int> = setOf(7, 30, 100, 365)

        /** Default goal target for a freshly created counter. */
        const val DEFAULT_GOAL_TARGET: Int = 30
    }
}
