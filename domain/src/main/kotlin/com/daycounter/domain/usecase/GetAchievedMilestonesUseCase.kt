package com.daycounter.domain.usecase

import com.daycounter.domain.model.MilestoneRecord
import javax.inject.Inject

/**
 * Returns every milestone in [MilestoneRecord.MILESTONE_DAYS] that is `<= streakDays`, ascending.
 * Informational (FR-022): drives the non-interactive achieved-milestones chips on Detail.
 */
class GetAchievedMilestonesUseCase @Inject constructor() {
    operator fun invoke(streakDays: Int): List<Int> =
        MilestoneRecord.MILESTONE_DAYS.filter { it <= streakDays }.sorted()
}
