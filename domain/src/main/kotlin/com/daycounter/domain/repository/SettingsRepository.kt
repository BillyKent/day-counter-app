package com.daycounter.domain.repository

import com.daycounter.domain.model.AppLanguage
import com.daycounter.domain.model.AppearanceMode
import com.daycounter.domain.model.ReminderTime
import kotlinx.coroutines.flow.Flow

/**
 * App-level user preferences (non-sensitive). Backed by DataStore in `:data`.
 *
 * Covers the in-app language (US3), appearance/dark-mode (US7), and the daily reminder toggle + time
 * (US4). The milestone-notifications toggle remains in the existing notification preferences store.
 */
interface SettingsRepository {
    val language: Flow<AppLanguage>
    suspend fun setLanguage(language: AppLanguage)

    val appearance: Flow<AppearanceMode>
    suspend fun setAppearance(mode: AppearanceMode)

    val dailyReminderEnabled: Flow<Boolean>
    suspend fun setDailyReminderEnabled(enabled: Boolean)

    val reminderTime: Flow<ReminderTime>
    suspend fun setReminderTime(time: ReminderTime)
}
