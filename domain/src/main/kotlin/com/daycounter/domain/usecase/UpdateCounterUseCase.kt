package com.daycounter.domain.usecase

import com.daycounter.domain.repository.CounterRepository
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Validates and persists changes to an existing counter. Validation rules mirror
 * [CreateCounterUseCase]. `createdAt` is preserved from the existing row.
 */
class UpdateCounterUseCase @Inject constructor(
    private val repository: CounterRepository,
    private val clock: Clock,
    private val zone: ZoneId,
) {
    suspend operator fun invoke(counterId: Long, goalName: String, startDate: LocalDate?): Result {
        val trimmed = goalName.trim()
        val today = LocalDate.now(clock.withZone(zone))
        val effectiveStart = startDate ?: today

        if (trimmed.isBlank()) return Result.ValidationError(ValidationFailure.NameBlank)
        if (trimmed.length > MAX_NAME_LENGTH) return Result.ValidationError(ValidationFailure.NameTooLong)
        if (effectiveStart.isAfter(today)) return Result.ValidationError(ValidationFailure.FutureStartDate)

        val existing = repository.getById(counterId) ?: return Result.NotFound
        repository.update(existing.copy(goalName = trimmed, startDate = effectiveStart))
        return Result.Success
    }

    enum class ValidationFailure { NameBlank, NameTooLong, FutureStartDate }

    sealed interface Result {
        data object Success : Result
        data object NotFound : Result
        data class ValidationError(val failure: ValidationFailure) : Result
    }

    private companion object {
        const val MAX_NAME_LENGTH = 100
    }
}
