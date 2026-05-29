package com.daycounter.presentation.counter

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.data.datastore.NotificationPreferencesDataStore
import com.daycounter.domain.model.Counter
import com.daycounter.domain.usecase.CreateCounterUseCase
import com.daycounter.presentation.widget.WidgetStateUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateCounterUiState(
    val goalName: String = "",
    val category: String = "",
    val startDate: LocalDate? = null,
    val goalMilestoneTarget: Int = Counter.DEFAULT_GOAL_TARGET,
    val isSaving: Boolean = false,
    val nameError: NameError? = null,
    val categoryError: CategoryError? = null,
    val dateError: DateError? = null,
) {
    val canSave: Boolean
        get() = !isSaving &&
            goalName.trim().isNotEmpty() &&
            goalName.length <= MAX_NAME &&
            category.length <= MAX_CATEGORY &&
            goalMilestoneTarget in Counter.GOAL_TARGETS

    enum class NameError { Blank, TooLong }
    enum class CategoryError { TooLong }
    enum class DateError { Future }

    companion object {
        const val MAX_NAME = 100
        const val MAX_CATEGORY = 50
    }
}

@HiltViewModel
class CreateCounterViewModel @Inject constructor(
    private val createCounter: CreateCounterUseCase,
    private val widgetStateUpdater: WidgetStateUpdater,
    private val notificationPrefs: NotificationPreferencesDataStore,
    private val application: Application,
) : ViewModel() {

    val notificationPermissionAlreadyRequested = notificationPrefs.permissionRequested

    fun onPermissionRequestComplete() {
        viewModelScope.launch { notificationPrefs.setPermissionRequested() }
    }

    private val _state = MutableStateFlow(CreateCounterUiState())
    val state: StateFlow<CreateCounterUiState> = _state.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onGoalNameChange(value: String) = _state.update { it.copy(goalName = value, nameError = null) }
    fun onCategoryChange(value: String) = _state.update { it.copy(category = value, categoryError = null) }
    fun onStartDateChange(date: LocalDate?) = _state.update { it.copy(startDate = date, dateError = null) }
    fun onGoalTargetChange(target: Int) = _state.update { it.copy(goalMilestoneTarget = target) }

    fun onSave() {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val s = _state.value
                val result = createCounter(
                    goalName = s.goalName,
                    startDate = s.startDate,
                    category = s.category,
                    goalMilestoneTarget = s.goalMilestoneTarget,
                )
                when (result) {
                    is CreateCounterUseCase.Result.Success -> {
                        widgetStateUpdater.refreshForCounter(application.applicationContext, result.id)
                        _events.send(UiEvent.NavigateBack)
                    }
                    is CreateCounterUseCase.Result.ValidationError -> applyValidationError(result.failure)
                }
            } catch (e: Exception) {
                _events.send(UiEvent.ShowStorageError)
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun applyValidationError(failure: CreateCounterUseCase.ValidationFailure) {
        _state.update {
            when (failure) {
                CreateCounterUseCase.ValidationFailure.NameBlank -> it.copy(nameError = CreateCounterUiState.NameError.Blank)
                CreateCounterUseCase.ValidationFailure.NameTooLong -> it.copy(nameError = CreateCounterUiState.NameError.TooLong)
                CreateCounterUseCase.ValidationFailure.CategoryTooLong -> it.copy(categoryError = CreateCounterUiState.CategoryError.TooLong)
                CreateCounterUseCase.ValidationFailure.InvalidGoalTarget -> it // not user-reachable (chips constrain choice)
                CreateCounterUseCase.ValidationFailure.FutureStartDate -> it.copy(dateError = CreateCounterUiState.DateError.Future)
            }
        }
    }

    sealed interface UiEvent {
        data object NavigateBack : UiEvent
        data object ShowStorageError : UiEvent
    }
}
