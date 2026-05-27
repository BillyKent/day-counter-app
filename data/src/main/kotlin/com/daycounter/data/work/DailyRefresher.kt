package com.daycounter.data.work

import android.content.Context
import com.daycounter.domain.repository.CounterRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * Encapsulates the refresh logic so [DailyUpdateWorker] stays thin and so the work
 * can be unit-tested without WorkManager. Notifier and widget updater are injected
 * as plain interfaces; concrete bindings live in :app / :presentation respectively.
 */
@Singleton
class DailyRefresher @Inject constructor(
    private val counterRepository: CounterRepository,
    private val milestoneNotifier: MilestoneNotifier,
    private val widgetRefresher: WidgetRefresher,
) {
    suspend fun refresh(context: Context) {
        val counters = counterRepository.getAllSortedByStreak().first()
        counters.forEach { counter ->
            milestoneNotifier.evaluateAndNotify(context, counter)
        }
        widgetRefresher.refreshAll(context)
    }
}

interface MilestoneNotifier {
    suspend fun evaluateAndNotify(context: Context, counter: com.daycounter.domain.model.Counter)
}

interface WidgetRefresher {
    suspend fun refreshAll(context: Context)
}
