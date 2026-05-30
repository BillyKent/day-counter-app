package com.daycounter.data.work

import com.daycounter.domain.model.ReminderTime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset

class DailyReminderSchedulerTest {

    private val zone = ZoneOffset.UTC

    private fun schedulerNowAt(dateTime: LocalDateTime): DailyReminderScheduler {
        val clock = Clock.fixed(dateTime.toInstant(zone), zone)
        return DailyReminderScheduler(clock, zone)
    }

    @Test
    fun `delay to a time later today`() {
        // now 08:00, reminder 09:00 → 1 hour.
        val sut = schedulerNowAt(LocalDateTime.of(2026, 5, 29, 8, 0))
        assertEquals(60L * 60 * 1000, sut.nextDelayMillis(ReminderTime(9, 0)))
    }

    @Test
    fun `delay rolls to tomorrow when the time already passed today`() {
        // now 10:00, reminder 09:00 → 23 hours.
        val sut = schedulerNowAt(LocalDateTime.of(2026, 5, 29, 10, 0))
        assertEquals(23L * 60 * 60 * 1000, sut.nextDelayMillis(ReminderTime(9, 0)))
    }

    @Test
    fun `delay rolls to tomorrow when exactly now`() {
        // now == reminder → next is tomorrow (24h), never 0.
        val sut = schedulerNowAt(LocalDateTime.of(2026, 5, 29, 9, 0))
        assertEquals(24L * 60 * 60 * 1000, sut.nextDelayMillis(ReminderTime(9, 0)))
    }
}
