package com.hedvig.android.auth.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.android.auth.AccessToken
import com.hedvig.android.auth.RefreshCode
import com.hedvig.android.auth.RefreshToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// TODO Work with expiration dates.
internal class AuthTokenStorage(
  private val dataStore: DataStore<Preferences>,
) {
  fun getTokens(): Flow<Pair<AccessToken?, RefreshToken?>> {
    return dataStore.data
      .map { preferences ->
        preferences[accessTokenPreferenceKey] to preferences[refreshTokenPreferenceKey]
      }
      .map { (accessToken, refreshToken) ->
        Pair(
          first = accessToken?.let { AccessToken(it, 0) },
          second = refreshToken?.let { RefreshToken(RefreshCode(it), 0) },
        )
      }
  }

  suspend fun updateTokens(accessToken: AccessToken, refreshToken: RefreshToken) {
    dataStore.edit { preferences ->
      preferences[accessTokenPreferenceKey] = accessToken.token
      preferences[refreshTokenPreferenceKey] = refreshToken.token.code
    }
  }

  suspend fun clearTokens() {
    dataStore.edit { preferences ->
      preferences.remove(accessTokenPreferenceKey)
      preferences.remove(refreshTokenPreferenceKey)
    }
  }

  companion object {
    private val accessTokenPreferenceKey = stringPreferencesKey("com.hedvig.android.auth.storage.ACCESS_TOKEN")
    private val refreshTokenPreferenceKey = stringPreferencesKey("com.hedvig.android.auth.storage.REFRESH_TOKEN")
  }
}
