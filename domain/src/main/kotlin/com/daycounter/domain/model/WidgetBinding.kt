package com.daycounter.domain.model

/**
 * Maps a system-assigned App Widget ID to the counter it displays.
 *
 * @property widgetId The system-assigned App Widget ID (primary key).
 * @property counterId The bound counter's id; nullable. Null indicates the counter
 *  was deleted; the widget renders a "Counter removed" placeholder.
 */
data class WidgetBinding(
    val widgetId: Int,
    val counterId: Long?,
)
