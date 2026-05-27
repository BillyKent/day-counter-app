package com.daycounter.domain.usecase

import com.daycounter.domain.repository.CounterRepository
import com.daycounter.domain.repository.MilestoneRepository
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Resets a counter's streak to 0 by setting startDate to today and explicitly
 * clearing all MilestoneRecord rows for the counter (so milestones can fire again).
 */
class ResetCounterUseCase @Inject constructor(
    private val counterRepository: CounterRepository,
    private val milestoneRepository: MilestoneRepository,
    private val clock: Clock,
    private val zone: ZoneId,
) {
    suspend operator fun invoke(counterId: Long) {
        val existing = counterRepository.getById(counterId) ?: return
        milestoneRepository.deleteAllForCounter(counterId)
        val today = LocalDate.now(clock.withZone(zone))
        counterRepository.update(existing.copy(startDate = today))
    }
}
