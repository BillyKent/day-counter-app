package com.daycounter.presentation.counter

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.domain.model.Counter
import com.daycounter.domain.usecase.GetCounterByIdUseCase
import com.daycounter.domain.usecase.UpdateCounterUseCase
import com.daycounter.presentation.widget.WidgetStateUpdater
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditCounterUiState(
    val counterId: Long = 0L,
    val goalName: String = "",
    val category: String = "",
    val startDate: LocalDate? = null,
    val goalMilestoneTarget: Int = Counter.DEFAULT_GOAL_TARGET,
    val isSaving: Boolean = false,
    val nameError: CreateCounterUiState.NameError? = null,
    val categoryError: CreateCounterUiState.CategoryError? = null,
) {
    val canSave: Boolean
        get() = !isSaving &&
            goalName.trim().isNotEmpty() &&
            goalName.length <= CreateCounterUiState.MAX_NAME &&
            category.length <= CreateCounterUiState.MAX_CATEGORY &&
            goalMilestoneTarget in Counter.GOAL_TARGETS
}

@HiltViewModel(assistedFactory = EditCounterViewModel.Factory::class)
class EditCounterViewModel @AssistedInject constructor(
    @Assisted private val counterId: Long,
    private val getCounter: GetCounterByIdUseCase,
    private val updateCounter: UpdateCounterUseCase,
    private val widgetStateUpdater: WidgetStateUpdater,
    private val application: Application,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(counterId: Long): EditCounterViewModel
    }

    private val _state = MutableStateFlow(EditCounterUiState())
    val state: StateFlow<EditCounterUiState> = _state.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            val c = getCounter(counterId)
            if (c == null) {
                _events.send(UiEvent.Done)
                return@launch
            }
            _state.update {
                it.copy(
                    counterId = c.id,
                    goalName = c.goalName,
                    category = c.category.orEmpty(),
                    startDate = c.startDate,
                    goalMilestoneTarget = c.goalMilestoneTarget,
                )
            }
        }
    }

    fun onGoalNameChange(value: String) = _state.update { it.copy(goalName = value, nameError = null) }
    fun onCategoryChange(value: String) = _state.update { it.copy(category = value, categoryError = null) }
    fun onGoalTargetChange(target: Int) = _state.update { it.copy(goalMilestoneTarget = target) }

    fun onSave() {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val s = _state.value
                val result = updateCounter(
                    counterId = s.counterId,
                    goalName = s.goalName,
                    startDate = s.startDate, // read-only in the UI; sent unchanged
                    category = s.category,
                    goalMilestoneTarget = s.goalMilestoneTarget,
                )
                when (result) {
                    is UpdateCounterUseCase.Result.Success -> {
                        widgetStateUpdater.refreshForCounter(application.applicationContext, s.counterId)
                        _events.send(UiEvent.Done)
                    }
                    is UpdateCounterUseCase.Result.NotFound -> _events.send(UiEvent.Done)
                    is UpdateCounterUseCase.Result.ValidationError -> applyValidationError(result.failure)
                }
            } catch (e: Exception) {
                _events.send(UiEvent.ShowStorageError)
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun applyValidationError(failure: UpdateCounterUseCase.ValidationFailure) {
        _state.update {
            when (failure) {
                UpdateCounterUseCase.ValidationFailure.NameBlank -> it.copy(nameError = CreateCounterUiState.NameError.Blank)
                UpdateCounterUseCase.ValidationFailure.NameTooLong -> it.copy(nameError = CreateCounterUiState.NameError.TooLong)
                UpdateCounterUseCase.ValidationFailure.CategoryTooLong -> it.copy(categoryError = CreateCounterUiState.CategoryError.TooLong)
                else -> it
            }
        }
    }

    sealed interface UiEvent {
        data object Done : UiEvent
        data object ShowStorageError : UiEvent
    }
}
