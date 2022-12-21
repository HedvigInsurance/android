package com.hedvig.android.auth

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import slimber.log.d
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

internal class AndroidAccessTokenProvider(
  private val authTokenService: AuthTokenService,
  private val clock: Clock = Clock.System,
) : AccessTokenProvider {
  private val mutex = Mutex()

  override suspend fun provide(): String? {
    val requestId = UUID.randomUUID().leastSignificantBits.toString(16)
    val accessToken = (authTokenService.authStatus.value as? AuthStatus.LoggedIn)?.accessToken
    d { "$requestId Got accessToken: $accessToken" }
    if (accessToken?.expiryDate?.isExpired()?.not() == true) {
      d { "$requestId Current AccessToken not expired, fast track to adding the header" }
      return accessToken.token
    }
    return mutex.withLock {
      val authTokens = authTokenService.getTokens() ?: return@withLock null.also {
        d { "$requestId Tokens were not stored, proceeding unauthenticated" }
      }
      if (authTokens.accessToken.expiryDate.isExpired().not()) {
        d { "$requestId After lock, token was refreshed, proceeding with refreshed token" }
        return@withLock authTokens.accessToken.token
      }
      d { "$requestId Still an expired token at this point, try to refresh it" }
      if (authTokens.refreshToken.expiryDate.isExpired()) {
        d { "$requestId Refresh token expired, invalidating tokens and proceeding unauthenticated" }
        // If refresh is also expired, consider ourselves logged out
        authTokenService.invalidateTokens()
        return@withLock null
      }
      d { "$requestId Access token expired, but not expired refresh token, refreshing tokens now" }
      val refreshedAccessToken = authTokenService.refreshAndGetAccessToken() ?: return@withLock null.also {
        d { "$requestId Refreshing failed, proceed unauthenticated" }
      }
      d { "$requestId Refreshing succeeded, proceeding with refreshed tokens" }
      refreshedAccessToken.token
    }
  }

  private fun Instant.isExpired(): Boolean {
    val bufferAdjustedExpirationInstant = this - expirationTimeBuffer
    return bufferAdjustedExpirationInstant <= clock.now()
  }

  companion object {
    /**
     * Assume the token expires a bit earlier than it really does, to give some room for the network requests and such
     * to run and not risk assuming it's active but until the request goes through it's already become invalidated.
     */
    private val expirationTimeBuffer = 60.seconds
  }
}
