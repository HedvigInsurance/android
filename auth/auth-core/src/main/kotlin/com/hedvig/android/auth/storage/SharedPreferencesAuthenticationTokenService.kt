package com.hedvig.android.auth.storage

import android.content.SharedPreferences

private const val SHARED_PREFERENCE_AUTHENTICATION_TOKEN = "shared_preference_authentication_token"

// Storage for old authentication token
class SharedPreferencesAuthenticationTokenService(
  private val sharedPreferences: SharedPreferences,
) {
  var authenticationToken: String?
    set(value) = sharedPreferences.edit()
      .putString(SHARED_PREFERENCE_AUTHENTICATION_TOKEN, value)
      .apply()
    get() = sharedPreferences.getString(SHARED_PREFERENCE_AUTHENTICATION_TOKEN, null)
}
