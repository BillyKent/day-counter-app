package com.daycounter.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.daycounter.data.work.DailyReminderScheduler
import com.daycounter.data.work.DailyUpdateWorker
import com.daycounter.domain.repository.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * On boot, re-arms background work: the daily refresh worker (no-op via `KEEP` if already enqueued)
 * and the daily reminder schedule (FR-025 / T109), since the self-rescheduling one-time reminder
 * work does not survive a reboot on its own.
 */
class BootReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootEntryPoint {
        fun settingsRepository(): SettingsRepository
        fun dailyReminderScheduler(): DailyReminderScheduler
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_LOCKED_BOOT_COMPLETED) return

        val app = context.applicationContext
        DailyUpdateWorker.enqueue(app)

        val entryPoint = EntryPointAccessors.fromApplication(app, BootEntryPoint::class.java)
        runBlocking {
            entryPoint.dailyReminderScheduler().schedule(
                context = app,
                enabled = entryPoint.settingsRepository().dailyReminderEnabled.first(),
                time = entryPoint.settingsRepository().reminderTime.first(),
            )
        }
    }
}
