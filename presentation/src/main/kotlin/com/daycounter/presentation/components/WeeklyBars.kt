package com.daycounter.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.daycounter.domain.model.WeeklyActivity
import java.time.format.TextStyle
import java.util.Locale

/**
 * 7-bar weekly "días cumplidos" chart (FR-028). Today's bar is emphasized with the brand color.
 * Day labels are the locale's narrow weekday names so they follow the in-app language (US3).
 */
@Composable
fun WeeklyBars(activity: WeeklyActivity, modifier: Modifier = Modifier) {
    val max = (activity.days.maxOfOrNull { it.fulfilled } ?: 0).coerceAtLeast(1)
    Row(
        modifier = modifier.fillMaxWidth().height(132.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        activity.days.forEachIndexed { index, bar ->
            val isToday = index == activity.todayIndex
            val barColor = if (isToday) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
            val barHeight = (24 + (bar.fulfilled.toFloat() / max * 84f)).dp
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = bar.fulfilled.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight)
                        .background(barColor, RoundedCornerShape(10.dp)),
                ) {}
                Text(
                    text = bar.date.dayOfWeek
                        .getDisplayName(TextStyle.NARROW, Locale.getDefault())
                        .uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
