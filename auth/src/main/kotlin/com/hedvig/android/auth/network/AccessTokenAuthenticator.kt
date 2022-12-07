package com.hedvig.android.auth.network

import com.hedvig.android.auth.AuthenticationTokenService
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.RefreshTokenGrant
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AccessTokenAuthenticator(
  private val authenticationTokenService: AuthenticationTokenService,
  private val authRepository: AuthRepository,
) : Authenticator {

  private val mutex = Mutex()

  override fun authenticate(route: Route?, response: Response): Request? {
    return runBlocking {
      val refreshToken = authenticationTokenService.refreshToken ?: return@runBlocking null

      return@runBlocking mutex.withLock {
        when (val result = authRepository.exchange(RefreshTokenGrant(refreshToken.token))) {
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
  }
}
