package com.hedvig.android.feature.chat.floating

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatTooltipStorage(
  private val dataStore: DataStore<Preferences>,
) {
  suspend fun setLastEpochDayWhenChatTooltipWasShown(epochDay: Long) {
    dataStore.edit { preferences ->
      preferences.set(SHARED_PREFERENCE_LAST_OPEN, epochDay)
    }
  }

  fun getLastEpochDayWhenChatTooltipWasShown(): Flow<Long> {
    return dataStore.data.map { preferences ->
      preferences.get(SHARED_PREFERENCE_LAST_OPEN) ?: 0
    }
  }
}

private val SHARED_PREFERENCE_LAST_OPEN = longPreferencesKey(
  "com.hedvig.android.feature.chat.floating.SHARED_PREFERENCE_LAST_OPEN",
)
