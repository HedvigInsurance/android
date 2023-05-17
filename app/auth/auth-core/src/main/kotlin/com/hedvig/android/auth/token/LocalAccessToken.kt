package com.hedvig.android.auth.token

import kotlinx.datetime.Instant

data class LocalAccessToken(
  val token: String,
  val expiryDate: Instant,
)
