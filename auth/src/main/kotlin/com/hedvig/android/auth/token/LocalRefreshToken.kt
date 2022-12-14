package com.hedvig.android.auth.token

import kotlinx.datetime.Instant

data class LocalRefreshToken(
  val token: String,
  val expiryDate: Instant,
)
