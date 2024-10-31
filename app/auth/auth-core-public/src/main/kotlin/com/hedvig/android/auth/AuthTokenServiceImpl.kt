package com.hedvig.android.auth

import com.hedvig.android.auth.token.AuthTokens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class AuthTokenServiceImpl() : AuthTokenService {
  override val authStatus: StateFlow<AuthStatus?> = MutableStateFlow(null)

  override suspend fun getTokens(): AuthTokens? {
    return null
  }

  override suspend fun refreshAndGetAccessToken() {
  }

  override suspend fun loginWithTokens(accessToken: Unit, refreshToken: Unit) {
  }

  override suspend fun logoutAndInvalidateTokens() {
  }
}
