package com.daycounter.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationDataStore by preferencesDataStore(name = "notification_prefs")

/**
 * In-app notifications toggle (FR-017) plus a flag to prevent re-prompting after the
 * first POST_NOTIFICATIONS request.
 */
@Singleton
class NotificationPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val notificationsEnabled: Flow<Boolean> =
        context.notificationDataStore.data.map { it[KEY_ENABLED] ?: true }

    val permissionRequested: Flow<Boolean> =
        context.notificationDataStore.data.map { it[KEY_PERMISSION_REQUESTED] ?: false }

    suspend fun isNotificationsEnabled(): Boolean = notificationsEnabled.first()

    suspend fun setNotificationsEnabled(value: Boolean) {
        context.notificationDataStore.edit { it[KEY_ENABLED] = value }
    }

    suspend fun setPermissionRequested() {
        context.notificationDataStore.edit { it[KEY_PERMISSION_REQUESTED] = true }
    }

    private companion object {
        val KEY_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_PERMISSION_REQUESTED = booleanPreferencesKey("notification_permission_requested")
    }
}
