package com.hedvig.android.feature.chat.closedevent

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

interface ChatClosedEventStore {
  suspend fun increaseChatClosedCounter()

  suspend fun resetChatClosedCounter()

  fun observeChatClosedCounter(): Flow<Int>
}

internal class ChatClosedEventDataStore(
  private val dataStore: DataStore<Preferences>,
) : ChatClosedEventStore {
  override suspend fun increaseChatClosedCounter() {
    dataStore.edit {
      it[CHAT_CLOSED_COUNTER] = (it[CHAT_CLOSED_COUNTER] ?: 0) + 1
    }
  }

  override suspend fun resetChatClosedCounter() {
    dataStore.edit {
      it[CHAT_CLOSED_COUNTER] = 0
    }
  }

  override fun observeChatClosedCounter() = dataStore.data
    .catch { logcat(LogPriority.ERROR, it) { "observeChatClosedCounter failed" } }
    .map { it[CHAT_CLOSED_COUNTER] ?: 0 }

  companion object {
    private val CHAT_CLOSED_COUNTER = intPreferencesKey("CHAT_CLOSED_COUNTER")
  }
}
