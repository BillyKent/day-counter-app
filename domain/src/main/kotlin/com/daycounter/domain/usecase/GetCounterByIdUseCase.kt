package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import com.daycounter.domain.repository.CounterRepository
import javax.inject.Inject

/** Returns the counter for the given id, or null if it does not exist. */
class GetCounterByIdUseCase @Inject constructor(
    private val repository: CounterRepository,
) {
    suspend operator fun invoke(id: Long): Counter? = repository.getById(id)
}
