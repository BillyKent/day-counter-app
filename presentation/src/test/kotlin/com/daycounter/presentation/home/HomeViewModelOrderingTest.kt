package com.daycounter.presentation.home

import app.cash.turbine.test
import com.daycounter.domain.model.Counter
import com.daycounter.domain.repository.CounterRepository
import com.daycounter.domain.usecase.CalculateStreakUseCase
import com.daycounter.domain.usecase.GetAllCountersUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class HomeViewModelOrderingTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `tied streakDays preserve repository order (createdAt ASC tie-breaker)`() = runTest {
        val today = LocalDate.of(2026, 5, 27)
        val sameDate = today.minusDays(10)
        // CounterDao orders by start_date ASC then created_at ASC; both rows have the same
        // start_date, so the earlier createdAt wins the tie.
        val counters = listOf(
            Counter(1L, "Earlier", sameDate, Instant.parse("2026-05-17T08:00:00Z")),
            Counter(2L, "Later", sameDate, Instant.parse("2026-05-17T09:00:00Z")),
        )
        val repo = mockk<CounterRepository>()
        every { repo.getAllSortedByStreak() } returns flowOf(counters)
        val getAll = GetAllCountersUseCase(repo)
        val clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)
        val calc = CalculateStreakUseCase(clock, ZoneOffset.UTC)

        val sut = HomeViewModel(getAll, calc)

        sut.uiState.test {
            // Skip the initial loading state, then read the first emission with data.
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }
            assertEquals(listOf(1L, 2L), state.counters.map { it.id })
            assertEquals(listOf(10, 10), state.counters.map { it.streakDays })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
