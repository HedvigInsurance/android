package com.hedvig.app.service.badge

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface NotificationBadgeService {
    fun seenStatus(notificationBadgeList: List<NotificationBadge>): Flow<List<Pair<NotificationBadge, Seen>>>
    fun seenStatus(notificationBadge: NotificationBadge): Flow<Seen>
    suspend fun setSeenStatus(notificationBadge: NotificationBadge, newSeenStatus: Seen)
}

class NotificationBadgeServiceImpl(
    private val dataStore: DataStore<Preferences>,
) : NotificationBadgeService {

    override fun seenStatus(
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

    override fun seenStatus(
        notificationBadge: NotificationBadge
    ): Flow<Seen> {
        return dataStore
            .data
            .map { preferences ->
                Seen.fromNullableBoolean(preferences[notificationBadge.key])
            }
    }

    override suspend fun setSeenStatus(
        notificationBadge: NotificationBadge,
        newSeenStatus: Seen
    ) {
        dataStore.edit { preferences ->
            preferences[notificationBadge.key] = newSeenStatus.isSeen()
        }
    }
}
