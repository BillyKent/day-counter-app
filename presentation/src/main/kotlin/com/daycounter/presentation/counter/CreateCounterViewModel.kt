package com.daycounter.presentation.counter

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.data.datastore.NotificationPreferencesDataStore
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
    val startDate: LocalDate? = null,
    val isSaving: Boolean = false,
    val nameError: NameError? = null,
    val dateError: DateError? = null,
) {
    val canSave: Boolean
        get() = !isSaving && goalName.trim().isNotEmpty() && goalName.length <= 100

    enum class NameError { Blank, TooLong }
    enum class DateError { Future }
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

    fun onGoalNameChange(value: String) {
        _state.update {
            it.copy(
                goalName = value,
                nameError = null,
            )
        }
    }

    fun onStartDateChange(date: LocalDate?) {
        _state.update { it.copy(startDate = date, dateError = null) }
    }

    fun onSave() {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val result = createCounter(
                    goalName = _state.value.goalName,
                    startDate = _state.value.startDate,
                )
                when (result) {
                    is CreateCounterUseCase.Result.Success -> {
                        widgetStateUpdater.refreshForCounter(application.applicationContext, result.id)
                        _events.send(UiEvent.NavigateBack)
                    }
                    is CreateCounterUseCase.Result.ValidationError -> applyValidationError(result)
                }
            } catch (e: Exception) {
                _events.send(UiEvent.ShowStorageError)
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun applyValidationError(error: CreateCounterUseCase.Result.ValidationError) {
        _state.update {
            when (error.failure) {
                CreateCounterUseCase.ValidationFailure.NameBlank ->
                    it.copy(nameError = CreateCounterUiState.NameError.Blank)
                CreateCounterUseCase.ValidationFailure.NameTooLong ->
                    it.copy(nameError = CreateCounterUiState.NameError.TooLong)
                CreateCounterUseCase.ValidationFailure.FutureStartDate ->
                    it.copy(dateError = CreateCounterUiState.DateError.Future)
            }
        }
    }

    sealed interface UiEvent {
        data object NavigateBack : UiEvent
        data object ShowStorageError : UiEvent
    }
}
