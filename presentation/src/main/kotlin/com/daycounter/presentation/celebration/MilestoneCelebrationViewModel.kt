package com.daycounter.presentation.celebration

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.domain.usecase.GetCounterByIdUseCase
import com.daycounter.domain.usecase.MarkCelebrationsShownUseCase
import com.daycounter.presentation.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CelebrationUiState(
    val milestone: Int,
    val counterName: String = "",
    @StringRes val messageRes: Int,
)

@HiltViewModel(assistedFactory = MilestoneCelebrationViewModel.Factory::class)
class MilestoneCelebrationViewModel @AssistedInject constructor(
    @Assisted private val counterId: Long,
    @Assisted private val milestone: Int,
    private val getCounter: GetCounterByIdUseCase,
    private val markCelebrationsShown: MarkCelebrationsShownUseCase,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(counterId: Long, milestone: Int): MilestoneCelebrationViewModel
    }

    private val _state = MutableStateFlow(
        CelebrationUiState(milestone = milestone, messageRes = messageResFor(milestone)),
    )
    val state: StateFlow<CelebrationUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val name = getCounter(counterId)?.goalName.orEmpty()
            _state.update { it.copy(counterName = name) }
            // Opening the overlay (auto-launch or Revivir) marks all milestones shown (FR-021).
            markCelebrationsShown(counterId)
        }
    }

    private companion object {
        @StringRes
        fun messageResFor(milestone: Int): Int = when (milestone) {
            1 -> R.string.celebration_message_1
            7 -> R.string.celebration_message_7
            30 -> R.string.celebration_message_30
            100 -> R.string.celebration_message_100
            365 -> R.string.celebration_message_365
            else -> R.string.celebration_message_1000
        }
    }
}
