package com.hedvig.android.auth.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.RefreshToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

class AuthTokenStorage(
  private val dataStore: DataStore<Preferences>,
) {
  fun getTokens(): Flow<Pair<AccessToken, RefreshToken>?> {
    return dataStore.data
      .map { preferences ->
        val now = Clock.System.now()

        val accessTokenString = preferences[accessTokenPreferenceKey] ?: return@map null
        val accessTokenExpirationEpochMilliseconds =
          preferences[accessTokenExpirationEpochMillisecondsPreferenceKey] ?: return@map null
        val accessTokenExpirationInstant = Instant.fromEpochMilliseconds(accessTokenExpirationEpochMilliseconds)
        val accessTokenExpirationInSecondsFromNow =
          (accessTokenExpirationInstant.epochSeconds - now.epochSeconds).toInt()

        val refreshTokenString = preferences[refreshTokenPreferenceKey] ?: return@map null
        val refreshTokenExpirationEpochMilliseconds =
          preferences[refreshTokenExpirationEpochMillisecondsPreferenceKey] ?: return@map null
        val refreshTokenExpirationInstant = Instant.fromEpochMilliseconds(refreshTokenExpirationEpochMilliseconds)
        val refreshTokenExpirationInSecondsFromNow =
          (refreshTokenExpirationInstant.epochSeconds - now.epochSeconds).toInt()

        Pair(
          AccessToken(accessTokenString, accessTokenExpirationInSecondsFromNow),
          RefreshToken(refreshTokenString, refreshTokenExpirationInSecondsFromNow),
        )
      }
  }

  suspend fun updateTokens(accessToken: AccessToken, refreshToken: RefreshToken) {
    dataStore.edit { preferences ->
      val now = Clock.System.now()

      preferences[accessTokenPreferenceKey] = accessToken.token
      val accessTokenExpirationInstant = now + accessToken.expiryInSeconds.seconds - expirationTimeBuffer
      preferences[accessTokenExpirationEpochMillisecondsPreferenceKey] =
        accessTokenExpirationInstant.toEpochMilliseconds()

      preferences[refreshTokenPreferenceKey] = refreshToken.token
      val refreshTokenExpirationInstant = now + refreshToken.expiryInSeconds.seconds - expirationTimeBuffer
      preferences[refreshTokenExpirationEpochMillisecondsPreferenceKey] =
        refreshTokenExpirationInstant.toEpochMilliseconds()
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
    private val accessTokenExpirationEpochMillisecondsPreferenceKey =
      longPreferencesKey("com.hedvig.android.auth.storage.ACCESS_TOKEN_EXPIRATION_EPOCH_MILLISECONDS")
    private val refreshTokenPreferenceKey = stringPreferencesKey("com.hedvig.android.auth.storage.REFRESH_TOKEN")
    private val refreshTokenExpirationEpochMillisecondsPreferenceKey =
      longPreferencesKey("com.hedvig.android.auth.storage.REFRESH_TOKEN_EXPIRATION_EPOCH_MILLISECONDS")

    /**
     * Assume the token expires a bit earlier than it really does, to give some room for the network requests and such
     * to run and not risk assuming it's active but until the request goes through it's already become invalidated.
     */
    val expirationTimeBuffer = 60.seconds
  }
}
