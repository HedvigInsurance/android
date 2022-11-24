package com.hedvig.android.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {

  suspend fun startLoginAttempt(
    loginMethod: LoginMethod,
    market: String,
    personalNumber: String? = null,
    email: String? = null,
  ): AuthAttemptResult

  fun observeLoginStatus(statusUrl: StatusUrl): Flow<LoginStatusResult>

  suspend fun submitOtp(statusUrl: StatusUrl, otp: String): SubmitOtpResult

  suspend fun submitAuthorizationCode(authorizationCode: AuthorizationCode): AuthTokenResult

  suspend fun logout(refreshCode: RefreshCode): LogoutResult
}

enum class LoginMethod {
  SE_BANKID, ZIGNSEC, EMAIL_OTP
}

sealed interface AuthAttemptResult {

  data class Error(
    val message: String,
  ) : AuthAttemptResult

  data class BankIdProperties(
    val id: String,
    val statusUrl: StatusUrl,
    val autoStartToken: String,
  ) : AuthAttemptResult

  data class ZignSecProperties(
    val id: String,
    val statusUrl: StatusUrl,
    val redirectUrl: String,
  ) : AuthAttemptResult
}

@JvmInline
value class StatusUrl(val url: String)

sealed interface AuthTokenResult {

  data class Error(val message: String) : AuthTokenResult

  data class Success(
    val accessToken: AccessToken,
    val refreshToken: RefreshToken,
  ) : AuthTokenResult
}

sealed interface LoginStatusResult {
  data class Failed(val message: String) : LoginStatusResult
  data class Pending(val statusMessage: String?) : LoginStatusResult
  data class Completed(val authorizationCode: LoginAuthorizationCode) : LoginStatusResult
}


sealed interface SubmitOtpResult {
  data class Error(val message: String) : SubmitOtpResult
  data class Success(val loginAuthorizationCode: LoginAuthorizationCode) : SubmitOtpResult
}


sealed interface AuthorizationCode {
  val code: String
}

@JvmInline
value class LoginAuthorizationCode(override val code: String) : AuthorizationCode

@JvmInline
value class RefreshCode(override val code: String) : AuthorizationCode


data class AccessToken(
  val token: String,
  val expiryInSeconds: Int,
)

data class RefreshToken(
  val token: RefreshCode,
  val expiryInSeconds: Int,
)

sealed interface LogoutResult {
  data class Error(val message: String) : LogoutResult
  object Success : LogoutResult
}
