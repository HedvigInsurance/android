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

  suspend fun setSubscriptionPreference(subscribe: Boolean)

  fun observeSubscriptionPreference(): Flow<Boolean>
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

  override suspend fun setSubscriptionPreference(subscribe: Boolean) {
    dataStore.edit {
      it[subscriptionKey] = subscribe.toString()
    }
  }

  override fun observeSubscriptionPreference(): Flow<Boolean> {
    return dataStore.data.map { preferences ->
      preferences[subscriptionKey]?.let {
        it.toBoolean()
      } ?: true
      // here we assume that member is subscribed by default in customer.io
    }
  }

  companion object {
    private val themeKey = stringPreferencesKey("settings-theme")
    private val subscriptionKey = stringPreferencesKey("settings-email-subscription")
  }
}
