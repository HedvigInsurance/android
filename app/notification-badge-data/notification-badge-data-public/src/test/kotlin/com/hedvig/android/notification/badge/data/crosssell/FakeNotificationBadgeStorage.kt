package com.hedvig.android.notification.badge.data.crosssell

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.hedvig.android.notification.badge.data.storage.DatastoreNotificationBadgeStorage
import com.hedvig.android.notification.badge.data.storage.NotificationBadgeStorage
import java.io.File
import kotlinx.coroutines.CoroutineScope

internal class FakeNotificationBadgeStorage(
  coroutineScope: CoroutineScope,
) : NotificationBadgeStorage by DatastoreNotificationBadgeStorage(
    PreferenceDataStoreFactory.create(
      scope = coroutineScope,
      produceFile = { File.createTempFile("test_datastore_file", ".preferences_pb") },
    ),
  )
