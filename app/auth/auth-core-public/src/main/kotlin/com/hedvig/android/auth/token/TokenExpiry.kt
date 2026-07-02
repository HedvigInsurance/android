package com.hedvig.android.auth.token

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * How long before its real expiry a token is already treated as expired, to leave room for the
 * network round-trip that will use it: a token still valid at check time can lapse before the request
 * (or the token refresh) it authorizes completes.
 */
internal val TOKEN_EXPIRATION_BUFFER: Duration = 60.seconds

/** True when this expiry instant is at, past, or within [buffer] of [clock]'s current time. */
internal fun Instant.isTokenExpired(clock: Clock, buffer: Duration = TOKEN_EXPIRATION_BUFFER): Boolean {
  return this - buffer <= clock.now()
}
