package com.daycounter.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Periodic worker that runs once per local day to:
 * 1. Recalculate streaks and post milestone notifications via [MilestoneNotifier].
 * 2. Push fresh state to every bound widget via [com.daycounter.presentation.widget.WidgetStateUpdater].
 *
 * The worker is enqueued with `ExistingPeriodicWorkPolicy.KEEP` so app launches and boot do not
 * create duplicate work entries.
 */
@HiltWorker
class DailyUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val refresher: DailyRefresher,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        refresher.refresh(applicationContext)
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    companion object {
        const val UNIQUE_NAME = "day_counter_daily_update"

        fun enqueue(context: Context, clock: Clock = Clock.systemDefaultZone(), zone: ZoneId = ZoneId.systemDefault()) {
            val initialDelay = millisUntilNextMidnight(clock, zone)
            val request = PeriodicWorkRequestBuilder<DailyUpdateWorker>(
                repeatInterval = 1, repeatIntervalTimeUnit = TimeUnit.DAYS,
                flexTimeInterval = 15, flexTimeIntervalUnit = TimeUnit.MINUTES,
            )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        private fun millisUntilNextMidnight(clock: Clock, zone: ZoneId): Long {
            val now = LocalDateTime.now(clock.withZone(zone))
            val nextMidnight = LocalDate.now(clock.withZone(zone)).plusDays(1).atTime(LocalTime.MIDNIGHT)
            return Duration.between(now, nextMidnight).toMillis().coerceAtLeast(0L)
        }
    }
}
