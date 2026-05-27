package com.daycounter.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class CalculateStreakUseCaseTest {

    private fun useCaseFor(today: LocalDate, zone: ZoneId = ZoneOffset.UTC): CalculateStreakUseCase {
        val clock = Clock.fixed(today.atStartOfDay(zone).toInstant(), zone)
        return CalculateStreakUseCase(clock, zone)
    }

    @Test
    fun `startDate equals today returns 0`() {
        val today = LocalDate.of(2026, 5, 27)
        val sut = useCaseFor(today)
        assertEquals(0, sut(today))
    }

    @Test
    fun `startDate yesterday returns 1`() {
        val today = LocalDate.of(2026, 5, 27)
        val sut = useCaseFor(today)
        assertEquals(1, sut(today.minusDays(1)))
    }

    @Test
    fun `startDate seven days ago returns 7`() {
        val today = LocalDate.of(2026, 5, 27)
        val sut = useCaseFor(today)
        assertEquals(7, sut(today.minusDays(7)))
    }

    @Test
    fun `startDate one year ago returns 365`() {
        val today = LocalDate.of(2026, 5, 27)
        val sut = useCaseFor(today)
        assertEquals(365, sut(today.minusDays(365)))
    }

    @Test
    fun `clock at midnight in a non-UTC zone uses local date`() {
        val zone = ZoneId.of("America/Los_Angeles")
        val localToday = LocalDate.of(2026, 5, 27)
        val clock = Clock.fixed(Instant.parse("2026-05-27T09:30:00Z"), zone)
        val sut = CalculateStreakUseCase(clock, zone)
        assertEquals(0, sut(localToday))
    }
}
