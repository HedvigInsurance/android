package com.hedvig.android.auth.interceptor

import com.hedvig.android.auth.AccessTokenProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthTokenRefreshingInterceptor(
  private val accessTokenProvider: AccessTokenProvider,
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val retrievedAccessToken = runBlocking { accessTokenProvider.provide() }
    return if (retrievedAccessToken != null) {
      chain.proceed(chain.request().withAuthorizationToken(retrievedAccessToken))
    } else {
      chain.proceed(chain.request())
    }
  }
}

private fun Request.withAuthorizationToken(token: String): Request {
  return newBuilder()
    .header("Authorization", "Bearer $token")
    .build()
}
