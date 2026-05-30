package com.daycounter.domain.usecase

import com.daycounter.domain.repository.CounterRepository
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Pauses an active counter, freezing its effective streak at today (FR-007..FR-009). No-op if the
 * counter is already paused. Pausing never archives a streak or clears milestones (FR-012/FR-013).
 */
class PauseCounterUseCase @Inject constructor(
    private val repository: CounterRepository,
    private val clock: Clock,
    private val zone: ZoneId,
) {
    suspend operator fun invoke(counterId: Long) {
        repository.pause(counterId, LocalDate.now(clock.withZone(zone)))
    }
}
