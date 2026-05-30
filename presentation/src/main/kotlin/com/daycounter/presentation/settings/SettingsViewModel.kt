package com.daycounter.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.data.datastore.NotificationPreferencesDataStore
import com.daycounter.domain.model.AppLanguage
import com.daycounter.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: NotificationPreferencesDataStore,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val notificationsEnabled: StateFlow<Boolean> = prefs.notificationsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = true,
        )

    val language: StateFlow<AppLanguage> = settingsRepository.language
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AppLanguage.DEFAULT,
        )

    // Emitted after a language change is persisted so the screen can recreate() the activity (US3).
    private val _languageChanged = Channel<Unit>(Channel.BUFFERED)
    val languageChanged = _languageChanged.receiveAsFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setNotificationsEnabled(enabled) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            if (language != this@SettingsViewModel.language.value) {
                settingsRepository.setLanguage(language)
                _languageChanged.send(Unit)
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
