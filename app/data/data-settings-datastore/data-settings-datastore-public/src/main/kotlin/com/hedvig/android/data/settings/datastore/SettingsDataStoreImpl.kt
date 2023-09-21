package com.hedvig.android.data.settings.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsDataStore {
  suspend fun setTheme(theme: Theme)

  /**
   * Returns if a specific [Theme] was explicitly chosen, otherwise null.
   */
  fun observeTheme(): Flow<Theme?>
}

class SettingsDataStoreImpl(
  private val dataStore: DataStore<Preferences>,
) : SettingsDataStore {
  override suspend fun setTheme(theme: Theme) {
    dataStore.edit {
      it[themeKey] = theme.name
    }
  }

  override fun observeTheme(): Flow<Theme?> {
    return dataStore.data.map {
      it[themeKey]?.let { themeString -> Theme.valueOf(themeString) }
    }
  }

  companion object {
    private val themeKey = stringPreferencesKey("settings-theme")
  }
}
