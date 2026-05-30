package com.daycounter

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.daycounter.data.work.DailyReminderScheduler
import com.daycounter.data.work.DailyUpdateWorker
import com.daycounter.domain.repository.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Day Counter application entry point.
 * Provides the Hilt component root and registers the milestone notification channel.
 */
@HiltAndroidApp
class DayCounterApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var dailyReminderScheduler: DailyReminderScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        registerMilestoneNotificationChannel()
        registerDailyReminderChannel()
        DailyUpdateWorker.enqueue(this)
        // Re-arm the daily reminder to match the persisted preference (idempotent unique work).
        runBlocking {
            dailyReminderScheduler.schedule(
                context = this@DayCounterApplication,
                enabled = settingsRepository.dailyReminderEnabled.first(),
                time = settingsRepository.reminderTime.first(),
            )
        }
    }

    private fun registerMilestoneNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            MILESTONE_CHANNEL_ID,
            getString(com.daycounter.presentation.R.string.notification_channel_milestones),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(com.daycounter.presentation.R.string.notification_channel_milestones_description)
        }
        manager.createNotificationChannel(channel)
    }

    private fun registerDailyReminderChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            DAILY_REMINDER_CHANNEL_ID,
            getString(com.daycounter.presentation.R.string.notification_channel_daily_reminder),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(com.daycounter.presentation.R.string.notification_channel_daily_reminder_description)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val MILESTONE_CHANNEL_ID = "milestone_notifications"
        const val DAILY_REMINDER_CHANNEL_ID = "daily_reminder"
    }
}
