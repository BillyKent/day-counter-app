package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import com.daycounter.domain.repository.CounterRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ArchiveAndResetCounterUseCaseTest {

    private val today = LocalDate.of(2026, 5, 29)
    private val zone = ZoneOffset.UTC
    private val clock = Clock.fixed(today.atStartOfDay(zone).toInstant(), zone)
    private lateinit var repo: CounterRepository
    private lateinit var sut: ArchiveAndResetCounterUseCase

    @Before
    fun setUp() {
        repo = mockk(relaxed = true)
        sut = ArchiveAndResetCounterUseCase(repo, CalculateStreakUseCase(clock, zone), clock, zone)
    }

    private fun counter(daysAgo: Long) = Counter(
        id = 1L,
        goalName = "Run",
        startDate = today.minusDays(daysAgo),
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    )

    @Test
    fun `passes the computed streak to the reset transaction`() = runTest {
        coEvery { repo.getById(1L) } returns counter(daysAgo = 15)
        sut(1L)
        coVerify { repo.archiveAndReset(counterId = 1L, streakDaysAtReset = 15, today = today, now = clock.instant()) }
    }

    @Test
    fun `zero-day streak is still forwarded (the DAO guards archival)`() = runTest {
        coEvery { repo.getById(1L) } returns counter(daysAgo = 0)
        sut(1L)
        coVerify { repo.archiveAndReset(counterId = 1L, streakDaysAtReset = 0, today = today, now = clock.instant()) }
    }

    @Test
    fun `missing counter performs no reset`() = runTest {
        coEvery { repo.getById(99L) } returns null
        sut(99L)
        coVerify(exactly = 0) { repo.archiveAndReset(any(), any(), any(), any()) }
    }
}
