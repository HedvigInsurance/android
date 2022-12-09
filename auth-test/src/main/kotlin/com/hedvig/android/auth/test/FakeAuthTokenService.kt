package com.hedvig.android.auth.test

import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.RefreshToken
import kotlinx.coroutines.flow.StateFlow

class FakeAuthTokenService : AuthTokenService {
  override fun getToken(): AccessToken? {
    TODO("Not yet implemented")
  }

  override suspend fun refreshAndGetToken(): AccessToken? {
    TODO("Not yet implemented")
  }

  override fun updateTokens(
    accessToken: AccessToken,
    refreshToken: RefreshToken,
  ) {
    TODO("Not yet implemented")
  }

  override fun invalidateTokens() {
    TODO("Not yet implemented")
  }

  override fun authStatus(): StateFlow<AuthStatus?> {
    TODO("Not yet implemented")
  }
}
