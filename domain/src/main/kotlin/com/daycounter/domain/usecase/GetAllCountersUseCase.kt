package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import com.daycounter.domain.repository.CounterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Streams the counter list, sorted by longest-streak first with createdAt as tie-breaker. */
class GetAllCountersUseCase @Inject constructor(
    private val repository: CounterRepository,
) {
    operator fun invoke(): Flow<List<Counter>> = repository.getAllSortedByStreak()
}
