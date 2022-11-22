package com.hedvig.android.auth.network

import com.hedvig.android.auth.AuthorizationCode
import com.hedvig.android.auth.LoginStatusResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.Response

@Serializable
data class LoginStatusResponse(
  val status: LoginStatus,
  val seBankidHintCode: String?,
  val authorizationCode: String?,
) {
  enum class LoginStatus {
    PENDING, FAILED, COMPLETED
  }
}

fun Response.toLoginStatusResult(): LoginStatusResult {
  val responseBody = body?.string()
  val result = if (isSuccessful && responseBody != null) {
    Json.decodeFromString<LoginStatusResponse>(responseBody).toLoginStatusResult()
  } else {
    LoginStatusResult.Failed(message = message)
  }
  return result
}

private fun LoginStatusResponse.toLoginStatusResult() = when (status) {
  LoginStatusResponse.LoginStatus.PENDING -> LoginStatusResult.Pending(seBankidHintCode)
  LoginStatusResponse.LoginStatus.FAILED -> LoginStatusResult.Failed("Login status failed")
  LoginStatusResponse.LoginStatus.COMPLETED -> {
    require(authorizationCode != null) {
      "Login status completed but did not receive authorization code"
    }

    val code = AuthorizationCode(authorizationCode)
    LoginStatusResult.Completed(code)
  }
}
