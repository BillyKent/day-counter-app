package com.daycounter.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.daycounter.domain.repository.CounterRepository
import com.daycounter.domain.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Posts the daily reminder notification at the user's chosen time, then re-arms itself for the next
 * day (research R6). No-op (and no reschedule) when the reminder is disabled. Honors the OS
 * notification permission inside [DailyReminderNotifier].
 */
@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val counterRepository: CounterRepository,
    private val notifier: DailyReminderNotifier,
    private val scheduler: DailyReminderScheduler,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        if (settingsRepository.dailyReminderEnabled.first()) {
            val activeCount = counterRepository.getAllSortedByStreak().first().count { !it.isPaused }
            notifier.notifyDailyReminder(applicationContext, activeCount)
            // Re-arm for tomorrow at the (possibly updated) time.
            scheduler.schedule(
                context = applicationContext,
                enabled = true,
                time = settingsRepository.reminderTime.first(),
            )
        }
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }
}

/** Posts the daily reminder notification. Implemented in `:app` (channel + strings + permission). */
interface DailyReminderNotifier {
    suspend fun notifyDailyReminder(context: Context, activeCounters: Int)
}
