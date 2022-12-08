package com.hedvig.android.auth.network

import com.hedvig.android.auth.AuthenticationTokenService
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.RefreshTokenGrant
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val MAX_PEEK_BYTES = 1000000L

class AuthInterceptor(
  private val authRepository: AuthRepository,
  private val authenticationTokenService: AuthenticationTokenService,
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val original = chain.request()
    val builder = original.newBuilder().method(original.method, original.body)

    val accessToken = authenticationTokenService.authenticationToken ?: return chain.proceed(builder.build())

    val response = builder.proceedWithAuthorization(accessToken, chain)

    return if (isUnauthorized(response)) {
      handleUnauthorized(accessToken, builder, chain, response)
    } else {
      response
    }
  }

  private fun handleUnauthorized(
    accessToken: String,
    builder: Request.Builder,
    chain: Interceptor.Chain,
    originalResponse: Response,
  ): Response {
    val newAccessToken = authenticationTokenService.authenticationToken
    return if (newAccessToken != accessToken) {
      // Access token refresh finished by another request, try that instead
      builder.proceedWithAuthorization(newAccessToken, chain)
    } else {
      // Use stored refresh token
      val refreshToken = authenticationTokenService.refreshToken ?: return originalResponse
      fetchNewTokenAndUpdateStored(refreshToken.token, originalResponse, chain, builder)
    }
  }

  private fun fetchNewTokenAndUpdateStored(
    refreshToken: String,
    originalResponse: Response,
    chain: Interceptor.Chain,
    request: Request.Builder,
  ): Response = synchronized(this) {
    runBlocking {
      when (val result = authRepository.exchange(RefreshTokenGrant(refreshToken))) {
        is AuthTokenResult.Error -> {
          authenticationTokenService.refreshToken = null
          authenticationTokenService.authenticationToken = null
          // TODO: Logout
          originalResponse
        }
        is AuthTokenResult.Success -> {
          authenticationTokenService.refreshToken = result.refreshToken
          authenticationTokenService.authenticationToken = result.accessToken.token

          request.proceedWithAuthorization(result.accessToken.token, chain)
        }
      }
    }
  }

  private fun Request.Builder.proceedWithAuthorization(token: String?, chain: Interceptor.Chain): Response {
    token?.let { header("Authorization", it) }
    return chain.proceed(build())
  }

  // GraphQL will often respond with 200, but the body will contain codes or statuses
  // explaining that some service(s) responded with 401
  private fun isUnauthorized(response: Response): Boolean {
    val responseBody = response.peekBody(MAX_PEEK_BYTES).string()
    return response.code == 401
      || responseBody.contains("status: 401")
      || responseBody.contains("UNAUTHENTICATED")
  }
}
