package com.hedvig.android.auth.network

import com.hedvig.android.auth.AuthRepository
import com.hedvig.android.auth.AuthTokenResult
import com.hedvig.android.auth.AuthenticationTokenService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AccessTokenAuthenticator(
  private val authenticationTokenService: AuthenticationTokenService,
  private val authRepository: AuthRepository,
) : Authenticator {

  override fun authenticate(route: Route?, response: Response): Request? {
    val refreshToken = authenticationTokenService.refreshToken ?: return null
    val result = synchronized(this) {
      runBlocking {
        authRepository.submitAuthorizationCode(refreshToken.token)
      }
    }

    return when (result) {
      is AuthTokenResult.Error -> {
        authenticationTokenService.refreshToken = null
        authenticationTokenService.authenticationToken = null
        null
      }
      is AuthTokenResult.Success -> {
        authenticationTokenService.refreshToken = result.refreshToken
        authenticationTokenService.authenticationToken = result.accessToken.token

        response.request
          .newBuilder()
          .removeHeader("Authorization")
          .header("Authorization", result.accessToken.token)
          .build()
      }
    }
  }
}
