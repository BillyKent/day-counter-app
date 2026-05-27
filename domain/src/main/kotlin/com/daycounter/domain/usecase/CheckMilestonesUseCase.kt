package com.daycounter.domain.usecase

import com.daycounter.domain.model.MilestoneRecord
import javax.inject.Inject

/**
 * Returns the matching milestone day count if [streakDays] is in
 * [MilestoneRecord.MILESTONE_DAYS], otherwise null.
 */
class CheckMilestonesUseCase @Inject constructor() {
    operator fun invoke(streakDays: Int): Int? =
        streakDays.takeIf { it in MilestoneRecord.MILESTONE_DAYS }
}
