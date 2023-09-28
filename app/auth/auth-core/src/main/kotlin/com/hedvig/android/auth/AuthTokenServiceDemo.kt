package com.hedvig.android.auth

import com.hedvig.android.auth.token.AuthTokens
import com.hedvig.android.auth.token.LocalAccessToken
import com.hedvig.android.auth.token.LocalRefreshToken
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.RefreshToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant

class AuthTokenServiceDemo : AuthTokenService {

  private val accessToken = LocalAccessToken("123", expiryDate = Instant.DISTANT_FUTURE)
  private val refreshToken = LocalRefreshToken("1234", expiryDate = Instant.DISTANT_FUTURE)

  private val testTokens = AuthTokens(
    accessToken = accessToken,
    refreshToken = refreshToken,
  )

  private val loggedInStatus = AuthStatus.LoggedIn(
    accessToken = LocalAccessToken("123", expiryDate = Instant.DISTANT_FUTURE),
    refreshToken = LocalRefreshToken("1234", expiryDate = Instant.DISTANT_FUTURE),
  )

  private val _authStatus: MutableStateFlow<AuthStatus?> = MutableStateFlow(loggedInStatus)
  override val authStatus: StateFlow<AuthStatus?> = _authStatus

  override suspend fun getTokens(): AuthTokens? = testTokens

  override suspend fun refreshAndGetAccessToken(): AccessToken? = AccessToken(
    "test",
    1000000000,
  )

  override suspend fun loginWithTokens(accessToken: AccessToken, refreshToken: RefreshToken) {

  }

  override suspend fun logoutAndInvalidateTokens() {
    _authStatus.value = AuthStatus.LoggedOut
    _authStatus.value = loggedInStatus
  }

  override suspend fun migrateFromToken(token: String) {

  }
}
