package com.hedvig.android.auth

import com.hedvig.android.auth.network.createLogoutRequestBody
import com.hedvig.android.auth.network.createRequestBody
import com.hedvig.android.auth.network.createStartLoginRequest
import com.hedvig.android.auth.network.createSubmitOtpRequest
import com.hedvig.android.auth.network.toAuthAttemptResult
import com.hedvig.android.auth.network.toAuthTokenResult
import com.hedvig.android.auth.network.toLoginStatusResult
import com.hedvig.android.auth.network.toSubmitOtpResult
import com.hedvig.android.core.common.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    val requestBody = createStartLoginRequest(loginMethod, market, personalNumber, email)

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

  override fun observeLoginStatus(statusUrl: StatusUrl): Flow<LoginStatusResult> {
    val request = Request.Builder()
      .get()
      .url("$url${statusUrl.url}")
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

  override suspend fun submitOtp(statusUrl: StatusUrl, otp: String): SubmitOtpResult {
    val requestBody = createSubmitOtpRequest(otp)
    val request = Request.Builder()
      .post(requestBody)
      .url("$url/${statusUrl.url}/otp")
      .build()

    return try {
      okhttpClient.newCall(request)
        .await()
        .toSubmitOtpResult()
    } catch (e: java.lang.Exception) {
      SubmitOtpResult.Error("Error: ${e.message}")
    }
  }

  override suspend fun submitAuthorizationCode(authorizationCode: AuthorizationCode): AuthTokenResult {
    val request = Request.Builder()
      .post(authorizationCode.createRequestBody())
      .url("$url/oauth/token")
      .build()

    return try {
      okhttpClient.newCall(request)
        .await()
        .toAuthTokenResult()
    } catch (e: java.lang.Exception) {
      AuthTokenResult.Error("Error: ${e.message}")
    }
  }

  override suspend fun logout(refreshCode: RefreshCode): LogoutResult {
    val request = Request.Builder()
      .post(createLogoutRequestBody(refreshCode))
      .url("$url/oauth/logout")
      .build()

    return try {
      val result = okhttpClient.newCall(request).await()
      if (result.isSuccessful) {
        LogoutResult.Success
      } else {
        LogoutResult.Error("Could not logout: ${result.message}")
      }
    } catch (e: java.lang.Exception) {
      LogoutResult.Error("Error: ${e.message}")
    }
  }
}
