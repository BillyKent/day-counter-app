package com.daycounter.domain.usecase

import com.daycounter.domain.repository.CounterRepository
import javax.inject.Inject

/**
 * Deletes a counter. Room's ON DELETE CASCADE removes associated MilestoneRecord
 * rows; widget bindings have their counter_id set to NULL.
 */
class DeleteCounterUseCase @Inject constructor(
    private val repository: CounterRepository,
) {
    suspend operator fun invoke(counterId: Long) {
        val existing = repository.getById(counterId) ?: return
        repository.delete(existing)
    }
}
