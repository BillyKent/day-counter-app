package com.daycounter.data.work

import android.content.Context
import com.daycounter.domain.model.Counter
import com.daycounter.domain.repository.CounterRepository
import io.mockk.coVerify
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class DailyRefresherTest {

    private val context: Context = mockk(relaxed = true)

    @Test
    fun `notifies each counter and refreshes all widgets`() = runTest {
        val counters = listOf(
            Counter(1L, "Run", LocalDate.of(2026, 5, 1), Instant.parse("2026-05-01T00:00:00Z")),
            Counter(2L, "Read", LocalDate.of(2026, 5, 20), Instant.parse("2026-05-20T00:00:00Z")),
        )
        val counterRepository = mockk<CounterRepository>()
        coEvery { counterRepository.getAllSortedByStreak() } returns flowOf(counters)
        val notifier = mockk<MilestoneNotifier>(relaxed = true)
        val widgetRefresher = mockk<WidgetRefresher>(relaxed = true)

        val sut = DailyRefresher(counterRepository, notifier, widgetRefresher)
        sut.refresh(context)

        counters.forEach { counter ->
            coVerify(exactly = 1) { notifier.evaluateAndNotify(context, counter) }
        }
        coVerify(exactly = 1) { widgetRefresher.refreshAll(context) }
    }
}
