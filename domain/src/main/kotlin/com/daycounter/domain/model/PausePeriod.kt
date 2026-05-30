package com.daycounter.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * A completed interval during which a [Counter] was paused. Banked on resume so that paused time can
 * be excluded from the counter's effective day count.
 *
 * @property id Row identifier; 0 for a not-yet-persisted instance.
 * @property counterId Owning counter id.
 * @property startDate Day the pause began (the counter's `pausedSince` at pause time), inclusive.
 * @property endDate Day the counter was resumed; `endDate >= startDate`.
 */
data class PausePeriod(
    val id: Long = 0L,
    val counterId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    /** Whole days spanned by this pause (0 for a same-day pause/resume). */
    val days: Int
        get() = ChronoUnit.DAYS.between(startDate, endDate).coerceAtLeast(0L).toInt()
}
