package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import com.daycounter.domain.model.CounterStatus
import com.daycounter.domain.model.PausePeriod
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class PauseStatsAndWeeklyActivityTest {

    private val zone = ZoneOffset.UTC
    private val today = LocalDate.of(2026, 5, 29)
    private val clock = Clock.fixed(today.atStartOfDay(zone).toInstant(), zone)
    private val createdAt = Instant.parse("2026-01-01T00:00:00Z")

    private fun counter(id: Long, startDaysAgo: Long, paused: Boolean = false, pausedDaysAgo: Long = 0) =
        Counter(
            id = id,
            goalName = "C$id",
            startDate = today.minusDays(startDaysAgo),
            createdAt = createdAt,
            status = if (paused) CounterStatus.PAUSED else CounterStatus.ACTIVE,
            pausedSince = if (paused) today.minusDays(pausedDaysAgo) else null,
        )

    // ----- GetPauseStatsUseCase -----

    @Test
    fun `pause stats combine completed and ongoing pauses`() {
        val sut = GetPauseStatsUseCase(clock, zone)
        val counters = listOf(counter(1, 10), counter(2, 30, paused = true, pausedDaysAgo = 5))
        val periods = listOf(PausePeriod(counterId = 1, startDate = today.minusDays(20), endDate = today.minusDays(10)))

        val stats = sut(counters, periods)
        assertEquals(1, stats.pausedNow)
        assertEquals(10 + 5, stats.totalPausedDays) // completed 10 + ongoing 5
        assertEquals(2, stats.totalPauses) // 1 completed + 1 currently paused
    }

    // ----- GetWeeklyActivityUseCase -----

    @Test
    fun `single active counter is fulfilled every day of the week`() {
        val sut = GetWeeklyActivityUseCase(clock, zone)
        val week = sut(listOf(counter(1, 10)), emptyList())
        assertEquals(7, week.days.size)
        assertEquals(6, week.todayIndex)
        assertEquals(7, week.weekTotal)
    }

    @Test
    fun `paused counter only counts on days before it was paused`() {
        val sut = GetWeeklyActivityUseCase(clock, zone)
        // active (all 7 days) + paused since today-2 (counts on today-6..today-3 = 4 days)
        val counters = listOf(counter(1, 10), counter(2, 20, paused = true, pausedDaysAgo = 2))
        val week = sut(counters, emptyList())
        assertEquals(7, week.days.size)
        // active contributes 7; paused contributes 4 ⇒ 11
        assertEquals(11, week.weekTotal)
    }
}
