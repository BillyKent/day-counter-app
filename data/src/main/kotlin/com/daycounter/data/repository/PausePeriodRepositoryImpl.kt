package com.daycounter.data.repository

import com.daycounter.data.database.dao.PausePeriodDao
import com.daycounter.data.database.entity.toDomain
import com.daycounter.data.database.entity.toEntity
import com.daycounter.domain.model.PausePeriod
import com.daycounter.domain.repository.PausePeriodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PausePeriodRepositoryImpl @Inject constructor(
    private val dao: PausePeriodDao,
) : PausePeriodRepository {

    override suspend fun insert(period: PausePeriod) {
        dao.insert(period.toEntity())
    }

    override fun observeAll(): Flow<List<PausePeriod>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun completedPausedDays(counterId: Long): Int =
        dao.selectForCounter(counterId).sumOf { it.toDomain().days }
}
