package com.hedvig.android.auth

import com.hedvig.android.auth.token.isTokenExpired
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.UUID
import kotlin.time.Clock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class AndroidAccessTokenProvider(
  private val authTokenService: AuthTokenService,
  private val clock: Clock = Clock.System,
) : AccessTokenProvider {
  private val mutex = Mutex()

  override suspend fun provide(): String? {
    val requestId = "#${UUID.randomUUID().toString().takeLast(6)}"
    val accessToken = (authTokenService.authStatus.value as? AuthStatus.LoggedIn)?.accessToken
    logcat(LogPriority.VERBOSE) { "$requestId Got accessToken: $accessToken" }
    if (accessToken?.expiryDate?.isTokenExpired(clock)?.not() == true) {
      logcat(LogPriority.VERBOSE) { "$requestId Current AccessToken not expired, fast track to adding the header" }
      return accessToken.token
    }
    return mutex.withLock {
      val authTokens = authTokenService.getTokens() ?: return@withLock null.also {
        logcat(LogPriority.VERBOSE) { "$requestId Tokens were not stored, proceeding unauthenticated" }
      }
      if (authTokens.accessToken.expiryDate.isTokenExpired(clock).not()) {
        logcat(LogPriority.VERBOSE) { "$requestId After lock, token was refreshed, proceeding with refreshed token" }
        return@withLock authTokens.accessToken.token
      }
      logcat(LogPriority.VERBOSE) { "$requestId Still an expired token at this point, try to refresh it" }
      if (authTokens.refreshToken.expiryDate.isTokenExpired(clock)) {
        logcat { "$requestId Refresh token expired, invalidating tokens and proceeding unauthenticated" }
        // If refresh is also expired, consider ourselves logged out
        authTokenService.logoutAndInvalidateTokens()
        return@withLock null
      }
      logcat(LogPriority.VERBOSE) {
        "$requestId Access token expired, but not expired refresh token, refreshing tokens now"
      }
      val refreshedAccessToken = authTokenService.refreshAndGetAccessToken() ?: return@withLock null.also {
        logcat(LogPriority.VERBOSE) { "$requestId Refreshing failed, proceed unauthenticated" }
      }
      logcat(LogPriority.VERBOSE) { "$requestId Refreshing succeeded, proceeding with refreshed tokens" }
      refreshedAccessToken.token
    }
  }
}
