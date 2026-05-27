package com.daycounter.domain.usecase

import app.cash.turbine.test
import com.daycounter.domain.model.Counter
import com.daycounter.domain.repository.CounterRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class GetAllCountersUseCaseTest {

    @Test
    fun `flow forwards the repository emission unchanged`() = runTest {
        val list = listOf(
            Counter(1, "Run", LocalDate.of(2026, 5, 1), Instant.parse("2026-05-01T00:00:00Z")),
            Counter(2, "Read", LocalDate.of(2026, 5, 20), Instant.parse("2026-05-20T00:00:00Z")),
        )
        val repo = mockk<CounterRepository>()
        every { repo.getAllSortedByStreak() } returns flowOf(list)
        val sut = GetAllCountersUseCase(repo)

        sut().test {
            assertEquals(list, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `ties on streakDays preserve repository order (createdAt ASC)`() = runTest {
        val sameDate = LocalDate.of(2026, 5, 20)
        val ordered = listOf(
            Counter(1, "Earlier", sameDate, Instant.parse("2026-05-20T08:00:00Z")),
            Counter(2, "Later", sameDate, Instant.parse("2026-05-20T09:00:00Z")),
        )
        val repo = mockk<CounterRepository>()
        every { repo.getAllSortedByStreak() } returns flowOf(ordered)
        val sut = GetAllCountersUseCase(repo)

        sut().test {
            val emitted = awaitItem()
            assertEquals(listOf(1L, 2L), emitted.map { it.id })
            awaitComplete()
        }
    }
}
