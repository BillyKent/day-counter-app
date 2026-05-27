package com.daycounter.presentation.counter

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.domain.usecase.DeleteCounterUseCase
import com.daycounter.domain.usecase.GetCounterByIdUseCase
import com.daycounter.domain.usecase.ResetCounterUseCase
import com.daycounter.domain.usecase.UpdateCounterUseCase
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

data class EditCounterUiState(
    val counterId: Long = 0L,
    val goalName: String = "",
    val startDate: LocalDate? = null,
    val isSaving: Boolean = false,
    val nameError: CreateCounterUiState.NameError? = null,
    val dateError: CreateCounterUiState.DateError? = null,
    val showResetDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
) {
    val canSave: Boolean
        get() = !isSaving && goalName.trim().isNotEmpty() && goalName.length <= 100
}

@HiltViewModel
class EditCounterViewModel @Inject constructor(
    private val getCounter: GetCounterByIdUseCase,
    private val updateCounter: UpdateCounterUseCase,
    private val resetCounter: ResetCounterUseCase,
    private val deleteCounter: DeleteCounterUseCase,
    private val widgetStateUpdater: WidgetStateUpdater,
    private val application: Application,
) : ViewModel() {

    private val _state = MutableStateFlow(EditCounterUiState())
    val state: StateFlow<EditCounterUiState> = _state.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun load(counterId: Long) {
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
                    startDate = c.startDate,
                )
            }
        }
    }

    fun onGoalNameChange(value: String) {
        _state.update { it.copy(goalName = value, nameError = null) }
    }

    fun onStartDateChange(date: LocalDate?) {
        _state.update { it.copy(startDate = date, dateError = null) }
    }

    fun onSave() {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val s = _state.value
                val result = updateCounter(
                    counterId = s.counterId,
                    goalName = s.goalName,
                    startDate = s.startDate,
                )
                when (result) {
                    is UpdateCounterUseCase.Result.Success -> {
                        widgetStateUpdater.refreshForCounter(application.applicationContext, s.counterId)
                        _events.send(UiEvent.Done)
                    }
                    is UpdateCounterUseCase.Result.NotFound -> _events.send(UiEvent.Done)
                    is UpdateCounterUseCase.Result.ValidationError -> applyValidationError(result)
                }
            } catch (e: Exception) {
                _events.send(UiEvent.ShowStorageError)
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    fun requestReset() = _state.update { it.copy(showResetDialog = true) }
    fun requestDelete() = _state.update { it.copy(showDeleteDialog = true) }
    fun dismissDialogs() = _state.update { it.copy(showResetDialog = false, showDeleteDialog = false) }

    fun confirmReset() {
        _state.update { it.copy(showResetDialog = false) }
        viewModelScope.launch {
            try {
                val id = _state.value.counterId
                resetCounter(id)
                widgetStateUpdater.refreshForCounter(application.applicationContext, id)
                _events.send(UiEvent.Done)
            } catch (e: Exception) {
                _events.send(UiEvent.ShowStorageError)
            }
        }
    }

    fun confirmDelete() {
        _state.update { it.copy(showDeleteDialog = false) }
        viewModelScope.launch {
            try {
                val id = _state.value.counterId
                deleteCounter(id)
                widgetStateUpdater.refreshForCounter(application.applicationContext, id)
                _events.send(UiEvent.Done)
            } catch (e: Exception) {
                _events.send(UiEvent.ShowStorageError)
            }
        }
    }

    private fun applyValidationError(error: UpdateCounterUseCase.Result.ValidationError) {
        _state.update {
            when (error.failure) {
                UpdateCounterUseCase.ValidationFailure.NameBlank ->
                    it.copy(nameError = CreateCounterUiState.NameError.Blank)
                UpdateCounterUseCase.ValidationFailure.NameTooLong ->
                    it.copy(nameError = CreateCounterUiState.NameError.TooLong)
                UpdateCounterUseCase.ValidationFailure.FutureStartDate ->
                    it.copy(dateError = CreateCounterUiState.DateError.Future)
            }
        }
    }

    sealed interface UiEvent {
        data object Done : UiEvent
        data object ShowStorageError : UiEvent
    }
}
