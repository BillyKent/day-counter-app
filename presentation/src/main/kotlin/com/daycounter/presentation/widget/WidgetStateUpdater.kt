package com.daycounter.presentation.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.daycounter.domain.repository.CounterRepository
import com.daycounter.domain.repository.WidgetBindingRepository
import com.daycounter.domain.usecase.CalculateStreakUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pushes current counter state to a widget's Glance DataStore and asks Glance to
 * re-render. Used by [CounterPickerActivity] after binding selection and by
 * [com.daycounter.data.work.DailyUpdateWorker] each scheduled run.
 */
@Singleton
class WidgetStateUpdater @Inject constructor(
    private val counterRepository: CounterRepository,
    private val widgetBindingRepository: WidgetBindingRepository,
    private val calculateStreak: CalculateStreakUseCase,
) {

    /** Refreshes all widget bindings. Called by DailyUpdateWorker. */
    suspend fun refreshAll(context: Context) {
        val bindings = widgetBindingRepository.getAll()
        bindings.forEach { binding ->
            refresh(context, binding.widgetId, binding.counterId)
        }
    }

    /** Refreshes the widgets bound to one specific counter. */
    suspend fun refreshForCounter(context: Context, counterId: Long) {
        val bindings = widgetBindingRepository.getAllForCounter(counterId)
        bindings.forEach { binding ->
            refresh(context, binding.widgetId, binding.counterId)
        }
    }

    /** Refreshes a single widget. */
    suspend fun refresh(context: Context, widgetId: Int, counterId: Long?) {
        val manager = GlanceAppWidgetManager(context)
        val glanceId = runCatching { manager.getGlanceIdBy(widgetId) }.getOrNull() ?: return

        val newState = if (counterId == null) {
            DayCounterWidgetState(isCounterDeleted = true)
        } else {
            val counter = counterRepository.getById(counterId)
            if (counter == null) {
                widgetBindingRepository.setCounterNull(counterId)
                DayCounterWidgetState(isCounterDeleted = true)
            } else {
                DayCounterWidgetState(
                    counterId = counter.id,
                    goalName = counter.goalName,
                    streakDays = calculateStreak(counter.startDate),
                    isCounterDeleted = false,
                )
            }
        }

        updateAppWidgetState(context, DayCounterWidgetStateDefinition, glanceId) { newState }
        DayCounterWidget().update(context, glanceId)
    }
}
