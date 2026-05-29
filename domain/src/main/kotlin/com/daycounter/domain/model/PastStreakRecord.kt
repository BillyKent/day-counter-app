package com.daycounter.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * A snapshot of a streak archived at reset time (FR-017, US7). Past streaks are retained
 * indefinitely and surfaced, paginated, in the per-counter History screen.
 *
 * @property id Row identifier; 0 for a not-yet-persisted instance.
 * @property counterId Owning counter's id; FK with ON DELETE CASCADE.
 * @property streakDays Streak length at the moment of reset; always `> 0`
 *   (a 0-day reset creates no record).
 * @property reason Why the streak ended; currently always [REASON_RESET].
 * @property endDate The day the streak ended (today at reset).
 * @property createdAt Wall-clock moment the record was archived.
 */
data class PastStreakRecord(
    val id: Long = 0L,
    val counterId: Long,
    val streakDays: Int,
    val reason: String,
    val endDate: LocalDate,
    val createdAt: Instant,
) {
    companion object {
        /** The only reason currently produced: a user-initiated reset. */
        const val REASON_RESET: String = "Reiniciado"
    }
}
