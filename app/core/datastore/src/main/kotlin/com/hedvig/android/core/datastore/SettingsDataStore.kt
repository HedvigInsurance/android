package com.hedvig.android.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsDataStore {
  suspend fun setTheme(theme: String)
  fun observeTheme(): Flow<String>
  suspend fun setLanguage(language: String)
  fun observeLanguage(): Flow<String>
}

internal class SettingsDataStoreImpl(
  private val dataStore: DataStore<Preferences>,
) : SettingsDataStore {

  private val languageKey = stringPreferencesKey("settings-language")
  private val themeKey = stringPreferencesKey("settings-theme")

  override suspend fun setTheme(theme: String) {
    dataStore.edit {
      it[themeKey] = theme
    }
  }

  override fun observeTheme(): Flow<String> {
    return dataStore.data.map { it[themeKey] ?: "LIGHT" }
  }

  override suspend fun setLanguage(language: String) {
    dataStore.edit {
      it[languageKey] = language
    }
  }

  override fun observeLanguage(): Flow<String> {
    return dataStore.data.map { it[languageKey] ?: "EN_SE" }
  }

}
