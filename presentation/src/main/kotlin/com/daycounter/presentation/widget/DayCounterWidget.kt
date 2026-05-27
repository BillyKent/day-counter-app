package com.daycounter.presentation.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.daycounter.presentation.MainActivity
import com.daycounter.presentation.R

/**
 * Day Counter Glance AppWidget. Renders two sizes (2×1 compact, 4×2 medium) by
 * branching on [LocalSize.current] inside [Content].
 */
class DayCounterWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<DayCounterWidgetState> = DayCounterWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Content()
            }
        }
    }

    @Composable
    private fun Content() {
        val context = LocalContext.current
        val state = currentState<DayCounterWidgetState>()
        val size = LocalSize.current
        val isMedium = size.width >= 180.dp && size.height >= 80.dp

        if (state.isCounterDeleted || state.counterId == null) {
            DeletedPlaceholder(context, isMedium)
            return
        }

        val openIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("daycounter://counter/${state.counterId}"),
            context,
            MainActivity::class.java,
        )

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(8.dp)
                .clickable(actionStartActivity(openIntent)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.streakDays.toString(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isMedium) 36.sp else 20.sp,
                    ),
                )
                if (isMedium) {
                    Text(
                        text = context.getString(R.string.widget_days_label),
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                    )
                }
                Text(
                    text = state.goalName,
                    maxLines = if (isMedium) 2 else 1,
                    style = TextStyle(color = GlanceTheme.colors.onSurface),
                )
            }
        }
    }

    @Composable
    private fun DeletedPlaceholder(context: Context, isMedium: Boolean) {
        val pickerIntent = Intent(context, CounterPickerActivity::class.java)
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(8.dp)
                .clickable(actionStartActivity(pickerIntent)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = context.getString(R.string.widget_counter_removed),
                    style = TextStyle(color = GlanceTheme.colors.onSurface),
                )
                if (isMedium) {
                    Text(
                        text = context.getString(R.string.widget_select_counter),
                        style = TextStyle(color = GlanceTheme.colors.primary),
                    )
                }
            }
        }
    }
}
