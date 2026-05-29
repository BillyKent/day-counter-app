package com.daycounter.domain.usecase

import com.daycounter.domain.model.PastStreakRecord
import com.daycounter.domain.repository.PastStreakRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class HistoryComputationsTest {

    private val today = LocalDate.of(2026, 5, 29)
    private val startDate = LocalDate.of(2026, 5, 20) // 9-day streak

    @Test
    fun `today is its own category`() {
        assertEquals(CalendarDayCategory.TODAY, HistoryComputations.calendarDayCategory(today, startDate, today))
    }

    @Test
    fun `days within the streak window are in-streak`() {
        assertEquals(
            CalendarDayCategory.IN_STREAK,
            HistoryComputations.calendarDayCategory(LocalDate.of(2026, 5, 25), startDate, today),
        )
        assertEquals(
            CalendarDayCategory.IN_STREAK,
            HistoryComputations.calendarDayCategory(startDate, startDate, today),
        )
    }

    @Test
    fun `days before the start are pre-streak and after today are future`() {
        assertEquals(
            CalendarDayCategory.PRE_STREAK,
            HistoryComputations.calendarDayCategory(LocalDate.of(2026, 5, 10), startDate, today),
        )
        assertEquals(
            CalendarDayCategory.FUTURE,
            HistoryComputations.calendarDayCategory(LocalDate.of(2026, 5, 31), startDate, today),
        )
    }

    @Test
    fun `sparkline yields one point same-day and N points for an N-day streak`() {
        assertEquals(listOf(0), HistoryComputations.sparklinePoints(0))
        assertEquals(12, HistoryComputations.sparklinePoints(12).size)
        assertEquals(listOf(1, 2, 3), HistoryComputations.sparklinePoints(3))
    }

    @Test
    fun `past streaks use case requests the right page window`() = runTest {
        val repo = mockk<PastStreakRepository>()
        coEvery { repo.getForCounterPaged(any(), any(), any()) } returns emptyList<PastStreakRecord>()
        val sut = GetPastStreaksUseCase(repo)

        sut(counterId = 1L, page = 0)
        sut(counterId = 1L, page = 2)

        coVerify { repo.getForCounterPaged(1L, 50, 0) }
        coVerify { repo.getForCounterPaged(1L, 50, 100) }
    }
}
