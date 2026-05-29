package com.daycounter.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.domain.model.Counter
import com.daycounter.domain.usecase.CalculateStreakUseCase
import com.daycounter.domain.usecase.GetAllCountersUseCase
import com.daycounter.domain.usecase.GetStatsSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** One counter card on the Contadores tab (US2). */
data class CounterCardUi(
    val id: Long,
    val name: String,
    val startDate: LocalDate,
    val streakDays: Int,
    val goalMilestoneTarget: Int,
    val ringFillRatio: Float,
    val goalReached: Boolean,
    val category: String?,
)

/** Global summary header (FR-006). */
data class SummaryUi(
    val totalDays: Int,
    val bestStreak: Int,
)

data class HomeUiState(
    val isLoading: Boolean,
    val counters: List<CounterCardUi>,
    val summary: SummaryUi?,
) {
    val isEmpty: Boolean get() = !isLoading && counters.isEmpty()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAllCounters: GetAllCountersUseCase,
    private val calculateStreak: CalculateStreakUseCase,
    private val getStatsSummary: GetStatsSummaryUseCase,
) : ViewModel() {

    // Pulsed on lifecycle resume so streaks recompute against the current date (Decision 8).
    private val refreshTick = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> =
        combine(getAllCounters(), refreshTick) { counters, _ -> counters.toUiState() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = HomeUiState(isLoading = true, counters = emptyList(), summary = null),
            )

    /** Call from the screen's lifecycle RESUMED to recompute derived streak values. */
    fun onResume() {
        refreshTick.value++
    }

    private fun List<Counter>.toUiState(): HomeUiState {
        val cards = map { it.toCardUi() }
        val summary = if (isEmpty()) {
            null
        } else {
            getStatsSummary(this).let { SummaryUi(totalDays = it.totalAccumulated, bestStreak = it.bestStreak) }
        }
        return HomeUiState(isLoading = false, counters = cards, summary = summary)
    }

    private fun Counter.toCardUi(): CounterCardUi {
        val streak = calculateStreak(startDate)
        val target = goalMilestoneTarget.coerceAtLeast(1)
        return CounterCardUi(
            id = id,
            name = goalName,
            startDate = startDate,
            streakDays = streak,
            goalMilestoneTarget = goalMilestoneTarget,
            ringFillRatio = (streak.toFloat() / target).coerceIn(0f, 1f),
            goalReached = streak >= goalMilestoneTarget,
            category = category,
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
