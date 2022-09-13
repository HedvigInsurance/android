package com.hedvig.android.notification.badge.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DatastoreNotificationBadgeService(
  private val dataStore: DataStore<Preferences>,
) : NotificationBadgeService {

  @Suppress("INAPPLICABLE_JVM_NAME")
  @JvmName("getValueOrEmptySetIfItsNull")
  override fun <SetOfT : Set<T>, T> getValue(
    notificationBadge: NotificationBadge<SetOfT>,
  ): Flow<Set<T>> {
    return dataStore
      .data
      .map { preferences ->
        val value = preferences[notificationBadge.preferencesKey]
        value ?: emptySet()
      }
  }

  override fun <T> getValue(
    notificationBadge: NotificationBadge<T>,
  ): Flow<T?> {
    return dataStore
      .data
      .map { preferences ->
        preferences[notificationBadge.preferencesKey]
      }
  }

  override suspend fun <T> setValue(
    notificationBadge: NotificationBadge<T>,
    newStatus: T,
  ) {
    dataStore.edit { preferences ->
      preferences[notificationBadge.preferencesKey] = newStatus
    }
  }
}
