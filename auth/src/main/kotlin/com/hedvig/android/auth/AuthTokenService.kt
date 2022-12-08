package com.hedvig.android.auth

import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.RefreshToken
import com.hedvig.authlib.RefreshTokenGrant
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AuthStatus {
  data class LoggedIn(val accessToken: AccessToken, val refreshToken: RefreshToken) : AuthStatus
  object LoggedOut : AuthStatus
}

interface AuthTokenService {
  fun getToken(): AccessToken?
  suspend fun refreshAndGetToken(): AccessToken?
  fun updateTokens(accessToken: AccessToken, refreshToken: RefreshToken)
  fun invalidateTokens()
  fun authStatus(): StateFlow<AuthStatus?>
}

internal class AuthTokenServiceImpl(
  private val authTokenStorage: AuthTokenStorage,
  private val authRepository: AuthRepository,
  private val applicationScope: ApplicationScope,
) : AuthTokenService {

  @Suppress("NAME_SHADOWING")
  private val authStatus: StateFlow<AuthStatus?> = authTokenStorage.getTokens()
    .mapLatest { (accessToken, refreshToken) ->
      val accessToken = accessToken ?: return@mapLatest AuthStatus.LoggedOut
      val refreshToken = refreshToken ?: return@mapLatest AuthStatus.LoggedOut
      AuthStatus.LoggedIn(accessToken, refreshToken)
    }
    .stateIn(
      applicationScope,
      SharingStarted.Eagerly,
      null,
    )

  override fun getToken(): AccessToken? {
    return (authStatus.value as? AuthStatus.LoggedIn)?.accessToken
  }

  override suspend fun refreshAndGetToken(): AccessToken? {
    val refreshToken = getRefreshToken() ?: return null
    return when (val result = authRepository.exchange(RefreshTokenGrant(refreshToken.token))) {
      is AuthTokenResult.Error -> {
        authTokenStorage.clearTokens()
        null
      }
      is AuthTokenResult.Success -> {
        authTokenStorage.updateTokens(
          result.accessToken,
          result.refreshToken,
        )
        result.accessToken
      }
    }
  }

  override fun updateTokens(accessToken: AccessToken, refreshToken: RefreshToken) {
    applicationScope.launch {
      authTokenStorage.updateTokens(accessToken, refreshToken)
    }
  }

  override fun invalidateTokens() {
    applicationScope.launch {
      authTokenStorage.clearTokens()
    }
  }

  override fun authStatus(): StateFlow<AuthStatus?> {
    return authStatus
  }

  private fun getRefreshToken(): RefreshToken? {
    return (authStatus.value as? AuthStatus.LoggedIn)?.refreshToken
  }
}
