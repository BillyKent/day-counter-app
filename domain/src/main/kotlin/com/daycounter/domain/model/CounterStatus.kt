package com.daycounter.domain.model

/**
 * Lifecycle state of a [Counter].
 *
 * - [ACTIVE]: the streak advances with the calendar.
 * - [PAUSED]: the streak is frozen; paused days are excluded from the effective day count
 *   (see [com.daycounter.domain.usecase.CalculateEffectiveStreakUseCase]).
 */
enum class CounterStatus {
    ACTIVE,
    PAUSED,
}
