package com.daycounter.domain.usecase

import com.daycounter.domain.model.DataSnapshot
import com.daycounter.domain.repository.CounterRepository
import javax.inject.Inject

/**
 * Erases all counters and their history, returning a [DataSnapshot] of what was removed so the UI can
 * offer a time-bounded undo (FR-030/FR-031).
 */
class EraseAllDataUseCase @Inject constructor(
    private val repository: CounterRepository,
) {
    suspend operator fun invoke(): DataSnapshot = repository.eraseAll()
}
