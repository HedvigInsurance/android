package com.hedvig.android.auth.network

import com.hedvig.android.auth.LoginAuthorizationCode
import com.hedvig.android.auth.LoginStatusResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Response

@Serializable
data class LoginStatusResponse(
  val status: LoginStatus,
  val statusText: String,
  val authorizationCode: String?,
) {
  enum class LoginStatus {
    PENDING, FAILED, COMPLETED
  }
}

fun Json.createLoginStatusResult(response: Response): LoginStatusResult {
  val responseBody = response.body?.string()
  return if (response.isSuccessful && responseBody != null) {
    decodeFromString<LoginStatusResponse>(responseBody).toLoginStatusResult()
  } else {
    LoginStatusResult.Failed(message = responseBody ?: "Unknown error")
  }
}

private fun LoginStatusResponse.toLoginStatusResult() = when (status) {
  LoginStatusResponse.LoginStatus.PENDING -> LoginStatusResult.Pending(statusText)
  LoginStatusResponse.LoginStatus.FAILED -> LoginStatusResult.Failed(statusText)
  LoginStatusResponse.LoginStatus.COMPLETED -> {
    if (authorizationCode == null) {
      LoginStatusResult.Failed("Did not get authorization code")
    } else {
      val code = LoginAuthorizationCode(authorizationCode)
      LoginStatusResult.Completed(code)
    }
  }
}
