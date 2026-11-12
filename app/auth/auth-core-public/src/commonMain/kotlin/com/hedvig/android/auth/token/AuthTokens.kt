package com.hedvig.android.auth.token

data class AuthTokens(
  val accessToken: LocalAccessToken,
  val refreshToken: LocalRefreshToken,
)
