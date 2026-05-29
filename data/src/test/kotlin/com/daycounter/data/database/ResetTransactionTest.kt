package com.daycounter.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.daycounter.data.database.entity.CounterEntity
import com.daycounter.data.database.entity.MilestoneRecordEntity
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
class ResetTransactionTest {

    private lateinit var db: AppDatabase
    private val today: LocalDate = LocalDate.of(2026, 5, 29)
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

    private suspend fun seedCounterWithMilestones(startDate: LocalDate): Long {
        val counterId = db.counterDao().insert(
            CounterEntity(goalName = "Run", startDate = startDate, createdAt = now),
        )
        listOf(1, 7).forEach { milestone ->
            db.milestoneRecordDao().insertOrIgnore(
                MilestoneRecordEntity(counterId = counterId, milestoneDays = milestone, notifiedAt = now),
            )
        }
        return counterId
    }

    @Test
    fun `archives streak when greater than zero, clears milestones, resets start date`() = runTest {
        val counterId = seedCounterWithMilestones(LocalDate.of(2026, 5, 14))

        db.counterDao().archiveAndReset(counterId, streakDaysAtReset = 15, today = today, now = now)

        val archived = db.pastStreakRecordDao().pagedByCounter(counterId, limit = 50, offset = 0)
        assertEquals(1, archived.size)
        assertEquals(15, archived.first().streakDays)
        assertEquals("Reiniciado", archived.first().reason)
        assertEquals(today, archived.first().endDate)
        assertTrue(db.milestoneRecordDao().selectForCounter(counterId).isEmpty())
        assertEquals(today, db.counterDao().getById(counterId)!!.startDate)
    }

    @Test
    fun `does not archive when streak is zero but still clears milestones and resets`() = runTest {
        val counterId = seedCounterWithMilestones(today)

        db.counterDao().archiveAndReset(counterId, streakDaysAtReset = 0, today = today, now = now)

        assertTrue(db.pastStreakRecordDao().pagedByCounter(counterId, 50, 0).isEmpty())
        assertTrue(db.milestoneRecordDao().selectForCounter(counterId).isEmpty())
        assertEquals(today, db.counterDao().getById(counterId)!!.startDate)
    }

    @Test
    fun `reset effects are applied together`() = runTest {
        val counterId = seedCounterWithMilestones(LocalDate.of(2026, 5, 9))
        assertEquals(2, db.milestoneRecordDao().selectForCounter(counterId).size)

        db.counterDao().archiveAndReset(counterId, streakDaysAtReset = 20, today = today, now = now)

        // Both the archive insert and the milestone clear are observed after the single call.
        assertEquals(1, db.pastStreakRecordDao().pagedByCounter(counterId, 50, 0).size)
        assertEquals(0, db.milestoneRecordDao().selectForCounter(counterId).size)
    }
}
