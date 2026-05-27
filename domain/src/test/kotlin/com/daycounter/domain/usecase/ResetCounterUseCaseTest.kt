package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import com.daycounter.domain.repository.CounterRepository
import com.daycounter.domain.repository.MilestoneRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ResetCounterUseCaseTest {

    private val today = LocalDate.of(2026, 5, 27)
    private val clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)

    @Test
    fun `deletes milestone records then updates counter startDate to today`() = runTest {
        val counter = Counter(
            id = 5L,
            goalName = "Run",
            startDate = today.minusDays(60),
            createdAt = Instant.parse("2026-03-28T00:00:00Z"),
        )
        val counterRepo = mockk<CounterRepository>(relaxed = true)
        val milestoneRepo = mockk<MilestoneRepository>(relaxed = true)
        coEvery { counterRepo.getById(counter.id) } returns counter
        val captured = slot<Counter>()

        val sut = ResetCounterUseCase(counterRepo, milestoneRepo, clock, ZoneOffset.UTC)
        sut(counter.id)

        coVerifyOrder {
            milestoneRepo.deleteAllForCounter(counter.id)
            counterRepo.update(capture(captured))
        }
        assertEquals(today, captured.captured.startDate)
    }

    @Test
    fun `is a no-op when counter does not exist`() = runTest {
        val counterRepo = mockk<CounterRepository>(relaxed = true)
        val milestoneRepo = mockk<MilestoneRepository>(relaxed = true)
        coEvery { counterRepo.getById(99L) } returns null

        val sut = ResetCounterUseCase(counterRepo, milestoneRepo, clock, ZoneOffset.UTC)
        sut(99L)

        coVerify(exactly = 0) { milestoneRepo.deleteAllForCounter(any()) }
        coVerify(exactly = 0) { counterRepo.update(any()) }
    }
}
