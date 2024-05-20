package com.hedvig.android.data.settings.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataStore(private val dataStore: DataStore<Preferences>) {
  suspend fun setTheme(theme: Theme) {
    dataStore.edit {
      it[themeKey] = theme.name
    }
  }

  fun observeTheme(): Flow<Theme?> {
    return dataStore.data.map {
      it[themeKey]?.let { themeString -> Theme.valueOf(themeString) }
    }
  }

  suspend fun setChatBubbleSetting(showChatBubble: Boolean) {
    dataStore.edit { preferences ->
      preferences[chatBubbleKey] = showChatBubble
    }
  }

  fun chatBubbleSetting(): Flow<Boolean> {
    return dataStore.data.map { preferences ->
      preferences.get(chatBubbleKey) ?: false
    }
  }

  companion object {
    private val themeKey = stringPreferencesKey("settings-theme")
    private val chatBubbleKey = booleanPreferencesKey("com.hedvig.android.data.settings.datastore.chatBubbleKey")
  }
}
