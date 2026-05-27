package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import com.daycounter.domain.repository.CounterRepository
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Validates a new counter and inserts it via [CounterRepository]. Validation rules:
 * - `goalName` non-blank, length <= 100.
 * - `startDate` not in the future. Null defaults to today.
 */
class CreateCounterUseCase @Inject constructor(
    private val repository: CounterRepository,
    private val clock: Clock,
    private val zone: ZoneId,
) {
    suspend operator fun invoke(goalName: String, startDate: LocalDate?): Result {
        val trimmed = goalName.trim()
        val today = LocalDate.now(clock.withZone(zone))
        val effectiveStart = startDate ?: today

        if (trimmed.isBlank()) return Result.ValidationError(ValidationFailure.NameBlank)
        if (trimmed.length > MAX_NAME_LENGTH) return Result.ValidationError(ValidationFailure.NameTooLong)
        if (effectiveStart.isAfter(today)) return Result.ValidationError(ValidationFailure.FutureStartDate)

        val newCounter = Counter(
            goalName = trimmed,
            startDate = effectiveStart,
            createdAt = clock.instant(),
        )
        val id = repository.insert(newCounter)
        return Result.Success(id)
    }

    enum class ValidationFailure { NameBlank, NameTooLong, FutureStartDate }

    sealed interface Result {
        data class Success(val id: Long) : Result
        data class ValidationError(val failure: ValidationFailure) : Result
    }

    private companion object {
        const val MAX_NAME_LENGTH = 100
    }
}
