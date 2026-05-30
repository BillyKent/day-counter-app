package com.daycounter.domain.usecase

import com.daycounter.domain.repository.CounterRepository
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Resumes a paused counter, banking the pause interval (pausedSince → today) so its days are
 * excluded, and continuing the streak from the frozen value (FR-008..FR-010). No-op if already
 * active.
 */
class ResumeCounterUseCase @Inject constructor(
    private val repository: CounterRepository,
    private val clock: Clock,
    private val zone: ZoneId,
) {
    suspend operator fun invoke(counterId: Long) {
        repository.resume(counterId, LocalDate.now(clock.withZone(zone)))
    }
}
