package com.hedvig.android.auth.test

import app.cash.turbine.Turbine
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.event.AuthEvent
import com.hedvig.android.auth.token.AuthTokens
import com.hedvig.android.auth.token.LocalAccessToken
import com.hedvig.android.auth.token.LocalRefreshToken
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.RefreshToken
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TestAuthTokenService : AuthTokenService {
  val authEventTurbine = Turbine<AuthEvent>()

  private val _authStatus: MutableStateFlow<AuthStatus?> = MutableStateFlow(null)
  override val authStatus: StateFlow<AuthStatus?>
    get() = _authStatus.asStateFlow()

  override suspend fun getTokens(): AuthTokens? = error("Not implemented")

  override suspend fun refreshAndGetAccessToken(): AccessToken? {
    error("Not implemented")
  }

  override suspend fun loginWithTokens(accessToken: AccessToken, refreshToken: RefreshToken) {
    authEventTurbine.add(AuthEvent.LoggedIn(accessToken.token, refreshToken.token))
    _authStatus.update {
      val now = Clock.System.now()
      AuthStatus.LoggedIn(
        LocalAccessToken(accessToken.token, now.plus(accessToken.expiryInSeconds.seconds)),
        LocalRefreshToken(refreshToken.token, now.plus(refreshToken.expiryInSeconds.seconds)),
      )
    }
  }

  override suspend fun logoutAndInvalidateTokens() {
    authEventTurbine.add(AuthEvent.LoggedOut)
    _authStatus.update { AuthStatus.LoggedOut }
  }
}
