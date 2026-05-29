package com.daycounter.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.domain.usecase.GetAllCountersUseCase
import com.daycounter.domain.usecase.GetStatsSummaryUseCase
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
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    getAllCounters: GetAllCountersUseCase,
    private val getStatsSummary: GetStatsSummaryUseCase,
) : ViewModel() {

    private val refreshTick = MutableStateFlow(0)

    val uiState: StateFlow<StatsUiState> =
        combine(getAllCounters(), refreshTick) { counters, _ ->
            val summary = getStatsSummary(counters)
            StatsUiState(
                isLoading = false,
                isEmpty = counters.isEmpty(),
                totalAccumulated = summary.totalAccumulated,
                bestStreak = summary.bestStreak,
                activeCounters = summary.activeCounters,
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
