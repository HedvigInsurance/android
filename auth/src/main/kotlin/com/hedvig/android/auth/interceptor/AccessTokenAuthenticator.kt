package com.hedvig.android.auth.interceptor

import com.hedvig.android.auth.AuthRepository
import com.hedvig.android.auth.AuthTokenResult
import com.hedvig.android.auth.AuthenticationTokenService
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
    return try {
      runBlocking {
        val refreshToken = authenticationTokenService.refreshToken ?: return@runBlocking null

        return@runBlocking mutex.withLock {
          when (val result = authRepository.submitAuthorizationCode(refreshToken.token)) {
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
    } catch (e: Exception) {
      null
    }
  }
}
