package com.daycounter.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.domain.model.PauseStats
import com.daycounter.domain.model.WeeklyActivity
import com.daycounter.domain.repository.PausePeriodRepository
import com.daycounter.domain.usecase.GetAllCountersUseCase
import com.daycounter.domain.usecase.GetPauseStatsUseCase
import com.daycounter.domain.usecase.GetStatsSummaryUseCase
import com.daycounter.domain.usecase.GetWeeklyActivityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false,
    val totalAccumulated: Int = 0,
    val bestStreak: Int = 0,
    val activeCounters: Int = 0,
    val milestonesReached: Int = 0,
    val averageStreak: Int = 0,
    val pauseStats: PauseStats = PauseStats.EMPTY,
    val weekly: WeeklyActivity = WeeklyActivity.EMPTY,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    getAllCounters: GetAllCountersUseCase,
    pausePeriodRepository: PausePeriodRepository,
    private val getStatsSummary: GetStatsSummaryUseCase,
    private val getPauseStats: GetPauseStatsUseCase,
    private val getWeeklyActivity: GetWeeklyActivityUseCase,
) : ViewModel() {

    private val refreshTick = MutableStateFlow(0)

    val uiState: StateFlow<StatsUiState> =
        combine(
            getAllCounters(),
            pausePeriodRepository.observeAll(),
            refreshTick,
        ) { counters, periods, _ ->
            val pausedDaysByCounter = periods.groupBy { it.counterId }
                .mapValues { (_, list) -> list.sumOf { it.days } }
            val summary = getStatsSummary(counters, pausedDaysByCounter)
            StatsUiState(
                isLoading = false,
                isEmpty = counters.isEmpty(),
                totalAccumulated = summary.totalAccumulated,
                bestStreak = summary.bestStreak,
                activeCounters = summary.activeCounters,
                milestonesReached = summary.milestonesReached,
                averageStreak = summary.averageStreak,
                pauseStats = getPauseStats(counters, periods),
                weekly = getWeeklyActivity(counters, periods),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = StatsUiState(),
        )

    /** Recompute on lifecycle resume (Decision 8). */
    fun onResume() {
        refreshTick.value++
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
