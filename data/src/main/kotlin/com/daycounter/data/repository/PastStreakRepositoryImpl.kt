package com.daycounter.data.repository

import com.daycounter.data.database.dao.PastStreakRecordDao
import com.daycounter.data.database.entity.toDomain
import com.daycounter.data.database.entity.toEntity
import com.daycounter.domain.model.PastStreakRecord
import com.daycounter.domain.repository.PastStreakRepository
import javax.inject.Inject

class PastStreakRepositoryImpl @Inject constructor(
    private val dao: PastStreakRecordDao,
) : PastStreakRepository {

    override suspend fun insert(record: PastStreakRecord): Long = dao.insert(record.toEntity())

    override suspend fun getForCounterPaged(
        counterId: Long,
        limit: Int,
        offset: Int,
    ): List<PastStreakRecord> =
        dao.pagedByCounter(counterId, limit, offset).map { it.toDomain() }
}
