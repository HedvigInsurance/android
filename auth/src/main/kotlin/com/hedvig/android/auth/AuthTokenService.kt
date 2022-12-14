package com.hedvig.android.auth

import com.hedvig.android.auth.token.LocalAccessToken
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.RefreshToken
import kotlinx.coroutines.flow.StateFlow

interface AuthTokenService {
  val authStatus: StateFlow<AuthStatus?>
  fun getToken(): LocalAccessToken?
  suspend fun refreshAndGetToken(): LocalAccessToken?
  fun updateTokens(accessToken: AccessToken, refreshToken: RefreshToken)
  fun invalidateTokens()
  suspend fun migrateFromToken(token: String)
}
