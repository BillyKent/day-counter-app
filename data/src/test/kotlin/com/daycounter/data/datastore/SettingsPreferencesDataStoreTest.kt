package com.daycounter.data.datastore

import androidx.test.core.app.ApplicationProvider
import com.daycounter.data.repository.SettingsRepositoryImpl
import com.daycounter.domain.model.AppLanguage
import com.daycounter.domain.model.AppearanceMode
import com.daycounter.domain.model.ReminderTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class SettingsPreferencesDataStoreTest {

    private lateinit var repo: SettingsRepositoryImpl

    @Before
    fun setUp() {
        val store = SettingsPreferencesDataStore(ApplicationProvider.getApplicationContext())
        repo = SettingsRepositoryImpl(store)
    }

    @Test
    fun `defaults are english, system, disabled, 0900`() = runTest {
        assertEquals(AppLanguage.ENGLISH, repo.language.first())
        assertEquals(AppearanceMode.SYSTEM, repo.appearance.first())
        assertEquals(false, repo.dailyReminderEnabled.first())
        assertEquals(ReminderTime(9, 0), repo.reminderTime.first())
    }

    @Test
    fun `round-trips language appearance reminder`() = runTest {
        repo.setLanguage(AppLanguage.SPANISH)
        repo.setAppearance(AppearanceMode.DARK)
        repo.setDailyReminderEnabled(true)
        repo.setReminderTime(ReminderTime.EVENING)

        assertEquals(AppLanguage.SPANISH, repo.language.first())
        assertEquals(AppearanceMode.DARK, repo.appearance.first())
        assertEquals(true, repo.dailyReminderEnabled.first())
        assertEquals(ReminderTime(21, 0), repo.reminderTime.first())
    }
}
