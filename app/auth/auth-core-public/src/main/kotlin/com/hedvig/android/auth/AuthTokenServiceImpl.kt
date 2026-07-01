package com.hedvig.android.auth

import com.hedvig.android.auth.event.AuthEventStorage
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.auth.token.AuthTokens
import com.hedvig.android.auth.token.LocalRefreshToken
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.RefreshToken
import com.hedvig.authlib.RefreshTokenGrant
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class AuthTokenServiceImpl(
  private val authTokenStorage: AuthTokenStorage,
  private val authRepository: AuthRepository,
  private val authEventStorage: AuthEventStorage,
  coroutineScope: ApplicationScope,
  private val clock: Clock = Clock.System,
) : AuthTokenService {
  override val authStatus: StateFlow<AuthStatus?> = authTokenStorage.getTokens()
    .mapLatest { authTokens ->
      val tokens = authTokens ?: return@mapLatest AuthStatus.LoggedOut
      // A stored session whose refresh token has already expired is unrecoverable. There is no valid
      // grant left to exchange, so the next authenticated request would fail its refresh and clear the
      // tokens anyway. Reporting LoggedIn here roots the app in the logged-in scene for a few frames,
      // then the forced logout flips it back to Login, a rapid re-root that crashes Nav3's scene
      // SaveableStateHolder ("Key <X> was used multiple times", b/516312097). Treat it as logged out up
      // front. Expiry is evaluated whenever storage emits; an in-session expiry is still handled by the
      // request-path refresh failure, which clears tokens and re-triggers this mapping.
      if (tokens.refreshToken.expiryDate <= clock.now()) {
        return@mapLatest AuthStatus.LoggedOut
      }
      AuthStatus.LoggedIn(tokens.accessToken, tokens.refreshToken)
    }
    .stateIn(
      coroutineScope,
      SharingStarted.Eagerly,
      null,
    )

  override suspend fun getTokens(): AuthTokens? {
    return authTokenStorage.getTokens().first()
  }

  override suspend fun refreshAndGetAccessToken(): AccessToken? {
    val refreshToken = getRefreshToken() ?: return null
    return when (val result = authRepository.exchange(RefreshTokenGrant(refreshToken.token))) {
      is AuthTokenResult.Error -> {
        logcat { "Refreshing token failed. Invalidating present tokens" }
        logoutAndInvalidateTokens()
        null
      }

      is AuthTokenResult.Success -> {
        logcat(LogPriority.VERBOSE) { "Refreshing token success. Updating tokens" }
        authTokenStorage.updateTokens(result.accessToken, result.refreshToken)
        result.accessToken
      }
    }
  }

  override suspend fun loginWithTokens(accessToken: AccessToken, refreshToken: RefreshToken) {
    authTokenStorage.updateTokens(accessToken, refreshToken)
    authEventStorage.loggedIn(accessToken.token, refreshToken.token)
  }

  override suspend fun logoutAndInvalidateTokens() {
    authTokenStorage.clearTokens()
    authEventStorage.loggedOut()
  }

  private suspend fun getRefreshToken(): LocalRefreshToken? {
    return authTokenStorage.getTokens().first()?.refreshToken
  }
}
