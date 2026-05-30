package com.daycounter.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daycounter.data.datastore.NotificationPreferencesDataStore
import com.daycounter.data.work.DailyReminderScheduler
import com.daycounter.domain.model.AppLanguage
import com.daycounter.domain.model.AppearanceMode
import com.daycounter.domain.model.DataSnapshot
import com.daycounter.domain.model.ReminderTime
import com.daycounter.domain.repository.SettingsRepository
import com.daycounter.domain.usecase.EraseAllDataUseCase
import com.daycounter.domain.usecase.GetAllCountersUseCase
import com.daycounter.domain.usecase.RestoreAllDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: NotificationPreferencesDataStore,
    private val settingsRepository: SettingsRepository,
    getAllCounters: GetAllCountersUseCase,
    private val eraseAllData: EraseAllDataUseCase,
    private val restoreAllData: RestoreAllDataUseCase,
    private val reminderScheduler: DailyReminderScheduler,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val counterCount: StateFlow<Int> = getAllCounters().map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = 0,
        )

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

    val appearance: StateFlow<AppearanceMode> = settingsRepository.appearance
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AppearanceMode.DEFAULT,
        )

    fun setAppearance(mode: AppearanceMode) {
        viewModelScope.launch { settingsRepository.setAppearance(mode) }
    }

    // Emitted after a language change is persisted so the screen can recreate() the activity (US3).
    private val _languageChanged = Channel<Unit>(Channel.BUFFERED)
    val languageChanged = _languageChanged.receiveAsFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setNotificationsEnabled(enabled) }
    }

    // Daily reminder (US4): persist + (re)schedule the WorkManager job.
    val dailyReminderEnabled: StateFlow<Boolean> = settingsRepository.dailyReminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), false)

    val reminderTime: StateFlow<ReminderTime> = settingsRepository.reminderTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ReminderTime.DEFAULT)

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDailyReminderEnabled(enabled)
            reminderScheduler.schedule(appContext, enabled, settingsRepository.reminderTime.first())
        }
    }

    fun setReminderTime(time: ReminderTime) {
        viewModelScope.launch {
            settingsRepository.setReminderTime(time)
            if (settingsRepository.dailyReminderEnabled.first()) {
                reminderScheduler.schedule(appContext, enabled = true, time = time)
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            if (language != this@SettingsViewModel.language.value) {
                settingsRepository.setLanguage(language)
                _languageChanged.send(Unit)
            }
        }
    }

    // Erase-all + undo (FR-030/FR-031). The snapshot is held until undo or the next erase.
    private var lastSnapshot: DataSnapshot? = null
    private val _eraseUndoAvailable = Channel<Unit>(Channel.BUFFERED)
    val eraseUndoAvailable = _eraseUndoAvailable.receiveAsFlow()

    fun eraseAll() {
        viewModelScope.launch {
            lastSnapshot = eraseAllData()
            _eraseUndoAvailable.send(Unit)
        }
    }

    fun undoErase() {
        viewModelScope.launch {
            lastSnapshot?.let { restoreAllData(it) }
            lastSnapshot = null
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
