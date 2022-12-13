package com.hedvig.android.auth.interceptor

import com.hedvig.android.auth.AuthTokenService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AccessTokenAuthenticator(
  private val authTokenService: AuthTokenService,
) : Authenticator {

  private val mutex = Mutex()

  override fun authenticate(route: Route?, response: Response): Request? {
    return runBlocking {
      val accessToken = authTokenService.getToken()
      mutex.withLock {
        val newAccessToken = authTokenService.getToken()
        if (newAccessToken != null && newAccessToken != accessToken) {
          return@withLock response.request.withAuthorizationToken(newAccessToken.token)
        }
        // Still same old token, so fetch a new one.
        val refreshedToken = authTokenService.refreshAndGetToken() ?: return@withLock null
        response.request.withAuthorizationToken(refreshedToken.token)
      }
    }
  }
}
