package com.hedvig.android.data.chat.read.timestamp

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

/**
 * Stores the timestamp of the newest message which the member has seen.
 */
internal interface ChatMessageTimestampStorage {
  suspend fun getLatestReadTimestamp(): Instant?

  suspend fun setLatestReadTimestamp(timestamp: Instant)

  suspend fun clearLatestReadTimestamp()
}

internal class ChatMessageTimestampStorageImpl(
  private val datastore: DataStore<Preferences>,
) : ChatMessageTimestampStorage {
  override suspend fun getLatestReadTimestamp(): Instant? {
    return datastore.data
      .map { it[lastReadChatMessageTimestampPreferenceKey] }
      .first()
      ?.let { Instant.parse(it) }
  }

  override suspend fun setLatestReadTimestamp(timestamp: Instant) {
    datastore.edit { preferences ->
      preferences[lastReadChatMessageTimestampPreferenceKey] = timestamp.toString()
    }
  }

  override suspend fun clearLatestReadTimestamp() {
    datastore.edit { preferences ->
      preferences.remove(lastReadChatMessageTimestampPreferenceKey)
    }
  }

  companion object {
    private val lastReadChatMessageTimestampPreferenceKey =
      stringPreferencesKey("com.hedvig.android.data.chat.read.timestamp.lastReadChatMessageTimestampPreferenceKey")
  }
}
