package com.hedvig.android.auth

import com.hedvig.android.auth.storage.AuthenticationDatastore
import com.hedvig.android.core.common.ApplicationScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LoggedInState {
  data class LoggedIn(val accessToken: AccessToken, val refreshToken: RefreshToken) : LoggedInState
  object LoggedOut : LoggedInState
}

interface AuthenticationRepository {
  fun loggedInState(): StateFlow<LoggedInState?>
  fun updateTokens(accessToken: AccessToken, refreshToken: RefreshToken)
}

internal class DataStoreAuthenticationRepository(
  private val authenticationDatastore: AuthenticationDatastore,
  private val applicationScope: ApplicationScope,
) : AuthenticationRepository {

  @Suppress("NAME_SHADOWING")
  private val loggedInState: StateFlow<LoggedInState?> = authenticationDatastore.getTokens()
    .mapLatest { (accessToken, refreshToken) ->
      val accessToken = accessToken ?: return@mapLatest LoggedInState.LoggedOut
      val refreshToken = refreshToken ?: return@mapLatest LoggedInState.LoggedOut
      LoggedInState.LoggedIn(accessToken, refreshToken)
    }
    .stateIn(
      applicationScope,
      SharingStarted.Eagerly,
      null,
    )

  override fun loggedInState(): StateFlow<LoggedInState?> {
    return loggedInState
  }

  override fun updateTokens(accessToken: AccessToken, refreshToken: RefreshToken) {
    applicationScope.launch {
      authenticationDatastore.updateTokens(accessToken, refreshToken)
    }
  }
}
