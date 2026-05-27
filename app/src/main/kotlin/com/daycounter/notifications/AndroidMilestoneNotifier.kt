package com.daycounter.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daycounter.DayCounterApplication
import com.daycounter.data.datastore.NotificationPreferencesDataStore
import com.daycounter.data.work.MilestoneNotifier
import com.daycounter.domain.model.Counter
import com.daycounter.domain.model.MilestoneRecord
import com.daycounter.domain.repository.MilestoneRepository
import com.daycounter.domain.usecase.CalculateStreakUseCase
import com.daycounter.domain.usecase.CheckMilestonesUseCase
import com.daycounter.presentation.MainActivity
import com.daycounter.presentation.R
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Posts milestone notifications after deduplicating via the MilestoneRepository
 * INSERT OR IGNORE pattern. No notification is posted if:
 * - the streak is not a milestone, or
 * - a record already exists (duplicate), or
 * - the in-app toggle is off, or
 * - the OS permission has not been granted.
 */
@Singleton
class AndroidMilestoneNotifier @Inject constructor(
    private val checkMilestones: CheckMilestonesUseCase,
    private val calculateStreak: CalculateStreakUseCase,
    private val milestoneRepository: MilestoneRepository,
    private val prefs: NotificationPreferencesDataStore,
) : MilestoneNotifier {

    override suspend fun evaluateAndNotify(context: Context, counter: Counter) {
        val streak = calculateStreak(counter.startDate)
        val milestone = checkMilestones(streak) ?: return

        val newRowId = milestoneRepository.insertOrIgnore(
            MilestoneRecord(
                counterId = counter.id,
                milestoneDays = milestone,
                notifiedAt = Instant.now(),
            ),
        )
        if (newRowId == -1L) return // duplicate; already notified
        if (!prefs.isNotificationsEnabled()) return
        if (!hasPostPermission(context)) return

        post(context, counter, milestone)
    }

    private fun hasPostPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun post(context: Context, counter: Counter, milestone: Int) {
        val openIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("daycounter://counter/${counter.id}"),
            context,
            MainActivity::class.java,
        )
        val pendingIntent = PendingIntent.getActivity(
            context,
            counter.id.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, DayCounterApplication.MILESTONE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(
                context.getString(R.string.notification_milestone_title, counter.goalName, milestone),
            )
            .setContentText(context.getString(R.string.notification_milestone_text, milestone))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(counter.id.toInt(), notification)
    }
}
