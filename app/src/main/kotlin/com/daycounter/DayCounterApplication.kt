package com.daycounter

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.daycounter.data.work.DailyUpdateWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Day Counter application entry point.
 * Provides the Hilt component root and registers the milestone notification channel.
 */
@HiltAndroidApp
class DayCounterApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        registerMilestoneNotificationChannel()
        DailyUpdateWorker.enqueue(this)
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

    companion object {
        const val MILESTONE_CHANNEL_ID = "milestone_notifications"
    }
}
