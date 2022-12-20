package com.hedvig.android.auth.interceptor

import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import slimber.log.d
import kotlin.time.Duration.Companion.seconds

class AuthTokenRefreshingInterceptor(
  private val authTokenService: AuthTokenService,
  private val clock: Clock = Clock.System,
) : Interceptor {

  private val mutex = Mutex()

  override fun intercept(chain: Interceptor.Chain): Response {
    val accessToken = (authTokenService.authStatus.value as? AuthStatus.LoggedIn)?.accessToken
    d { "${chain.identifyingCode()} Got accessToken: $accessToken" }
    if (accessToken?.expiryDate?.isExpired()?.not() == true) {
      d { "${chain.identifyingCode()} Current AccessToken not expired, fast track to adding the header" }
      return chain.proceed(chain.request().withAuthorizationToken(accessToken.token))
    }
    d { "${chain.identifyingCode()} AccessToken was expired or not cached yet, going to try to refresh it" }
    val retrievedAccessToken: String? = runBlocking {
      mutex.withLock {
        val authTokens = authTokenService.getTokens() ?: return@withLock null.also {
          d { "${chain.identifyingCode()} Tokens were not stored, proceeding unauthenticated" }
        }
        if (authTokens.accessToken.expiryDate.isExpired().not()) {
          d { "${chain.identifyingCode()} After lock, token was refreshed, proceeding with refreshed token" }
          return@withLock authTokens.accessToken.token
        }
        d { "${chain.identifyingCode()} Still an expired token at this point, try to refresh it" }
        if (authTokens.refreshToken.expiryDate.isExpired()) {
          d { "${chain.identifyingCode()} Refresh token expired, invalidating tokens and proceeding unauthenticated" }
          // If refresh is also expired, consider ourselves logged out
          authTokenService.invalidateTokens()
          return@withLock null
        }
        d { "${chain.identifyingCode()} Access token expired, but not expired refresh token, refreshing tokens now" }
        val refreshedAccessToken = authTokenService.refreshAndGetAccessToken() ?: return@withLock null.also {
          d { "${chain.identifyingCode()} Refreshing failed, proceed unauthenticated" }
        }
        d { "${chain.identifyingCode()} Refreshing succeeded, proceeding with refreshed tokens" }
        refreshedAccessToken.token
      }
    }
    return if (retrievedAccessToken != null) {
      chain.proceed(chain.request().withAuthorizationToken(retrievedAccessToken))
    } else {
      chain.proceed(chain.request())
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

    /**
     * Returns a unique ID from the chain. Used only for logging, to deduce which interceptor is doing what.
     */
    private fun Interceptor.Chain.identifyingCode(): String {
      return "#${hashCode().toString(16)}"
    }
  }
}

private fun Request.withAuthorizationToken(token: String): Request {
  return newBuilder().header("Authorization", token).build()
}
