package com.hedvig.android.auth

import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.Request

class NetworkAuthRepository(
  val okhttpClient: OkHttpClient,
) : AuthRepository {
  override fun startLoginAttempt(loginMethod: LoginMethod, market: String, email: String): AuthAttemptResult {
    TODO("Not yet implemented")
  }

  override fun observeLoginStatus(): Flow<LoginStatusResult> {
    TODO("Not yet implemented")
  }

  override fun submitOtp(otp: String): AuthorizationCode {
    TODO("Not yet implemented")
  }

  override fun submitAuthorizationCode(authorizationCode: AuthorizationCode): AuthTokenResult {
    TODO("Not yet implemented")
  }

  override fun submitRefreshToken(refreshToken: String): AuthTokenResult {
    TODO("Not yet implemented")
  }

  override fun logout(refreshToken: String): LogoutResult {
    TODO("Not yet implemented")
  }
}
