package com.hedvig.android.auth.network

import com.hedvig.android.auth.LoginAuthorizationCode
import com.hedvig.android.auth.SubmitOtpResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Response

@Serializable
data class SubmitOtpResponse(
  val authorizationCode: String,
)

fun Response.toSubmitOtpResult(json: Json): SubmitOtpResult {
  val responseBody = body?.string()
  return if (isSuccessful && responseBody != null) {
    val submitOtpResponse = json.decodeFromString<SubmitOtpResponse>(responseBody)
    SubmitOtpResult.Success(LoginAuthorizationCode(submitOtpResponse.authorizationCode))
  } else {
    SubmitOtpResult.Error(message = responseBody ?: "Unknown error")
  }
}
