package com.daycounter.presentation.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.daycounter.domain.repository.WidgetBindingRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DayCounterWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = DayCounterWidget()

    @Inject
    lateinit var widgetBindingRepository: WidgetBindingRepository

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            appWidgetIds.forEach { widgetBindingRepository.delete(it) }
        }
    }
}
