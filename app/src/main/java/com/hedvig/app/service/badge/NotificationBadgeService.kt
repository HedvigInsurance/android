package com.hedvig.app.service.badge

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationBadgeService(
    private val dataStore: DataStore<Preferences>,
) {

    @JvmName("getValueOrEmptySetIfItsNull")
    fun <SetOfT : Set<T>, T> getValue(
        notificationBadge: NotificationBadge<SetOfT>
    ): Flow<Set<T>> {
        return dataStore
            .data
            .map { preferences ->
                val value = preferences[notificationBadge.key]
                value ?: emptySet()
            }
    }

    fun <T> getValue(
        notificationBadge: NotificationBadge<T>
    ): Flow<T?> {
        return dataStore
            .data
            .map { preferences ->
                preferences[notificationBadge.key]
            }
    }

    suspend fun <T> setValue(
        notificationBadge: NotificationBadge<T>,
        newStatus: T
    ) {
        dataStore.edit { preferences ->
            preferences[notificationBadge.key] = newStatus
        }
    }
}
