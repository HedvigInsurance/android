package com.hedvig.android.auth.interceptor

import com.hedvig.android.auth.AuthTokenService
import okhttp3.Interceptor
import okhttp3.Response

class ExistingAuthTokenAppendingInterceptor(
  private val authTokenService: AuthTokenService,
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val currentToken = authTokenService.getToken()
    if (currentToken != null) {
      return chain.proceed(chain.request().withAuthorizationToken(currentToken.token))
    }
    return chain.proceed(chain.request())
  }
}
