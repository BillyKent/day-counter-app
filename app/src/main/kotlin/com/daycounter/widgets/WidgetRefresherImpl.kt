package com.daycounter.widgets

import android.content.Context
import com.daycounter.data.work.WidgetRefresher
import com.daycounter.presentation.widget.WidgetStateUpdater
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRefresherImpl @Inject constructor(
    private val widgetStateUpdater: WidgetStateUpdater,
) : WidgetRefresher {
    override suspend fun refreshAll(context: Context) {
        widgetStateUpdater.refreshAll(context)
    }
}
