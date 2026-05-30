package com.daycounter.data.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.daycounter.domain.model.ReminderTime
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the daily reminder via a self-rescheduling one-time [DailyReminderWorker] at the chosen
 * time-of-day (research R6). Uses unique work so app start / boot / time changes don't duplicate it.
 */
@Singleton
class DailyReminderScheduler @Inject constructor(
    private val clock: Clock,
    private val zone: ZoneId,
) {
    /** Enqueue (if [enabled]) the next reminder at [time], replacing any existing schedule; else cancel. */
    fun schedule(context: Context, enabled: Boolean, time: ReminderTime) {
        val wm = WorkManager.getInstance(context)
        if (!enabled) {
            wm.cancelUniqueWork(UNIQUE_NAME)
            return
        }
        val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(nextDelayMillis(time), TimeUnit.MILLISECONDS)
            .build()
        wm.enqueueUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    /** Milliseconds until the next occurrence of [time] (today if still ahead, else tomorrow). */
    fun nextDelayMillis(time: ReminderTime): Long {
        val now = LocalDateTime.now(clock.withZone(zone))
        var target = now.toLocalDate().atTime(time.hour, time.minute)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return Duration.between(now, target).toMillis().coerceAtLeast(0L)
    }

    companion object {
        const val UNIQUE_NAME = "day_counter_daily_reminder"
    }
}
