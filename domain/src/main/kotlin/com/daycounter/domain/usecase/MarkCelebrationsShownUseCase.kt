package com.daycounter.domain.usecase

import com.daycounter.domain.repository.MilestoneRepository
import javax.inject.Inject

/**
 * Marks every milestone row of a counter as celebration-shown (FR-021), so older skipped
 * milestones never produce a delayed auto-launch.
 */
class MarkCelebrationsShownUseCase @Inject constructor(
    private val milestoneRepository: MilestoneRepository,
) {
    suspend operator fun invoke(counterId: Long) =
        milestoneRepository.markAllShownForCounter(counterId)
}
