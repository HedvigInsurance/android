package com.hedvig.android.notification.badge.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * A storage which stores which notificationBadges have already been seen by the user and exposes a convenient API to
 * get and set that status.
 */
internal interface NotificationBadgeStorage {
  fun getValue(notificationBadge: NotificationBadge): Flow<Set<String>>

  suspend fun setValue(notificationBadge: NotificationBadge, newStatus: Set<String>)
}

internal sealed interface NotificationBadge {
  val preferencesKey: Preferences.Key<Set<String>>

  object CrossSellInsuranceFragmentCard : NotificationBadge {
    override val preferencesKey = stringSetPreferencesKey("SEEN_CROSS_SELLS_INSURANCE_FRAGMENT_CARD")
  }
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class DatastoreNotificationBadgeStorage(
  private val dataStore: DataStore<Preferences>,
) : NotificationBadgeStorage {
  override fun getValue(notificationBadge: NotificationBadge): Flow<Set<String>> {
    return dataStore
      .data
      .map { preferences ->
        preferences[notificationBadge.preferencesKey] ?: emptySet()
      }
      .distinctUntilChanged()
  }

  override suspend fun setValue(notificationBadge: NotificationBadge, newStatus: Set<String>) {
    dataStore.edit { preferences ->
      preferences[notificationBadge.preferencesKey] = newStatus
    }
  }
}
