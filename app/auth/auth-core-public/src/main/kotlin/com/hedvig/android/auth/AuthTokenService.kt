package com.hedvig.android.auth

import com.hedvig.android.auth.token.AuthTokens
import kotlinx.coroutines.flow.StateFlow

interface AuthTokenService {
  val authStatus: StateFlow<AuthStatus?>

  suspend fun getTokens(): AuthTokens?

//  suspend fun refreshAndGetAccessToken(): AccessToken?

  /**
   * Effectively the function which logs the user *in* the app. Should only be called with valid tokens
   */
//  suspend fun loginWithTokens(accessToken: AccessToken, refreshToken: RefreshToken)

  /**
   * Effectively the function which logs the user *out* from the app.
   */
  suspend fun logoutAndInvalidateTokens()
}
