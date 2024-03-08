package com.hedvig.android.auth.test

import app.cash.turbine.Turbine
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.event.AuthEvent
import com.hedvig.android.auth.token.AuthTokens
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.RefreshToken
import kotlinx.coroutines.flow.StateFlow

class TestAuthTokenService : AuthTokenService {
  val authTokensTurbine = Turbine<AuthTokens?>()

  val authEventTurbine = Turbine<AuthEvent>()

  override val authStatus: StateFlow<AuthStatus?>
    get() = error("Not implemented")

  override suspend fun getTokens(): AuthTokens? = authTokensTurbine.awaitItem()

  override suspend fun refreshAndGetAccessToken(): AccessToken? {
    error("Not implemented")
  }

  override suspend fun loginWithTokens(accessToken: AccessToken, refreshToken: RefreshToken) {
    authEventTurbine.add(AuthEvent.LoggedIn(accessToken.token, refreshToken.token))
  }

  override suspend fun logoutAndInvalidateTokens() {
    authEventTurbine.add(AuthEvent.LoggedOut)
  }
}
