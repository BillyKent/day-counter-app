package com.daycounter.presentation.home

import app.cash.turbine.test
import com.daycounter.domain.model.Counter
import com.daycounter.domain.model.CounterStatus
import com.daycounter.domain.repository.CounterRepository
import com.daycounter.domain.repository.PausePeriodRepository
import com.daycounter.domain.usecase.CalculateEffectiveStreakUseCase
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

class HomeViewModelFilterTest {

    private val today = LocalDate.of(2026, 5, 29)

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun sut(): HomeViewModel {
        val active = Counter(1L, "Active", today.minusDays(10), Instant.parse("2026-05-01T00:00:00Z"))
        val paused = Counter(
            id = 2L,
            goalName = "Paused",
            startDate = today.minusDays(20),
            createdAt = Instant.parse("2026-05-02T00:00:00Z"),
            status = CounterStatus.PAUSED,
            pausedSince = today.minusDays(5),
        )
        val repo = mockk<CounterRepository>()
        every { repo.getAllSortedByStreak() } returns flowOf(listOf(active, paused))
        val pauseRepo = mockk<PausePeriodRepository>()
        every { pauseRepo.observeAll() } returns flowOf(emptyList())
        val clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)
        return HomeViewModel(
            GetAllCountersUseCase(repo),
            pauseRepo,
            CalculateEffectiveStreakUseCase(clock, ZoneOffset.UTC),
        )
    }

    @Test
    fun `counts reflect active and paused, filter narrows the list`() = runTest {
        val vm = sut()
        vm.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            // ALL: both counters, counts correct.
            assertEquals(2, state.counters.size)
            assertEquals(FilterCounts(all = 2, active = 1, paused = 1), state.counts)

            vm.setFilter(CounterFilter.PAUSED)
            state = awaitItem()
            assertEquals(listOf(2L), state.counters.map { it.id })
            assertEquals(true, state.counters.single().isPaused)

            vm.setFilter(CounterFilter.ACTIVE)
            state = awaitItem()
            assertEquals(listOf(1L), state.counters.map { it.id })
            assertEquals(false, state.counters.single().isPaused)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
