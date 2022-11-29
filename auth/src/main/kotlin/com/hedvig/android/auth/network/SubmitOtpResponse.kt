package com.hedvig.android.auth.network

import com.hedvig.android.auth.LoginAuthorizationCode
import com.hedvig.android.auth.SubmitOtpResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Response

@Serializable
internal data class SubmitOtpResponse(
  val authorizationCode: String?,
  val statusText: String?,
  val result: String,
)

internal fun Response.toSubmitOtpResult(json: Json): SubmitOtpResult {
  val responseBody = body?.string()
  return if (isSuccessful && responseBody != null) {
    val submitOtpResponse = json.decodeFromString<SubmitOtpResponse>(responseBody)
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
