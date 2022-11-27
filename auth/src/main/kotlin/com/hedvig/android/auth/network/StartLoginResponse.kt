package com.hedvig.android.auth.network

import com.hedvig.android.auth.AuthAttemptResult
import com.hedvig.android.auth.StatusUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Response

@Serializable
internal data class StartLoginResponse(
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

internal fun Response.toAuthAttemptResult(json: Json): AuthAttemptResult {
  val responseBody = body?.string()
  return if (isSuccessful && responseBody != null) {
    json.decodeFromString<StartLoginResponse>(responseBody).toAuthAttemptResult()
  } else {
    AuthAttemptResult.Error(message = responseBody ?: "Unknown error")
  }
}

private fun StartLoginResponse.toAuthAttemptResult(): AuthAttemptResult = when {
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
