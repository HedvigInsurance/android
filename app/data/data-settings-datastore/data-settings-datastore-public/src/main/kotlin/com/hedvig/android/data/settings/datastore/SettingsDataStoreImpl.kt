package com.hedvig.android.data.settings.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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

  suspend fun setEmailSubscriptionPreference(subscribe: Boolean)

  fun observeEmailSubscriptionPreference(): Flow<Boolean>
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

  override suspend fun setEmailSubscriptionPreference(subscribe: Boolean) {
    dataStore.edit {
      it[subscriptionKey] = subscribe
    }
  }

  override fun observeEmailSubscriptionPreference(): Flow<Boolean> {
    return dataStore.data.map { preferences ->
      // here we assume that member is subscribed by default in customer.io
      preferences[subscriptionKey] ?: true
    }
  }

  companion object {
    private val themeKey = stringPreferencesKey("settings-theme")
    private val subscriptionKey = booleanPreferencesKey(
      "com.hedvig.android.data.settings.datastore.settings-email-subscription",
    )
  }
}
