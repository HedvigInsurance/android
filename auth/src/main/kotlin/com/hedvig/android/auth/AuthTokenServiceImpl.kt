package com.hedvig.android.auth

import com.hedvig.android.auth.storage.AuthTokenStorage
import com.hedvig.authlib.AccessToken
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.RefreshToken
import com.hedvig.authlib.RefreshTokenGrant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthTokenServiceImpl(
  private val authTokenStorage: AuthTokenStorage,
  private val authRepository: AuthRepository,
  private val coroutineScope: CoroutineScope,
) : AuthTokenService {

  @Suppress("NAME_SHADOWING")
  override val authStatus: StateFlow<AuthStatus?> = authTokenStorage.getTokens()
    .mapLatest { (accessToken, refreshToken) ->
      val accessToken = accessToken ?: return@mapLatest AuthStatus.LoggedOut
      val refreshToken = refreshToken ?: return@mapLatest AuthStatus.LoggedOut
      AuthStatus.LoggedIn(accessToken, refreshToken)
    }
    .stateIn(
      coroutineScope,
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
        invalidateTokens()
        null
      }
      is AuthTokenResult.Success -> {
        authTokenStorage.updateTokens(result.accessToken, result.refreshToken)
        result.accessToken
      }
    }
  }

  override fun updateTokens(accessToken: AccessToken, refreshToken: RefreshToken) {
    coroutineScope.launch {
      authTokenStorage.updateTokens(accessToken, refreshToken)
    }
  }

  override fun invalidateTokens() {
    coroutineScope.launch {
      authTokenStorage.clearTokens()
    }
  }

  private fun getRefreshToken(): RefreshToken? {
    return (authStatus.value as? AuthStatus.LoggedIn)?.refreshToken
  }

  override suspend fun migrateFromToken(token: String) {
    when (val result = authRepository.migrateOldToken(token)) {
      is AuthTokenResult.Error -> {
        // logout
      }
      is AuthTokenResult.Success -> {
        updateTokens(result.accessToken, result.refreshToken)
      }
    }
  }
}
