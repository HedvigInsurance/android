package com.hedvig.android.auth

import com.hedvig.android.auth.token.LocalAccessToken
import com.hedvig.android.auth.token.LocalRefreshToken

data class AuthTokens(
  val accessToken: LocalAccessToken,
  val refreshToken: LocalRefreshToken,
)
