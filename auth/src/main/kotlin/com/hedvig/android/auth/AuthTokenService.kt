package com.hedvig.android.auth

import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.RefreshToken
import kotlinx.coroutines.flow.StateFlow

interface AuthTokenService {
  fun getToken(): AccessToken?
  suspend fun refreshAndGetToken(): AccessToken?
  fun updateTokens(accessToken: AccessToken, refreshToken: RefreshToken)
  fun invalidateTokens()
  fun authStatus(): StateFlow<AuthStatus?>
}
