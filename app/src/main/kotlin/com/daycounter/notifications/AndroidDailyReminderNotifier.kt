package com.daycounter.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daycounter.DayCounterApplication
import com.daycounter.data.work.DailyReminderNotifier
import com.daycounter.presentation.MainActivity
import com.daycounter.presentation.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Posts the daily reminder on the dedicated channel. No-op if the OS notification permission has not
 * been granted (the in-app toggle is checked by [com.daycounter.data.work.DailyReminderWorker]).
 */
@Singleton
class AndroidDailyReminderNotifier @Inject constructor() : DailyReminderNotifier {

    // Permission is verified by hasPostPermission() below before notify(); lint's intraprocedural
    // analysis can't see the helper, so the guarded notify() is safe to suppress (Principle V).
    @SuppressLint("MissingPermission")
    override suspend fun notifyDailyReminder(context: Context, activeCounters: Int) {
        if (!hasPostPermission(context)) return

        val openIntent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, DayCounterApplication.DAILY_REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.notification_daily_reminder_title))
            .setContentText(context.getString(R.string.notification_daily_reminder_text, activeCounters))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun hasPostPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        const val NOTIFICATION_ID = 424_242
        const val REQUEST_CODE = 424_242
    }
}
