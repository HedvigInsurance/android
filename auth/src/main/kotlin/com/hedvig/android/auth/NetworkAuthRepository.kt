package com.hedvig.android.auth

import com.hedvig.android.auth.network.LoginStatusResponse
import com.hedvig.android.auth.network.createStartLoginRequest
import com.hedvig.android.auth.network.toAuthAttemptResult
import com.hedvig.android.auth.network.toLoginStatusResult
import com.hedvig.android.core.common.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable.cancel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

private const val POLL_DELAY_MILLIS = 1000L

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

    return try {
      okhttpClient.newCall(request)
        .await()
        .toAuthAttemptResult()
    } catch (e: java.lang.Exception) {
      AuthAttemptResult.Error("Error: ${e.message}")
    }
  }

  override fun observeLoginStatus(statusUrl: String): Flow<LoginStatusResult> {
    val request = Request.Builder()
      .get()
      .url("$url$statusUrl")
      .build()

    var isPending = false

    return flow {
      while (isPending) {
        try {
          val loginStatusResult = okhttpClient
            .newCall(request)
            .await()
            .toLoginStatusResult()

          emit(loginStatusResult)

          isPending = loginStatusResult.isPending()
          if (isPending) {
            delay(POLL_DELAY_MILLIS)
          }
        } catch (e: Exception) {
          emit(LoginStatusResult.Failed("Error: ${e.message}"))
        }
      }
    }
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
