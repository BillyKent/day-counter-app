package com.daycounter.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

/**
 * Persists app-level preferences (US3 language, US7 appearance, US4 daily reminder). Stores raw
 * primitives; mapping to domain models lives in `SettingsRepositoryImpl`.
 *
 * Defaults: language `en`, appearance `SYSTEM`, daily reminder disabled, time `09:00`.
 */
@Singleton
class SettingsPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val languageTag: Flow<String> =
        context.settingsDataStore.data.map { it[KEY_LANGUAGE] ?: DEFAULT_LANGUAGE }

    val appearanceName: Flow<String> =
        context.settingsDataStore.data.map { it[KEY_APPEARANCE] ?: DEFAULT_APPEARANCE }

    val dailyReminderEnabled: Flow<Boolean> =
        context.settingsDataStore.data.map { it[KEY_DAILY_REMINDER_ENABLED] ?: false }

    val reminderTime: Flow<String> =
        context.settingsDataStore.data.map { it[KEY_REMINDER_TIME] ?: DEFAULT_REMINDER_TIME }

    suspend fun setLanguageTag(tag: String) {
        context.settingsDataStore.edit { it[KEY_LANGUAGE] = tag }
    }

    suspend fun setAppearanceName(name: String) {
        context.settingsDataStore.edit { it[KEY_APPEARANCE] = name }
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_DAILY_REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderTime(value: String) {
        context.settingsDataStore.edit { it[KEY_REMINDER_TIME] = value }
    }

    private companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_APPEARANCE = stringPreferencesKey("appearance")
        val KEY_DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val KEY_REMINDER_TIME = stringPreferencesKey("daily_reminder_time")

        const val DEFAULT_LANGUAGE = "en"
        const val DEFAULT_APPEARANCE = "SYSTEM"
        const val DEFAULT_REMINDER_TIME = "09:00"
    }
}
