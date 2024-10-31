package com.hedvig.android.auth.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.auth.token.AuthTokens
import com.hedvig.android.auth.token.LocalAccessToken
import com.hedvig.android.auth.token.LocalRefreshToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class AuthTokenStorage(
  private val dataStore: DataStore<Preferences>,
  private val clock: Clock = Clock.System,
) {
  fun getTokens(): Flow<AuthTokens?> {
    return dataStore.data
      .map { preferences ->
        val accessTokenString = preferences[accessTokenPreferenceKey] ?: return@map null
        val accessTokenExpirationIso8601 = preferences[accessTokenExpirationIso8601PreferenceKey] ?: return@map null
        val accessTokenExpirationInstant = Instant.parse(accessTokenExpirationIso8601)

        val refreshTokenString = preferences[refreshTokenPreferenceKey] ?: return@map null
        val refreshTokenExpirationIso8601 = preferences[refreshTokenExpirationIso8601PreferenceKey] ?: return@map null
        val refreshTokenExpirationInstant = Instant.parse(refreshTokenExpirationIso8601)

        AuthTokens(
          LocalAccessToken(accessTokenString, accessTokenExpirationInstant),
          LocalRefreshToken(refreshTokenString, refreshTokenExpirationInstant),
        )
      }
  }

  suspend fun updateTokens() {}

  suspend fun clearTokens() {
    dataStore.edit { preferences ->
      preferences.remove(accessTokenPreferenceKey)
      preferences.remove(refreshTokenPreferenceKey)
    }
  }

  companion object {
    private val accessTokenPreferenceKey = stringPreferencesKey("com.hedvig.android.auth.storage.ACCESS_TOKEN")
    private val accessTokenExpirationIso8601PreferenceKey =
      stringPreferencesKey("com.hedvig.android.auth.storage.ACCESS_TOKEN_EXPIRATION_ISO_8601")
    private val refreshTokenPreferenceKey = stringPreferencesKey("com.hedvig.android.auth.storage.REFRESH_TOKEN")
    private val refreshTokenExpirationIso8601PreferenceKey =
      stringPreferencesKey("com.hedvig.android.auth.storage.REFRESH_TOKEN_EXPIRATION_ISO_8601")
  }
}
