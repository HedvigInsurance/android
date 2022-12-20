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
    d { "Got accessToken: $accessToken" }
    if (accessToken?.expiryDate?.isExpired()?.not() == true) {
      d { "Current AccessToken not expired, fast track to adding the header" }
      return chain.proceed(chain.request().withAuthorizationToken(accessToken.token))
    }
    d { "AccessToken was expired or not cached yet, going to try to refresh it" }
    val retrievedAccessToken: String? = runBlocking {
      mutex.withLock {
        val authTokens = authTokenService.getTokens()
          ?: return@withLock null.also { d { "Tokens were invalidated by the previous lock holder" } }
        if (authTokens.accessToken.expiryDate.isExpired().not()) {
          d { "After lock, token was refreshed, proceeding with refreshed token" }
          return@withLock authTokens.accessToken.token
        }
        d { "Still an expired token at this point, try to refresh it" }
        if (authTokens.refreshToken.expiryDate.isExpired()) {
          d { "Refresh token was expired, invalidating tokens and proceeding unauthenticated" }
          // If refresh is also expired, consider ourselves logged out
          authTokenService.invalidateTokens()
          return@withLock null
        }
        d { "Expired access, but not expired refresh token, refreshing tokens now" }
        val refreshedAccessToken = authTokenService.refreshAndGetAccessToken() ?: return@withLock null.also {
          d { "Refreshing failed, proceed unauthenticated" }
        }
        d { "Refreshing succeeded, proceeding with refreshed token" }
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
  }
}

private fun Request.withAuthorizationToken(token: String): Request {
  return newBuilder().header("Authorization", token).build()
}
