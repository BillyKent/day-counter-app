package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import com.daycounter.domain.repository.CounterRepository
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Validates and persists changes to an existing counter. Validation rules mirror
 * [CreateCounterUseCase]. `createdAt` is preserved from the existing row. On Edit the start date is
 * read-only at the UI layer, but it is still validated here for safety.
 */
class UpdateCounterUseCase @Inject constructor(
    private val repository: CounterRepository,
    private val clock: Clock,
    private val zone: ZoneId,
) {
    suspend operator fun invoke(
        counterId: Long,
        goalName: String,
        startDate: LocalDate?,
        category: String? = null,
        goalMilestoneTarget: Int = Counter.DEFAULT_GOAL_TARGET,
    ): Result {
        val trimmedName = goalName.trim()
        val trimmedCategory = category?.trim()
        val today = LocalDate.now(clock.withZone(zone))
        val effectiveStart = startDate ?: today

        if (trimmedName.isBlank()) return Result.ValidationError(ValidationFailure.NameBlank)
        if (trimmedName.length > MAX_NAME_LENGTH) return Result.ValidationError(ValidationFailure.NameTooLong)
        if ((trimmedCategory?.length ?: 0) > MAX_CATEGORY_LENGTH) {
            return Result.ValidationError(ValidationFailure.CategoryTooLong)
        }
        if (goalMilestoneTarget !in Counter.GOAL_TARGETS) {
            return Result.ValidationError(ValidationFailure.InvalidGoalTarget)
        }
        if (effectiveStart.isAfter(today)) return Result.ValidationError(ValidationFailure.FutureStartDate)

        val existing = repository.getById(counterId) ?: return Result.NotFound
        repository.update(
            existing.copy(
                goalName = trimmedName,
                startDate = effectiveStart,
                category = trimmedCategory?.ifBlank { null },
                goalMilestoneTarget = goalMilestoneTarget,
            ),
        )
        return Result.Success
    }

    enum class ValidationFailure { NameBlank, NameTooLong, CategoryTooLong, InvalidGoalTarget, FutureStartDate }

    sealed interface Result {
        data object Success : Result
        data object NotFound : Result
        data class ValidationError(val failure: ValidationFailure) : Result
    }

    private companion object {
        const val MAX_NAME_LENGTH = 100
        const val MAX_CATEGORY_LENGTH = 50
    }
}
