package com.daycounter.data.repository

import com.daycounter.data.database.dao.MilestoneRecordDao
import com.daycounter.data.database.entity.toDomain
import com.daycounter.data.database.entity.toEntity
import com.daycounter.domain.model.MilestoneRecord
import com.daycounter.domain.repository.MilestoneRepository
import javax.inject.Inject

class MilestoneRepositoryImpl @Inject constructor(
    private val dao: MilestoneRecordDao,
) : MilestoneRepository {

    override suspend fun insertOrIgnore(record: MilestoneRecord): Long =
        dao.insertOrIgnore(record.toEntity())

    override suspend fun deleteAllForCounter(counterId: Long) {
        dao.deleteAllForCounter(counterId)
    }

    override suspend fun getForCounter(counterId: Long): List<MilestoneRecord> =
        dao.selectForCounter(counterId).map { it.toDomain() }

    override suspend fun markAllShownForCounter(counterId: Long) {
        dao.markAllShownForCounter(counterId)
    }
}
