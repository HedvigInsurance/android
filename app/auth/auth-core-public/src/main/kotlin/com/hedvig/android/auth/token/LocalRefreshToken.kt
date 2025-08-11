package com.hedvig.android.auth.token

import kotlin.time.Instant

data class LocalRefreshToken(
  val token: String,
  val expiryDate: Instant,
)
