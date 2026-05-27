package com.daycounter.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_prefs")

/** Persisted flag indicating onboarding has been shown to the user (FR-000). */
@Singleton
class OnboardingPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val onboardingShown: Flow<Boolean> =
        context.onboardingDataStore.data.map { prefs -> prefs[KEY] ?: false }

    suspend fun setOnboardingShown(shown: Boolean) {
        context.onboardingDataStore.edit { it[KEY] = shown }
    }

    private companion object {
        val KEY = booleanPreferencesKey("onboarding_shown")
    }
}
