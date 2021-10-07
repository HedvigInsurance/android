package com.hedvig.app.service.badge

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationBadgeService(
    private val dataStore: DataStore<Preferences>,
) {

    fun seenStatus(
        notificationBadgeList: List<NotificationBadge>
    ): Flow<List<Pair<NotificationBadge, Seen>>> {
        return dataStore
            .data
            .map { preferences ->
                notificationBadgeList
                    .map { notificationBadge ->
                        notificationBadge to Seen.fromNullableBoolean(preferences.get(notificationBadge.key))
                    }
            }
    }

    fun seenStatus(
        notificationBadge: NotificationBadge
    ): Flow<Seen> {
        return dataStore
            .data
            .map { preferences ->
                Seen.fromNullableBoolean(preferences[notificationBadge.key])
            }
    }

    suspend fun setSeenStatus(
        notificationBadge: NotificationBadge,
        newSeenStatus: Seen
    ) {
        dataStore.edit { preferences ->
            preferences[notificationBadge.key] = newSeenStatus.isSeen()
        }
    }
}
