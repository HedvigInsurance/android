package com.hedvig.android.appinformation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

interface EnableNotificationsReminderManager {
  fun showNotificationReminder(): Flow<Boolean>

  suspend fun snoozeNotificationReminder()
}

internal class EnableNotificationsReminderManagerImpl(
  private val datastore: DataStore<Preferences>,
  private val clock: Clock,
) : EnableNotificationsReminderManager {
  override fun showNotificationReminder(): Flow<Boolean> {
    return getLastSnoozeTime().map { lastSnoozeTime ->
      val timeSinceLastSnooze: Duration = clock.now() - lastSnoozeTime
      timeSinceLastSnooze > snoozeTimeBeforeShowingReminderAgain
    }
  }

  override suspend fun snoozeNotificationReminder() {
    datastore.edit {
      it.set(notificationReminderTimeOfLastSnoozeKey, Json.encodeToString<Instant>(clock.now()))
    }
  }

  private fun getLastSnoozeTime(): Flow<Instant> {
    return datastore.data
      .map { it[notificationReminderTimeOfLastSnoozeKey] }
      .map {
        if (it == null) {
          Instant.DISTANT_PAST
        } else {
          Json.decodeFromString<Instant>(it)
        }
      }
  }

  companion object {
    private val snoozeTimeBeforeShowingReminderAgain: Duration = 60.days

    val notificationReminderTimeOfLastSnoozeKey = stringPreferencesKey("notificationReminderKey")
  }
}
