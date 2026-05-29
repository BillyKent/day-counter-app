package com.daycounter.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.daycounter.data.database.entity.CounterEntity
import com.daycounter.data.database.entity.PastStreakRecordEntity
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
class PastStreakRecordDaoTest {

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
        db.counterDao().insert(CounterEntity(goalName = "Run", startDate = LocalDate.of(2026, 1, 1), createdAt = now))

    @Test
    fun `pages newest end date first with limit and offset`() = runTest {
        val counterId = insertCounter()
        // end dates ascending by insertion; expect DESC ordering on read.
        val endDates = listOf(
            LocalDate.of(2026, 1, 10),
            LocalDate.of(2026, 2, 10),
            LocalDate.of(2026, 3, 10),
        )
        endDates.forEach { date ->
            db.pastStreakRecordDao().insert(
                PastStreakRecordEntity(
                    counterId = counterId, streakDays = 5, reason = "Reiniciado", endDate = date, createdAt = now,
                ),
            )
        }

        val firstPage = db.pastStreakRecordDao().pagedByCounter(counterId, limit = 2, offset = 0)
        assertEquals(listOf(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 2, 10)), firstPage.map { it.endDate })

        val secondPage = db.pastStreakRecordDao().pagedByCounter(counterId, limit = 2, offset = 2)
        assertEquals(listOf(LocalDate.of(2026, 1, 10)), secondPage.map { it.endDate })
    }

    @Test
    fun `same end date breaks ties by id descending`() = runTest {
        val counterId = insertCounter()
        val sameDate = LocalDate.of(2026, 4, 1)
        val firstId = db.pastStreakRecordDao().insert(
            PastStreakRecordEntity(counterId = counterId, streakDays = 1, reason = "Reiniciado", endDate = sameDate, createdAt = now),
        )
        val secondId = db.pastStreakRecordDao().insert(
            PastStreakRecordEntity(counterId = counterId, streakDays = 2, reason = "Reiniciado", endDate = sameDate, createdAt = now),
        )

        val page = db.pastStreakRecordDao().pagedByCounter(counterId, limit = 50, offset = 0)
        assertEquals(listOf(secondId, firstId), page.map { it.id })
    }

    @Test
    fun `deleting counter cascade-deletes its past streaks`() = runTest {
        val counterId = insertCounter()
        db.pastStreakRecordDao().insert(
            PastStreakRecordEntity(counterId = counterId, streakDays = 5, reason = "Reiniciado", endDate = LocalDate.of(2026, 1, 5), createdAt = now),
        )

        db.counterDao().delete(db.counterDao().getById(counterId)!!)

        assertTrue(db.pastStreakRecordDao().pagedByCounter(counterId, 50, 0).isEmpty())
    }
}
