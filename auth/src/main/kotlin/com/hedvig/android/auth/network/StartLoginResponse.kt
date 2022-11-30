package com.hedvig.android.auth.network

import com.hedvig.android.auth.AuthAttemptResult
import com.hedvig.android.auth.StatusUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Response

@Serializable
data class StartLoginResponse(
  val id: String,
  val method: String,
  val statusUrl: String,
  val seBankIdProperties: BankIdProperties?,
  val zignSecProperties: ZignSecProperties?,
  val otpProperties: OtpProperties?,
) {
  @Serializable
  data class BankIdProperties(
    val orderRef: String,
    val autoStartToken: String,
  )

  @Serializable
  data class ZignSecProperties(
    val redirectUrl: String,
  )

  @Serializable
  data class OtpProperties(
    val otp: String
  )
}

fun Json.toAuthAttemptResult(response: Response): AuthAttemptResult {
  val responseBody = response.body?.string()
  return if (response.isSuccessful && responseBody != null) {
    decodeFromString<StartLoginResponse>(responseBody).toAuthAttemptResult()
  } else {
    AuthAttemptResult.Error(message = responseBody ?: "Unknown error")
  }
}

private fun StartLoginResponse.toAuthAttemptResult() = when {
  seBankIdProperties != null -> AuthAttemptResult.BankIdProperties(
    id = id,
    statusUrl = StatusUrl(statusUrl),
    autoStartToken = seBankIdProperties.autoStartToken,
  )
  zignSecProperties != null -> AuthAttemptResult.ZignSecProperties(
    id = id,
    statusUrl = StatusUrl(statusUrl),
    redirectUrl = zignSecProperties.redirectUrl,
  )
  else -> AuthAttemptResult.Error(
    message = "Could not find properties on start login response",
  )
}


