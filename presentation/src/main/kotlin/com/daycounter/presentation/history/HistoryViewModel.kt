package com.daycounter.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.domain.usecase.CalculateStreakUseCase
import com.daycounter.domain.usecase.CalendarDayCategory
import com.daycounter.domain.usecase.GetCounterByIdUseCase
import com.daycounter.domain.usecase.GetPastStreaksUseCase
import com.daycounter.domain.usecase.HistoryComputations
import com.daycounter.presentation.components.CalendarCellState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One rendered calendar day (presentation state). */
data class CalendarCell(val dayOfMonth: Int, val state: CalendarCellState)

/** One archived streak row. */
data class PastStreakUi(val streakDays: Int, val reason: String, val endDate: LocalDate)

data class HistoryUiState(
    val isLoading: Boolean = true,
    val counterName: String = "",
    val currentStreak: Int = 0,
    val sparklinePoints: List<Int> = emptyList(),
    val leadingBlanks: Int = 0,
    val calendarCells: List<CalendarCell> = emptyList(),
    val pastStreaks: List<PastStreakUi> = emptyList(),
    val canLoadMore: Boolean = false,
)

@HiltViewModel(assistedFactory = HistoryViewModel.Factory::class)
class HistoryViewModel @AssistedInject constructor(
    @Assisted private val counterId: Long,
    private val getCounter: GetCounterByIdUseCase,
    private val calculateStreak: CalculateStreakUseCase,
    private val getPastStreaks: GetPastStreaksUseCase,
    private val clock: Clock,
    private val zone: ZoneId,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(counterId: Long): HistoryViewModel
    }

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    private var loadedPage = -1
    private val pastStreaks = mutableListOf<PastStreakUi>()

    init {
        loadHeaderAndCalendar()
        loadNextPastStreaksPage()
    }

    /** Recompute the streak-derived header/calendar on resume (Decision 8). */
    fun onResume() = loadHeaderAndCalendar()

    private fun loadHeaderAndCalendar() {
        viewModelScope.launch {
            val counter = getCounter(counterId) ?: return@launch
            val today = LocalDate.now(clock.withZone(zone))
            val streak = calculateStreak(counter.startDate)
            val firstOfMonth = today.withDayOfMonth(1)
            val leadingBlanks = firstOfMonth.dayOfWeek.value - 1 // Monday-first grid (MON=0 … SUN=6)
            val cells = (1..today.lengthOfMonth()).map { day ->
                val date = firstOfMonth.withDayOfMonth(day)
                CalendarCell(
                    dayOfMonth = day,
                    state = HistoryComputations.calendarDayCategory(date, counter.startDate, today).toCellState(),
                )
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    counterName = counter.goalName,
                    currentStreak = streak,
                    sparklinePoints = HistoryComputations.sparklinePoints(streak),
                    leadingBlanks = leadingBlanks,
                    calendarCells = cells,
                )
            }
        }
    }

    fun loadNextPastStreaksPage() {
        viewModelScope.launch {
            val nextPage = loadedPage + 1
            val batch = getPastStreaks(counterId, nextPage)
            loadedPage = nextPage
            pastStreaks += batch.map { PastStreakUi(it.streakDays, it.reason, it.endDate) }
            _state.update {
                it.copy(
                    pastStreaks = pastStreaks.toList(),
                    canLoadMore = batch.size == GetPastStreaksUseCase.PAGE_SIZE,
                )
            }
        }
    }

    private fun CalendarDayCategory.toCellState(): CalendarCellState = when (this) {
        CalendarDayCategory.IN_STREAK -> CalendarCellState.InStreak
        CalendarDayCategory.TODAY -> CalendarCellState.Today
        CalendarDayCategory.PRE_STREAK -> CalendarCellState.PreStreak
        CalendarDayCategory.FUTURE -> CalendarCellState.Future
    }
}
