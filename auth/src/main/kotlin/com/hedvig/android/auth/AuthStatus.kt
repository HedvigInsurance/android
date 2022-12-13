package com.hedvig.android.auth

import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.RefreshToken

sealed interface AuthStatus {
  data class LoggedIn(val accessToken: AccessToken, val refreshToken: RefreshToken) : AuthStatus
  object LoggedOut : AuthStatus
}
