package com.daycounter.data.repository

import com.daycounter.data.datastore.SettingsPreferencesDataStore
import com.daycounter.domain.model.AppLanguage
import com.daycounter.domain.model.AppearanceMode
import com.daycounter.domain.model.ReminderTime
import com.daycounter.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val store: SettingsPreferencesDataStore,
) : SettingsRepository {

    override val language: Flow<AppLanguage> =
        store.languageTag.map { AppLanguage.fromTag(it) }

    override suspend fun setLanguage(language: AppLanguage) = store.setLanguageTag(language.tag)

    override val appearance: Flow<AppearanceMode> =
        store.appearanceName.map { AppearanceMode.fromName(it) }

    override suspend fun setAppearance(mode: AppearanceMode) = store.setAppearanceName(mode.name)

    override val dailyReminderEnabled: Flow<Boolean> = store.dailyReminderEnabled

    override suspend fun setDailyReminderEnabled(enabled: Boolean) =
        store.setDailyReminderEnabled(enabled)

    override val reminderTime: Flow<ReminderTime> =
        store.reminderTime.map { ReminderTime.parse(it) }

    override suspend fun setReminderTime(time: ReminderTime) =
        store.setReminderTime(time.serialize())
}
