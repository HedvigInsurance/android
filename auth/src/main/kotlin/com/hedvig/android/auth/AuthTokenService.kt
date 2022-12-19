package com.hedvig.android.auth

import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.RefreshToken
import kotlinx.coroutines.flow.StateFlow

interface AuthTokenService {
  val authStatus: StateFlow<AuthStatus?>
  suspend fun getTokens(): AuthTokens?
  suspend fun refreshAndGetAccessToken(): AccessToken?
  suspend fun updateTokens(accessToken: AccessToken, refreshToken: RefreshToken)
  suspend fun invalidateTokens()
  suspend fun migrateFromToken(token: String)
}
