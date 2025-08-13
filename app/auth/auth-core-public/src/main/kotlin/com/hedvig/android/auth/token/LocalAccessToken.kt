package com.hedvig.android.auth.token

import kotlin.time.Instant

data class LocalAccessToken(
  val token: String,
  val expiryDate: Instant,
)
