package com.daycounter.domain.usecase

import com.daycounter.domain.model.MilestoneRecord
import javax.inject.Inject

/**
 * Returns the smallest milestone in [MilestoneRecord.MILESTONE_DAYS] strictly greater than
 * [streakDays], or `null` when the streak has reached/passed the largest milestone (FR-010).
 */
class GetNextMilestoneUseCase @Inject constructor() {
    operator fun invoke(streakDays: Int): Int? =
        MilestoneRecord.MILESTONE_DAYS.filter { it > streakDays }.minOrNull()
}
