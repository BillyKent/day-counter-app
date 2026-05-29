package com.daycounter.domain.usecase

import com.daycounter.domain.model.PastStreakRecord
import com.daycounter.domain.repository.PastStreakRepository
import javax.inject.Inject

/**
 * Returns a page of archived streaks for a counter, newest `end_date` first, in batches of
 * [PAGE_SIZE] (FR-029). `page` is 0-based; the offset is `page * PAGE_SIZE`.
 */
class GetPastStreaksUseCase @Inject constructor(
    private val repository: PastStreakRepository,
) {
    suspend operator fun invoke(counterId: Long, page: Int): List<PastStreakRecord> =
        repository.getForCounterPaged(
            counterId = counterId,
            limit = PAGE_SIZE,
            offset = page.coerceAtLeast(0) * PAGE_SIZE,
        )

    companion object {
        const val PAGE_SIZE = 50
    }
}
