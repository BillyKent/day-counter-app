package com.daycounter.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.daycounter.data.work.DailyUpdateWorker

/**
 * On boot, ensures the daily refresh worker is enqueued (no-op via
 * [androidx.work.ExistingPeriodicWorkPolicy.KEEP] if WorkManager already has it).
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            DailyUpdateWorker.enqueue(context.applicationContext)
        }
    }
}
