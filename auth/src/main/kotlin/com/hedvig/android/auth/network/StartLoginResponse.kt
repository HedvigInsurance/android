package com.hedvig.android.auth.network

import com.hedvig.android.auth.AuthAttemptResult
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
}

fun Response.toAuthAttemptResult(): AuthAttemptResult {
  val responseBody = body?.string()
  val result = if (isSuccessful && responseBody != null) {
    Json.decodeFromString<StartLoginResponse>(responseBody).toAuthAttemptResult()
  } else {
    AuthAttemptResult.Error(message = message)
  }
  return result
}

private fun StartLoginResponse.toAuthAttemptResult() = when {
  seBankIdProperties != null -> AuthAttemptResult.BankIdProperties(
    id = id,
    statusUrl = statusUrl,
    autoStartToken = seBankIdProperties.autoStartToken,
  )
  zignSecProperties != null -> AuthAttemptResult.ZignSecProperties(
    id = id,
    statusUrl = statusUrl,
    redirectUrl = zignSecProperties.redirectUrl,
  )
  else -> AuthAttemptResult.Error(
    message = "Could not find properties on start login response",
  )
}


