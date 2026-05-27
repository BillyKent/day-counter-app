package com.daycounter.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.data.datastore.OnboardingPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: OnboardingPreferencesDataStore,
) : ViewModel() {

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun complete() {
        viewModelScope.launch {
            prefs.setOnboardingShown(true)
            _events.send(UiEvent.NavigateToHome)
        }
    }

    sealed interface UiEvent {
        data object NavigateToHome : UiEvent
    }
}
