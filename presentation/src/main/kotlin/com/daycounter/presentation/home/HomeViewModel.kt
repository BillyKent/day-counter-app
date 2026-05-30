package com.daycounter.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.domain.model.Counter
import com.daycounter.domain.model.PausePeriod
import com.daycounter.domain.usecase.CalculateEffectiveStreakUseCase
import com.daycounter.domain.usecase.GetAllCountersUseCase
import com.daycounter.domain.repository.PausePeriodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Live filter over the Contadores list (FR-014). */
enum class CounterFilter { ALL, ACTIVE, PAUSED }

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
    val isPaused: Boolean,
)

/** Global summary header (FR-006), in effective (paused-excluded) days. */
data class SummaryUi(
    val totalDays: Int,
    val bestStreak: Int,
)

/** Live counts shown on the filter chips. */
data class FilterCounts(val all: Int, val active: Int, val paused: Int)

/** Which empty message to show when [HomeUiState.counters] is empty. */
enum class EmptyKind { NONE, NO_COUNTERS, NO_ACTIVE, NO_PAUSED }

data class HomeUiState(
    val isLoading: Boolean,
    val counters: List<CounterCardUi>,
    val summary: SummaryUi?,
    val filter: CounterFilter = CounterFilter.ALL,
    val counts: FilterCounts = FilterCounts(0, 0, 0),
    val emptyKind: EmptyKind = EmptyKind.NONE,
) {
    val isEmpty: Boolean get() = !isLoading && counters.isEmpty()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAllCounters: GetAllCountersUseCase,
    pausePeriodRepository: PausePeriodRepository,
    private val calculateEffectiveStreak: CalculateEffectiveStreakUseCase,
) : ViewModel() {

    // Pulsed on lifecycle resume so streaks recompute against the current date (Decision 8).
    private val refreshTick = MutableStateFlow(0)
    private val filter = MutableStateFlow(CounterFilter.ALL)

    val uiState: StateFlow<HomeUiState> =
        combine(
            getAllCounters(),
            pausePeriodRepository.observeAll(),
            filter,
            refreshTick,
        ) { counters, periods, activeFilter, _ ->
            toUiState(counters, periods, activeFilter)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HomeUiState(isLoading = true, counters = emptyList(), summary = null),
        )

    /** Call from the screen's lifecycle RESUMED to recompute derived streak values. */
    fun onResume() {
        refreshTick.value++
    }

    /** Set the active list filter (FR-014). */
    fun setFilter(value: CounterFilter) {
        filter.value = value
    }

    private fun toUiState(
        counters: List<Counter>,
        periods: List<PausePeriod>,
        activeFilter: CounterFilter,
    ): HomeUiState {
        val pausedDaysByCounter = periods.groupBy { it.counterId }
            .mapValues { (_, list) -> list.sumOf { it.days } }

        val allCards = counters.map { it.toCardUi(pausedDaysByCounter[it.id] ?: 0) }
        val counts = FilterCounts(
            all = allCards.size,
            active = allCards.count { !it.isPaused },
            paused = allCards.count { it.isPaused },
        )
        val visible = when (activeFilter) {
            CounterFilter.ALL -> allCards
            CounterFilter.ACTIVE -> allCards.filter { !it.isPaused }
            CounterFilter.PAUSED -> allCards.filter { it.isPaused }
        }
        val summary = if (allCards.isEmpty()) {
            null
        } else {
            // Effective totals keep Contadores and Estadísticas consistent (SC-008).
            SummaryUi(
                totalDays = allCards.sumOf { it.streakDays },
                bestStreak = allCards.maxOf { it.streakDays },
            )
        }
        val emptyKind = when {
            allCards.isEmpty() -> EmptyKind.NO_COUNTERS
            visible.isEmpty() && activeFilter == CounterFilter.PAUSED -> EmptyKind.NO_PAUSED
            visible.isEmpty() && activeFilter == CounterFilter.ACTIVE -> EmptyKind.NO_ACTIVE
            else -> EmptyKind.NONE
        }
        return HomeUiState(
            isLoading = false,
            counters = visible,
            summary = summary,
            filter = activeFilter,
            counts = counts,
            emptyKind = emptyKind,
        )
    }

    private fun Counter.toCardUi(completedPausedDays: Int): CounterCardUi {
        val streak = calculateEffectiveStreak(this, completedPausedDays)
        val target = goalMilestoneTarget.coerceAtLeast(1)
        return CounterCardUi(
            id = id,
            name = goalName,
            startDate = startDate,
            streakDays = streak,
            goalMilestoneTarget = goalMilestoneTarget,
            ringFillRatio = (streak.toFloat() / target).coerceIn(0f, 1f),
            goalReached = !isPaused && streak >= goalMilestoneTarget,
            category = category,
            isPaused = isPaused,
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
