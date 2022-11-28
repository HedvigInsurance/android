package com.hedvig.android.auth.network

import com.hedvig.android.auth.LoginAuthorizationCode
import com.hedvig.android.auth.SubmitOtpResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Response

@Serializable
data class SubmitOtpResponse(
  val authorizationCode: String?,
  val statusText: String?,
  val result: String,
)

fun Json.toSubmitOtpResult(response: Response): SubmitOtpResult {
  val responseBody = response.body?.string()
  return if (response.isSuccessful && responseBody != null) {
    val submitOtpResponse = decodeFromString<SubmitOtpResponse>(responseBody)
    when (submitOtpResponse.result) {
      "success" -> {
        SubmitOtpResult.Success(LoginAuthorizationCode(submitOtpResponse.authorizationCode!!))
      }
      "error" -> {
        SubmitOtpResult.Error(submitOtpResponse.statusText ?: "Unknown error")
      }
      else -> SubmitOtpResult.Error("Unknown error")
    }
  } else {
    SubmitOtpResult.Error(message = responseBody ?: "Unknown error")
  }
}
