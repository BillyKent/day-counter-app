package com.daycounter.presentation.counter

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.domain.model.MilestoneRecord
import com.daycounter.domain.repository.MilestoneRepository
import com.daycounter.domain.usecase.CalculateStreakUseCase
import com.daycounter.domain.usecase.DeleteCounterUseCase
import com.daycounter.domain.usecase.GetAchievedMilestonesUseCase
import com.daycounter.domain.usecase.GetCounterByIdUseCase
import com.daycounter.domain.usecase.GetMostRecentMilestoneUseCase
import com.daycounter.domain.usecase.GetNextMilestoneUseCase
import com.daycounter.domain.usecase.MarkCelebrationsShownUseCase
import com.daycounter.presentation.widget.WidgetStateUpdater
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CounterDetailUiState(
    val isLoading: Boolean = true,
    val missing: Boolean = false,
    val name: String = "",
    val streakDays: Int = 0,
    val goalMilestoneTarget: Int = 0,
    val ringFillRatio: Float = 0f,
    /** Smallest milestone strictly greater than the streak; null when all milestones reached. */
    val nextMilestone: Int? = null,
    val achievedMilestones: List<Int> = emptyList(),
    val canRevive: Boolean = false,
    val mostRecentMilestone: Int? = null,
    val deleteConfirmVisible: Boolean = false,
)

@HiltViewModel(assistedFactory = CounterDetailViewModel.Factory::class)
class CounterDetailViewModel @AssistedInject constructor(
    @Assisted private val counterId: Long,
    private val getCounter: GetCounterByIdUseCase,
    private val calculateStreak: CalculateStreakUseCase,
    private val getNextMilestone: GetNextMilestoneUseCase,
    private val getAchievedMilestones: GetAchievedMilestonesUseCase,
    private val getMostRecentMilestone: GetMostRecentMilestoneUseCase,
    private val deleteCounter: DeleteCounterUseCase,
    private val milestoneRepository: MilestoneRepository,
    private val markCelebrationsShown: MarkCelebrationsShownUseCase,
    private val clock: Clock,
    private val widgetStateUpdater: WidgetStateUpdater,
    private val application: Application,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(counterId: Long): CounterDetailViewModel
    }

    private val _state = MutableStateFlow(CounterDetailUiState())
    val state: StateFlow<CounterDetailUiState> = _state.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    /** Recompute on lifecycle resume so the streak reflects the current date (Decision 8). */
    fun onResume() = load()

    private fun load() {
        viewModelScope.launch {
            val counter = getCounter(counterId)
            if (counter == null) {
                _state.update { it.copy(isLoading = false, missing = true) }
                return@launch
            }
            val streak = calculateStreak(counter.startDate)
            val target = counter.goalMilestoneTarget.coerceAtLeast(1)
            val mostRecent = getMostRecentMilestone(streak)
            _state.update {
                it.copy(
                    isLoading = false,
                    missing = false,
                    name = counter.goalName,
                    streakDays = streak,
                    goalMilestoneTarget = counter.goalMilestoneTarget,
                    ringFillRatio = (streak.toFloat() / target).coerceIn(0f, 1f),
                    nextMilestone = getNextMilestone(streak),
                    achievedMilestones = getAchievedMilestones(streak),
                    canRevive = mostRecent != null,
                    mostRecentMilestone = mostRecent,
                )
            }
            maybeAutoLaunchCelebration(streak)
        }
    }

    /**
     * Ensures a [MilestoneRecord] exists for every milestone the current streak has reached, then
     * auto-launches the celebration for the most-recent one if it has not yet been shown (FR-021,
     * SC-004). Marking-all-shown happens here so a subsequent resume never re-launches.
     */
    private suspend fun maybeAutoLaunchCelebration(streak: Int) {
        val achieved = getAchievedMilestones(streak)
        if (achieved.isEmpty()) return
        val mostRecent = achieved.max()
        achieved.forEach { milestone ->
            milestoneRepository.insertOrIgnore(
                MilestoneRecord(counterId = counterId, milestoneDays = milestone, notifiedAt = clock.instant()),
            )
        }
        val mostRecentRecord = milestoneRepository.getForCounter(counterId)
            .firstOrNull { it.milestoneDays == mostRecent }
        if (mostRecentRecord != null && !mostRecentRecord.celebrationShown) {
            markCelebrationsShown(counterId)
            _events.send(UiEvent.AutoLaunchCelebration(mostRecent))
        }
    }

    fun requestDelete() = _state.update { it.copy(deleteConfirmVisible = true) }
    fun dismissDelete() = _state.update { it.copy(deleteConfirmVisible = false) }

    fun confirmDelete() {
        _state.update { it.copy(deleteConfirmVisible = false) }
        viewModelScope.launch {
            deleteCounter(counterId)
            widgetStateUpdater.refreshForCounter(application.applicationContext, counterId)
            _events.send(UiEvent.CounterDeleted)
        }
    }

    sealed interface UiEvent {
        /** Auto-launch the celebration overlay for [milestone] (wired in US4). */
        data class AutoLaunchCelebration(val milestone: Int) : UiEvent
        data object CounterDeleted : UiEvent
    }
}
