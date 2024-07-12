package com.hedvig.android.auth

import com.hedvig.android.auth.event.AuthEventStorage
import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.auth.token.AuthTokens
import com.hedvig.android.auth.token.LocalRefreshToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

internal class AuthTokenServiceImpl(
  private val authTokenStorage: AuthTokenStorage,
//  private val authRepository: AuthRepository,
  private val authEventStorage: AuthEventStorage,
  coroutineScope: CoroutineScope,
) : AuthTokenService {
  override val authStatus: StateFlow<AuthStatus?> = authTokenStorage
    .getTokens()
    .mapLatest { authTokens ->
      val (accessToken, refreshToken) = authTokens ?: return@mapLatest AuthStatus.LoggedOut
      AuthStatus.LoggedIn(accessToken, refreshToken)
    }.stateIn(
      coroutineScope,
      SharingStarted.Eagerly,
      null,
    )

  override suspend fun getTokens(): AuthTokens? {
    return authTokenStorage.getTokens().first()
  }

//  override suspend fun refreshAndGetAccessToken(): AccessToken? {
//    val refreshToken = getRefreshToken() ?: return null
//    return when (val result = authRepository.exchange(RefreshTokenGrant(refreshToken.token))) {
//      is AuthTokenResult.Error -> {
//        logcat { "Refreshing token failed. Invalidating present tokens" }
//        logoutAndInvalidateTokens()
//        null
//      }
//      is AuthTokenResult.Success -> {
//        logcat(LogPriority.VERBOSE) { "Refreshing token success. Updating tokens" }
//        authTokenStorage.updateTokens(result.accessToken, result.refreshToken)
//        result.accessToken
//      }
//    }
//  }

//  override suspend fun loginWithTokens(accessToken: AccessToken, refreshToken: RefreshToken) {
//    authTokenStorage.updateTokens(accessToken, refreshToken)
//    authEventStorage.loggedIn(accessToken.token, refreshToken.token)
//  }

  override suspend fun logoutAndInvalidateTokens() {
    authTokenStorage.clearTokens()
    authEventStorage.loggedOut()
  }

  private suspend fun getRefreshToken(): LocalRefreshToken? {
    return authTokenStorage.getTokens().first()?.refreshToken
  }
}
