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

fun Response.toSubmitOtpResult(): SubmitOtpResult {
  val responseBody = body?.string()
  val result = if (isSuccessful && responseBody != null) {
    val response = Json.decodeFromString<SubmitOtpResponse>(responseBody)
    SubmitOtpResult.Success(LoginAuthorizationCode(response.authorizationCode))
  } else {
    SubmitOtpResult.Error(message)
  }
  return result
}
