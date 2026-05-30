package com.daycounter.domain.usecase

import com.daycounter.domain.model.Counter
import com.daycounter.domain.model.CounterStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class CalculateEffectiveStreakUseCaseTest {

    private val zone: ZoneId = ZoneOffset.UTC
    private val createdAt: Instant = Instant.parse("2026-01-01T00:00:00Z")

    private fun useCaseFor(today: LocalDate): CalculateEffectiveStreakUseCase {
        val clock = Clock.fixed(today.atStartOfDay(zone).toInstant(), zone)
        return CalculateEffectiveStreakUseCase(clock, zone)
    }

    private fun counter(
        start: LocalDate,
        status: CounterStatus = CounterStatus.ACTIVE,
        pausedSince: LocalDate? = null,
    ) = Counter(
        id = 1L,
        goalName = "Test",
        startDate = start,
        createdAt = createdAt,
        status = status,
        pausedSince = pausedSince,
    )

    @Test
    fun `same day returns 0`() {
        val today = LocalDate.of(2026, 1, 1)
        val sut = useCaseFor(today)
        assertEquals(0, sut(counter(start = today), completedPausedDays = 0))
    }

    @Test
    fun `active counter with no pauses equals elapsed days`() {
        val sut = useCaseFor(LocalDate.of(2026, 1, 20))
        assertEquals(19, sut(counter(start = LocalDate.of(2026, 1, 1)), completedPausedDays = 0))
    }

    @Test
    fun `handoff example - start 1 Jan pause 10 resume 20 yields 9`() {
        // After resume: active, today = 20 Jan, one completed pause 10->20 = 10 days.
        val sut = useCaseFor(LocalDate.of(2026, 1, 20))
        val c = counter(start = LocalDate.of(2026, 1, 1))
        assertEquals(9, sut(c, completedPausedDays = 10))
    }

    @Test
    fun `paused counter freezes at pausedSince across a date rollover`() {
        val start = LocalDate.of(2026, 1, 1)
        val pausedSince = LocalDate.of(2026, 1, 10)
        val c = counter(start = start, status = CounterStatus.PAUSED, pausedSince = pausedSince)
        // Today advances well past pausedSince; effective stays frozen at 9 (1->10), no completed pauses yet.
        assertEquals(9, useCaseFor(LocalDate.of(2026, 1, 15))(c, completedPausedDays = 0))
        assertEquals(9, useCaseFor(LocalDate.of(2026, 2, 1))(c, completedPausedDays = 0))
    }

    @Test
    fun `multiple completed pauses are all excluded`() {
        val sut = useCaseFor(LocalDate.of(2026, 2, 1)) // 31 days elapsed from 1 Jan
        val c = counter(start = LocalDate.of(2026, 1, 1))
        // Two pauses totalling 5 + 3 = 8 days.
        assertEquals(23, sut(c, completedPausedDays = 8))
    }

    @Test
    fun `pause on day zero stays at 0`() {
        val today = LocalDate.of(2026, 1, 1)
        val c = counter(start = today, status = CounterStatus.PAUSED, pausedSince = today)
        assertEquals(0, useCaseFor(today)(c, completedPausedDays = 0))
    }

    @Test
    fun `never returns negative when paused days exceed elapsed`() {
        val sut = useCaseFor(LocalDate.of(2026, 1, 5))
        val c = counter(start = LocalDate.of(2026, 1, 1))
        assertEquals(0, sut(c, completedPausedDays = 99))
    }
}
