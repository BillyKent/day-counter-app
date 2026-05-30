package com.daycounter.domain.usecase

import com.daycounter.domain.model.DataSnapshot
import com.daycounter.domain.repository.CounterRepository
import javax.inject.Inject

/** Restores a [DataSnapshot] captured by [EraseAllDataUseCase] (the "Deshacer" action, FR-031). */
class RestoreAllDataUseCase @Inject constructor(
    private val repository: CounterRepository,
) {
    suspend operator fun invoke(snapshot: DataSnapshot) = repository.restore(snapshot)
}
