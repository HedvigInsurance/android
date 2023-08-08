package com.hedvig.android.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.theme.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsDataStore {
  suspend fun setTheme(theme: Theme)
  fun observeTheme(): Flow<Theme>
}

internal class SettingsDataStoreImpl(
  private val dataStore: DataStore<Preferences>,
) : SettingsDataStore {

  private val themeKey = stringPreferencesKey("settings-theme")

  override suspend fun setTheme(theme: Theme) {
    dataStore.edit {
      it[themeKey] = theme.name
    }
  }

  override fun observeTheme(): Flow<Theme> {
    return dataStore.data.map {
      it[themeKey]?.let { themeString -> Theme.valueOf(themeString) } ?: Theme.SYSTEM_DEFAULT
    }
  }
}
