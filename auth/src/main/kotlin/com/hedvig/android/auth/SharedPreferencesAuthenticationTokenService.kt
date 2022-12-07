package com.hedvig.android.auth

import android.content.SharedPreferences
import com.hedvig.authlib.RefreshToken

private const val SHARED_PREFERENCE_AUTHENTICATION_TOKEN = "shared_preference_authentication_token"
private const val SHARED_PREFERENCE_REFRESH_TOKEN = "shared_preference_refresh_token"
private const val SHARED_PREFERENCE_REFRESH_TOKEN_EXPIRY = "shared_preference_refresh_token_expiry"

internal class SharedPreferencesAuthenticationTokenService(
  private val sharedPreferences: SharedPreferences,
) : AuthenticationTokenService {
  override var authenticationToken: String?
    set(value) = sharedPreferences.edit()
      .putString(SHARED_PREFERENCE_AUTHENTICATION_TOKEN, value)
      .apply()
    get() = sharedPreferences.getString(SHARED_PREFERENCE_AUTHENTICATION_TOKEN, null)

  override var refreshToken: RefreshToken?
    set(value) {
      sharedPreferences.edit()
        .putString(SHARED_PREFERENCE_REFRESH_TOKEN, value?.token)
        .apply()

      sharedPreferences.edit()
        .putInt(SHARED_PREFERENCE_REFRESH_TOKEN_EXPIRY, value?.expiryInSeconds ?: 0)
        .apply()
    }
    get() {
      val code = sharedPreferences.getString(SHARED_PREFERENCE_REFRESH_TOKEN, "")
      val expiry = sharedPreferences.getInt(SHARED_PREFERENCE_REFRESH_TOKEN_EXPIRY, 0)

      return code?.let {
        RefreshToken(it, expiry)
      }
    }
}
