package com.daycounter.domain.usecase

import com.daycounter.domain.model.MilestoneRecord
import javax.inject.Inject

/**
 * Returns the highest milestone in [MilestoneRecord.MILESTONE_DAYS] that is `<= streakDays`, or
 * `null` when no milestone has been reached. Drives "Revivir celebración" and the auto-launch
 * target (FR-024).
 */
class GetMostRecentMilestoneUseCase @Inject constructor() {
    operator fun invoke(streakDays: Int): Int? =
        MilestoneRecord.MILESTONE_DAYS.filter { it <= streakDays }.maxOrNull()
}
