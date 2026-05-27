package com.daycounter.data.repository

import com.daycounter.data.database.dao.MilestoneRecordDao
import com.daycounter.data.database.entity.MilestoneRecordEntity
import com.daycounter.domain.model.MilestoneRecord
import com.daycounter.domain.repository.MilestoneRepository
import javax.inject.Inject

class MilestoneRepositoryImpl @Inject constructor(
    private val dao: MilestoneRecordDao,
) : MilestoneRepository {

    override suspend fun insertOrIgnore(record: MilestoneRecord): Long {
        val entity = MilestoneRecordEntity(
            id = record.id,
            counterId = record.counterId,
            milestoneDays = record.milestoneDays,
            notifiedAt = record.notifiedAt,
        )
        return dao.insertOrIgnore(entity)
    }

    override suspend fun deleteAllForCounter(counterId: Long) {
        dao.deleteAllForCounter(counterId)
    }
}
