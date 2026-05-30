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
 * @property status Whether the counter is [CounterStatus.ACTIVE] or [CounterStatus.PAUSED].
 * @property pausedSince When the current pause began; non-null **iff** [status] is
 *   [CounterStatus.PAUSED]. The effective day count freezes at this date while paused.
 *
 * The streak day count is derived (never stored). The active-only value is
 * `ChronoUnit.DAYS.between(startDate, today)`; the pause-aware effective value is computed by
 * `CalculateEffectiveStreakUseCase` (elapsed − completed paused days, frozen at [pausedSince]).
 */
data class Counter(
    val id: Long = 0L,
    val goalName: String,
    val startDate: LocalDate,
    val createdAt: Instant,
    val category: String? = null,
    val goalMilestoneTarget: Int = DEFAULT_GOAL_TARGET,
    val status: CounterStatus = CounterStatus.ACTIVE,
    val pausedSince: LocalDate? = null,
) {
    /** True when this counter is currently paused. */
    val isPaused: Boolean get() = status == CounterStatus.PAUSED

    companion object {
        /** Selectable goal-milestone targets offered on Create/Edit (FR-014). */
        val GOAL_TARGETS: Set<Int> = setOf(7, 30, 100, 365)

        /** Default goal target for a freshly created counter. */
        const val DEFAULT_GOAL_TARGET: Int = 30

        /**
         * Fixed category chip set offered on Create/Edit (FR-006c). Stable keys; the UI resolves a
         * localized label per key. Pre-existing free-text categories display as-is but are not
         * offered as new choices.
         */
        val CATEGORIES: List<String> =
            listOf("salud", "ejercicio", "ahorro", "estudio", "mente")
    }
}
