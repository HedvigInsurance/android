package com.hedvig.android.auth.network

import com.hedvig.android.auth.AccessToken
import com.hedvig.android.auth.AuthTokenResult
import com.hedvig.android.auth.RefreshToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Response

@Serializable
data class SubmitAuthorizationCodeResponse(
  @SerialName("access_token")
  val accessToken: String,
  @SerialName("expires_in")
  val accessTokenExpiresIn: Int,
  @SerialName("refresh_token")
  val refreshToken: String,
  @SerialName("refresh_token_expires_in")
  val refreshTokenExpiresIn: Int,
)

fun Response.toAuthTokenResult(): AuthTokenResult {
  val responseBody = body?.string()
  val result = if (isSuccessful && responseBody != null) {
    Json.decodeFromString<SubmitAuthorizationCodeResponse>(responseBody).toAuthAttemptResult()
  } else {
    AuthTokenResult.Error(message)
  }
  return result
}

private fun SubmitAuthorizationCodeResponse.toAuthAttemptResult() = AuthTokenResult.Success(
  accessToken = AccessToken(
    token = accessToken,
    expiryInSeconds = accessTokenExpiresIn,
  ),
  refreshToken = RefreshToken(
    token = refreshToken,
    expiryInSeconds = refreshTokenExpiresIn,
  ),
)
