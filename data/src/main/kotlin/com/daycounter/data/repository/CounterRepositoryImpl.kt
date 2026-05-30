package com.daycounter.data.repository

import com.daycounter.data.database.dao.CounterDao
import com.daycounter.data.database.entity.toDomain
import com.daycounter.data.database.entity.toEntity
import com.daycounter.domain.model.Counter
import com.daycounter.domain.repository.CounterRepository
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CounterRepositoryImpl @Inject constructor(
    private val dao: CounterDao,
) : CounterRepository {

    override fun getAllSortedByStreak(): Flow<List<Counter>> =
        dao.getAllCountersSortedByStreak().map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: Long): Counter? = dao.getById(id)?.toDomain()

    override suspend fun insert(counter: Counter): Long = dao.insert(counter.toEntity())

    override suspend fun update(counter: Counter) = dao.update(counter.toEntity())

    override suspend fun delete(counter: Counter) = dao.delete(counter.toEntity())

    override suspend fun archiveAndReset(
        counterId: Long,
        streakDaysAtReset: Int,
        today: LocalDate,
        now: Instant,
    ) = dao.archiveAndReset(counterId, streakDaysAtReset, today, now)

    override suspend fun pause(counterId: Long, today: LocalDate) = dao.pause(counterId, today)

    override suspend fun resume(counterId: Long, today: LocalDate) = dao.resume(counterId, today)
}
