package com.hedvig.android.auth

import com.hedvig.android.auth.token.LocalAccessToken
import com.hedvig.android.auth.token.LocalRefreshToken

sealed interface AuthStatus {
  data class LoggedIn(
    val accessToken: LocalAccessToken,
    val refreshToken: LocalRefreshToken,
  ) : AuthStatus

  data object LoggedOut : AuthStatus
}
