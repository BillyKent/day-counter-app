package com.daycounter.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.daycounter.data.database.AppDatabase
import com.daycounter.data.database.entity.CounterEntity
import com.daycounter.data.database.entity.MilestoneRecordEntity
import com.daycounter.data.database.entity.PastStreakRecordEntity
import com.daycounter.data.database.entity.PausePeriodEntity
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
class EraseAllRestoreTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: CounterRepositoryImpl
    private val now: Instant = Instant.parse("2026-05-29T12:00:00Z")

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        repo = CounterRepositoryImpl(db.counterDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `eraseAll snapshots everything then clears, restore puts it all back`() = runTest {
        val counterId = db.counterDao().insert(
            CounterEntity(goalName = "Run", startDate = LocalDate.of(2026, 1, 1), createdAt = now),
        )
        db.pausePeriodDao().insert(
            PausePeriodEntity(counterId = counterId, startDate = LocalDate.of(2026, 1, 5), endDate = LocalDate.of(2026, 1, 8)),
        )
        db.milestoneRecordDao().insertOrIgnore(
            MilestoneRecordEntity(counterId = counterId, milestoneDays = 7, notifiedAt = now),
        )
        db.pastStreakRecordDao().insert(
            PastStreakRecordEntity(counterId = counterId, streakDays = 5, reason = "Reiniciado", endDate = LocalDate.of(2026, 1, 4), createdAt = now),
        )

        val snapshot = repo.eraseAll()

        // Snapshot captured everything.
        assertEquals(1, snapshot.counters.size)
        assertEquals(1, snapshot.pausePeriods.size)
        assertEquals(1, snapshot.milestones.size)
        assertEquals(1, snapshot.pastStreaks.size)
        // DB is now empty.
        assertTrue(db.counterDao().selectAllCounters().isEmpty())
        assertTrue(db.pausePeriodDao().selectAll().isEmpty())

        repo.restore(snapshot)

        // Everything is back with the same ids.
        assertEquals(listOf(counterId), db.counterDao().selectAllCounters().map { it.id })
        assertEquals(1, db.pausePeriodDao().selectAll().size)
        assertEquals(1, db.counterDao().selectAllMilestones().size)
        assertEquals(1, db.counterDao().selectAllPastStreaks().size)
    }
}
