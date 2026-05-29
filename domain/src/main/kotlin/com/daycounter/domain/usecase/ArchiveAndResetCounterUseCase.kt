package com.daycounter.domain.usecase

import com.daycounter.domain.repository.CounterRepository
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Archives the current streak (if > 0) and resets a counter to start from today, atomically
 * (FR-017). The streak length is computed with [CalculateStreakUseCase] before delegating to the
 * data layer's single reset `@Transaction`. Replaces the previous `ResetCounterUseCase`.
 */
class ArchiveAndResetCounterUseCase @Inject constructor(
    private val counterRepository: CounterRepository,
    private val calculateStreak: CalculateStreakUseCase,
    private val clock: Clock,
    private val zone: ZoneId,
) {
    suspend operator fun invoke(counterId: Long) {
        val counter = counterRepository.getById(counterId) ?: return
        val streakAtReset = calculateStreak(counter.startDate)
        val today = LocalDate.now(clock.withZone(zone))
        counterRepository.archiveAndReset(
            counterId = counterId,
            streakDaysAtReset = streakAtReset,
            today = today,
            now = clock.instant(),
        )
    }
}
