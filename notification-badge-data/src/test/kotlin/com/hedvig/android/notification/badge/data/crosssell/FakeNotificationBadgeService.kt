package com.hedvig.android.notification.badge.data.crosssell

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.hedvig.android.notification.badge.data.storage.DatastoreNotificationBadgeService
import com.hedvig.android.notification.badge.data.storage.NotificationBadge
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeService
import kotlinx.coroutines.CoroutineScope
import java.io.File

internal class FakeNotificationBadgeService(
  coroutineScope: CoroutineScope,
) : NotificationBadgeService by DatastoreNotificationBadgeService(
  PreferenceDataStoreFactory.create(
    scope = coroutineScope,
    produceFile = { File.createTempFile("test_datastore_file", ".preferences_pb") },
  ),
) {
  suspend fun <T> setData(badgeToDataMap: Map<NotificationBadge<T>, T>) {
    badgeToDataMap.forEach { (key, value) ->
      setValue(key, value)
    }
  }
}
