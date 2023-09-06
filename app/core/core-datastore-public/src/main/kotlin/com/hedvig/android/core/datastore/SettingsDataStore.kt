package com.hedvig.android.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataStore(
  private val dataStore: DataStore<Preferences>,
) {
  suspend fun setTheme(theme: Theme) {
    dataStore.edit {
      it[themeKey] = theme.name
    }
  }

  fun observeTheme(): Flow<Theme> {
    return dataStore.data.map {
      it[themeKey]?.let { themeString -> Theme.valueOf(themeString) } ?: Theme.SYSTEM_DEFAULT
    }
  }

  companion object {
    private val themeKey = stringPreferencesKey("settings-theme")
  }
}
