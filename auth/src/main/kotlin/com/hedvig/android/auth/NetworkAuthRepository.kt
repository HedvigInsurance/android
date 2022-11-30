package com.hedvig.android.auth

import com.hedvig.android.auth.network.createLoginStatusResult
import com.hedvig.android.auth.network.toAuthAttemptResult
import com.hedvig.android.auth.network.toAuthTokenResult
import com.hedvig.android.auth.network.toResendOtpResult
import com.hedvig.android.auth.network.toSubmitOtpResult
import com.hedvig.android.core.common.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

private const val POLL_DELAY_MILLIS = 1000L
const val SUCCESS_CALLBACK_URL = "https://google.se?q=success"
const val FAILURE_CALLBACK_URL = "https://google.se?q=failure"

class NetworkAuthRepository(
  private val url: String,
) : AuthRepository {

  private val okHttpClient = OkHttpClient.Builder().build()

  private val jsonBuilder = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
  }

  override suspend fun startLoginAttempt(
    loginMethod: LoginMethod,
    market: String,
    personalNumber: String?,
    email: String?,
  ): AuthAttemptResult {

    val requestBody = buildJsonObject {
      put("method", loginMethod.name)
      put("country", market)
      put("callbackSuccess", SUCCESS_CALLBACK_URL)
      put("callbackFailure", FAILURE_CALLBACK_URL)

      if (personalNumber != null) {
        put("personalNumber", personalNumber)
      }

      if (email != null) {
        put("email", email)
      }
    }
      .toString()
      .toRequestBody()

    val request = createPostRequest("$url/member-login", requestBody)

    return try {
      okHttpClient.newCall(request)
        .await()
        .let(jsonBuilder::toAuthAttemptResult)
    } catch (e: Exception) {
      AuthAttemptResult.Error("Error: ${e.message}")
    }
  }

  override fun observeLoginStatus(statusUrl: StatusUrl): Flow<LoginStatusResult> {
    val request = Request.Builder()
      .get()
      .url("$url${statusUrl.url}")
      .build()

    return flow {
      while (true) {
        try {
          val loginStatusResult = okHttpClient
            .newCall(request)
            .await()
            .let(jsonBuilder::createLoginStatusResult)

          emit(loginStatusResult)

          if (loginStatusResult is LoginStatusResult.Pending) {
            delay(POLL_DELAY_MILLIS)
          } else {
            break
          }
        } catch (e: Exception) {
          emit(LoginStatusResult.Failed("Error: ${e.message}"))
        }
      }
    }
  }

  override suspend fun submitOtp(verifyUrl: String, otp: String): SubmitOtpResult {
    val requestBody = buildJsonObject {
      put("otp", otp)
    }
      .toString()
      .toRequestBody()

    val request = createPostRequest("$url${verifyUrl}", requestBody)

    return try {
      okHttpClient.newCall(request)
        .await()
        .let(jsonBuilder::toSubmitOtpResult)
    } catch (e: Exception) {
      SubmitOtpResult.Error("Error: ${e.message}")
    }
  }

  override suspend fun resendOtp(resendUrl: String): ResendOtpResult {
    val request = createPostRequest("$url${resendUrl}", ByteArray(0).toRequestBody())

    return try {
      okHttpClient.newCall(request)
        .await()
        .let(::toResendOtpResult)
    } catch (e: Exception) {
      ResendOtpResult.Error("Error: ${e.message}")
    }
  }

  override suspend fun submitAuthorizationCode(authorizationCode: AuthorizationCode): AuthTokenResult {
    val requestBody = buildJsonObject {
      when (authorizationCode) {
        is LoginAuthorizationCode -> {
          put("authorization_code", authorizationCode.code)
          put("grant_type", "authorization_code")
        }
        is RefreshCode -> {
          put("refresh_token", authorizationCode.code)
          put("grant_type", "refresh_token")
        }
      }
    }
      .toString()
      .toRequestBody()

    val request = createPostRequest("$url/oauth/token", requestBody)

    return try {
      okHttpClient.newCall(request)
        .await()
        .let(jsonBuilder::toAuthTokenResult)
    } catch (e: Exception) {
      AuthTokenResult.Error("Error: ${e.message}")
    }
  }

  override suspend fun logout(refreshCode: RefreshCode): LogoutResult {
    val requestBody = buildJsonObject {
      put("refresh_token", refreshCode.code)
    }
      .toString()
      .toRequestBody()

    val request = createPostRequest("$url/oauth/logout", requestBody)

    return try {
      val result = okHttpClient.newCall(request).await()
      if (result.isSuccessful) {
        LogoutResult.Success
      } else {
        LogoutResult.Error("Could not logout: ${result.message}")
      }
    } catch (e: Exception) {
      LogoutResult.Error("Error: ${e.message}")
    }
  }

  private fun createPostRequest(url: String, requestBody: RequestBody) = Request.Builder()
    .header("Content-Type", "application/json")
    .post(requestBody)
    .url(url)
    .build()
}
