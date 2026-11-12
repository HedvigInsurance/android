package com.hedvig.android.auth.token

import kotlin.time.Instant

data class LocalRefreshToken(
  val token: String,
  val expiryDate: Instant,
) {
  /**
   * Redacted, so that interpolating this token, or anything holding it, can never publish a live
   * bearer credential to a log sink.
   */
  override fun toString(): String = "LocalRefreshToken(token=REDACTED, expiryDate=$expiryDate)"
}
