package com.hedvig.android.auth

import com.hedvig.android.core.common.android.ifPlanted
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import slimber.log.v
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

internal class AndroidAccessTokenProvider(
  private val authTokenService: AuthTokenService,
  private val clock: Clock = Clock.System,
) : AccessTokenProvider {
  private val mutex = Mutex()

  override suspend fun provide(): String? {
    val requestId = ifPlanted { "#${UUID.randomUUID().toString().takeLast(6)}" }
    val accessToken = (authTokenService.authStatus.value as? AuthStatus.LoggedIn)?.accessToken
    v { "$requestId Got accessToken: $accessToken" }
    if (accessToken?.expiryDate?.isExpired()?.not() == true) {
      v { "$requestId Current AccessToken not expired, fast track to adding the header" }
      return accessToken.token
    }
    return mutex.withLock {
      val authTokens = authTokenService.getTokens() ?: return@withLock null.also {
        v { "$requestId Tokens were not stored, proceeding unauthenticated" }
      }
      if (authTokens.accessToken.expiryDate.isExpired().not()) {
        v { "$requestId After lock, token was refreshed, proceeding with refreshed token" }
        return@withLock authTokens.accessToken.token
      }
      v { "$requestId Still an expired token at this point, try to refresh it" }
      if (authTokens.refreshToken.expiryDate.isExpired()) {
        v { "$requestId Refresh token expired, invalidating tokens and proceeding unauthenticated" }
        // If refresh is also expired, consider ourselves logged out
        authTokenService.invalidateTokens()
        return@withLock null
      }
      v { "$requestId Access token expired, but not expired refresh token, refreshing tokens now" }
      val refreshedAccessToken = authTokenService.refreshAndGetAccessToken() ?: return@withLock null.also {
        v { "$requestId Refreshing failed, proceed unauthenticated" }
      }
      v { "$requestId Refreshing succeeded, proceeding with refreshed tokens" }
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
