package com.daycounter.presentation.counter

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.domain.usecase.ArchiveAndResetCounterUseCase
import com.daycounter.presentation.widget.WidgetStateUpdater
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ResetConfirmViewModel.Factory::class)
class ResetConfirmViewModel @AssistedInject constructor(
    @Assisted private val counterId: Long,
    private val archiveAndReset: ArchiveAndResetCounterUseCase,
    private val widgetStateUpdater: WidgetStateUpdater,
    private val application: Application,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(counterId: Long): ResetConfirmViewModel
    }

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun confirm() {
        viewModelScope.launch {
            archiveAndReset(counterId)
            widgetStateUpdater.refreshForCounter(application.applicationContext, counterId)
            _events.send(UiEvent.Done)
        }
    }

    sealed interface UiEvent {
        data object Done : UiEvent
    }
}
