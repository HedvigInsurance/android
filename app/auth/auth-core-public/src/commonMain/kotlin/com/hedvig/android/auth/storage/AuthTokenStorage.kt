package com.hedvig.android.auth.storage

import com.hedvig.android.auth.token.AuthTokens
import com.hedvig.android.authlib.AccessToken
import com.hedvig.android.authlib.RefreshToken
import kotlinx.coroutines.flow.Flow

interface AuthTokenStorage {
  fun getTokens(): Flow<AuthTokens?>

  suspend fun updateTokens(accessToken: AccessToken, refreshToken: RefreshToken)

  suspend fun clearTokens()
}
