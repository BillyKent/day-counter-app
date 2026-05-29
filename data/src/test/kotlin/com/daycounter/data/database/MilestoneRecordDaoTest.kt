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
class MilestoneRecordDaoTest {

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

    @Test
    fun `database opens with the new schema and basic CRUD works`() = runTest {
        val counterId = db.counterDao().insert(
            CounterEntity(
                goalName = "Read",
                startDate = LocalDate.of(2026, 5, 1),
                createdAt = now,
                category = "Health",
                goalMilestoneTarget = 100,
            ),
        )
        val stored = db.counterDao().getById(counterId)!!
        assertEquals("Health", stored.category)
        assertEquals(100, stored.goalMilestoneTarget)
    }

    @Test
    fun `selectForCounter returns inserted rows with celebrationShown default false`() = runTest {
        val counterId = db.counterDao().insert(
            CounterEntity(goalName = "Read", startDate = LocalDate.of(2026, 5, 1), createdAt = now),
        )
        db.milestoneRecordDao().insertOrIgnore(MilestoneRecordEntity(counterId = counterId, milestoneDays = 1, notifiedAt = now))
        db.milestoneRecordDao().insertOrIgnore(MilestoneRecordEntity(counterId = counterId, milestoneDays = 7, notifiedAt = now))

        val rows = db.milestoneRecordDao().selectForCounter(counterId)
        assertEquals(listOf(1, 7), rows.map { it.milestoneDays })
        assertTrue(rows.none { it.celebrationShown })
    }

    @Test
    fun `markAllShownForCounter flips celebrationShown for every row`() = runTest {
        val counterId = db.counterDao().insert(
            CounterEntity(goalName = "Read", startDate = LocalDate.of(2026, 5, 1), createdAt = now),
        )
        db.milestoneRecordDao().insertOrIgnore(MilestoneRecordEntity(counterId = counterId, milestoneDays = 1, notifiedAt = now))
        db.milestoneRecordDao().insertOrIgnore(MilestoneRecordEntity(counterId = counterId, milestoneDays = 7, notifiedAt = now))

        db.milestoneRecordDao().markAllShownForCounter(counterId)

        assertTrue(db.milestoneRecordDao().selectForCounter(counterId).all { it.celebrationShown })
    }

    @Test
    fun `duplicate milestone for same counter is ignored`() = runTest {
        val counterId = db.counterDao().insert(
            CounterEntity(goalName = "Read", startDate = LocalDate.of(2026, 5, 1), createdAt = now),
        )
        db.milestoneRecordDao().insertOrIgnore(MilestoneRecordEntity(counterId = counterId, milestoneDays = 1, notifiedAt = now))
        val secondResult = db.milestoneRecordDao().insertOrIgnore(MilestoneRecordEntity(counterId = counterId, milestoneDays = 1, notifiedAt = now))

        assertEquals(-1L, secondResult)
        assertEquals(1, db.milestoneRecordDao().selectForCounter(counterId).size)
    }
}
