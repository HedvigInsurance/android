package com.hedvig.android.auth.interceptor

import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.auth.LoggedInState
import okhttp3.Interceptor
import okhttp3.Response

class ExistingTokenAppendingInterceptor(
  private val authTokenService: AuthTokenService,
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val builder = chain.request().newBuilder()

    val currentToken = (authTokenService.loggedInState().value as? LoggedInState.LoggedIn)?.accessToken
    if (currentToken != null) {
      builder.header("Authorization", currentToken.token)
    }
    return chain.proceed(builder.build())
  }
}
