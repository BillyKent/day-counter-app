package com.daycounter.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.daycounter.data.database.entity.CounterEntity
import com.daycounter.data.database.entity.PausePeriodEntity
import com.daycounter.data.database.entity.toDomain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class PausePeriodDaoTest {

    private lateinit var db: AppDatabase
    private val now: Instant = Instant.parse("2026-05-29T12:00:00Z")

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() = db.close()

    private suspend fun insertCounter(): Long =
        db.counterDao().insert(
            CounterEntity(goalName = "Run", startDate = LocalDate.of(2026, 1, 1), createdAt = now),
        )

    @Test
    fun `insert and select returns periods for counter with correct day math`() = runTest {
        val counterId = insertCounter()
        db.pausePeriodDao().insert(
            PausePeriodEntity(counterId = counterId, startDate = LocalDate.of(2026, 1, 10), endDate = LocalDate.of(2026, 1, 20)),
        )
        db.pausePeriodDao().insert(
            PausePeriodEntity(counterId = counterId, startDate = LocalDate.of(2026, 2, 1), endDate = LocalDate.of(2026, 2, 4)),
        )

        val periods = db.pausePeriodDao().selectForCounter(counterId)
        assertEquals(2, periods.size)
        assertEquals(listOf(10, 3), periods.map { it.toDomain().days })
        assertEquals(2, db.pausePeriodDao().selectAll().size)
    }

    @Test
    fun `deleting counter cascade-deletes its pause periods`() = runTest {
        val counterId = insertCounter()
        db.pausePeriodDao().insert(
            PausePeriodEntity(counterId = counterId, startDate = LocalDate.of(2026, 1, 10), endDate = LocalDate.of(2026, 1, 12)),
        )

        db.counterDao().delete(db.counterDao().getById(counterId)!!)

        assertTrue(db.pausePeriodDao().selectForCounter(counterId).isEmpty())
        assertTrue(db.pausePeriodDao().selectAll().isEmpty())
    }

    @Test
    fun `counter status and pausedSince round-trip through Room`() = runTest {
        val counterId = insertCounter()
        val stored = db.counterDao().getById(counterId)!!
        // Default status is ACTIVE.
        assertEquals(com.daycounter.domain.model.CounterStatus.ACTIVE, stored.status)

        db.counterDao().update(
            stored.copy(
                status = com.daycounter.domain.model.CounterStatus.PAUSED,
                pausedSince = LocalDate.of(2026, 1, 10),
            ),
        )
        val updated = db.counterDao().getById(counterId)!!.toDomain()
        assertTrue(updated.isPaused)
        assertEquals(LocalDate.of(2026, 1, 10), updated.pausedSince)
    }
}
