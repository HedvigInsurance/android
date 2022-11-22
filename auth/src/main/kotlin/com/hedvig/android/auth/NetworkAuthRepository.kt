package com.hedvig.android.auth

import com.hedvig.android.auth.network.createStartLoginRequest
import com.hedvig.android.auth.network.createResponseCallback
import com.hedvig.android.auth.network.responseToResult
import com.hedvig.android.core.common.await
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class NetworkAuthRepository(
  private val okhttpClient: OkHttpClient,
  private val url: String,
) : AuthRepository {

  override suspend fun startLoginAttempt(
    loginMethod: LoginMethod,
    market: String,
    personalNumber: String,
    email: String?,
  ): AuthAttemptResult {

    val requestBody = FormBody
      .Builder()
      .createStartLoginRequest(loginMethod, market, personalNumber, email)

    val request = Request.Builder()
      .post(requestBody)
      .url("$url/member-login")
      .build()

    return withContext(Dispatchers.IO) {
      try {
        okhttpClient.newCall(request)
          .await()
          .responseToResult()
      } catch (e: java.lang.Exception) {
        AuthAttemptResult.Error("Error: ${e.message}")
      }
    }
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
