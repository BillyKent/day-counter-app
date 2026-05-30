package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class GetStatsSummaryUseCaseTest {

    private val zone = ZoneOffset.UTC
    private val clock = Clock.fixed(Instant.parse("2026-05-29T00:00:00Z"), zone)
    private val sut = GetStatsSummaryUseCase(CalculateEffectiveStreakUseCase(clock, zone))

    private fun counter(id: Long, daysAgo: Long) = Counter(
        id = id,
        goalName = "C$id",
        startDate = LocalDate.of(2026, 5, 29).minusDays(daysAgo),
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    )

    @Test
    fun `empty counters yields all zeros`() {
        val summary = sut(emptyList())
        assertEquals(0, summary.totalAccumulated)
        assertEquals(0, summary.bestStreak)
        assertEquals(0, summary.activeCounters)
    }

    @Test
    fun `total is sum, best is max, active is count`() {
        // streaks 5, 30, 120
        val summary = sut(listOf(counter(1, 5), counter(2, 30), counter(3, 120)))
        assertEquals(155, summary.totalAccumulated)
        assertEquals(120, summary.bestStreak)
        assertEquals(3, summary.activeCounters)
        // milestones reached: 5→{1}=1, 30→{1,7,30}=3, 120→{1,7,30,100}=4 ⇒ 8
        assertEquals(8, summary.milestonesReached)
        // average 155/3 ≈ 52
        assertEquals(52, summary.averageStreak)
    }

    @Test
    fun `paused counters are excluded from active count`() {
        val active = counter(1, 10)
        val paused = counter(2, 40).copy(
            status = com.daycounter.domain.model.CounterStatus.PAUSED,
            pausedSince = LocalDate.of(2026, 5, 29).minusDays(5),
        )
        val summary = sut(listOf(active, paused))
        assertEquals(1, summary.activeCounters)
    }

    @Test
    fun `single counter best equals its streak`() {
        val summary = sut(listOf(counter(1, 7)))
        assertEquals(7, summary.totalAccumulated)
        assertEquals(7, summary.bestStreak)
        assertEquals(1, summary.activeCounters)
    }
}
