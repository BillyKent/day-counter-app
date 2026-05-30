package com.daycounter.domain.repository

import com.daycounter.domain.model.PausePeriod
import kotlinx.coroutines.flow.Flow

/** Repository for completed [PausePeriod] rows. Implementations live in `:data`. */
interface PausePeriodRepository {
    /** Banks a completed pause interval. */
    suspend fun insert(period: PausePeriod)

    /** All completed pause periods across all counters (for Stats and snapshot/restore). */
    fun observeAll(): Flow<List<PausePeriod>>

    /** Sum of completed paused days for one counter — fed to `CalculateEffectiveStreakUseCase`. */
    suspend fun completedPausedDays(counterId: Long): Int
}
