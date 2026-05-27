package com.daycounter.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.domain.usecase.CalculateStreakUseCase
import com.daycounter.domain.usecase.GetAllCountersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CounterRowState(
    val id: Long,
    val goalName: String,
    val streakDays: Int,
)

data class HomeUiState(
    val isLoading: Boolean,
    val counters: List<CounterRowState>,
) {
    val isEmpty: Boolean get() = !isLoading && counters.isEmpty()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAllCounters: GetAllCountersUseCase,
    calculateStreak: CalculateStreakUseCase,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getAllCounters()
        .map { counters ->
            HomeUiState(
                isLoading = false,
                counters = counters.map {
                    CounterRowState(
                        id = it.id,
                        goalName = it.goalName,
                        streakDays = calculateStreak(it.startDate),
                    )
                },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HomeUiState(isLoading = true, counters = emptyList()),
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
