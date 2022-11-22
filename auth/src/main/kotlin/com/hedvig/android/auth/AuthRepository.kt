package com.hedvig.android.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {

  suspend fun startLoginAttempt(
    loginMethod: LoginMethod,
    market: String,
    personalNumber: String,
    email: String? = null,
  ): AuthAttemptResult

  fun observeLoginStatus(): Flow<LoginStatusResult>

  fun submitOtp(otp: String): AuthorizationCode

  fun submitAuthorizationCode(authorizationCode: AuthorizationCode): AuthTokenResult

  fun submitRefreshToken(refreshToken: String): AuthTokenResult

  fun logout(refreshToken: String): LogoutResult
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
    val statusUrl: String,
    val autoStartToken: String,
  ) : AuthAttemptResult

  data class ZignSecProperties(
    val id: String,
    val statusUrl: String,
    val redirectUrl: String,
  ) : AuthAttemptResult

  data class OtpProperties(
    val id: String,
    val statusUrl: String,
    val validationUrl: String,
  ) : AuthAttemptResult
}

sealed interface AuthTokenResult {

  data class Error(val message: String) : AuthTokenResult

  data class Success(
    val accessToken: AccessToken,
    val refreshToken: RefreshToken,
  ) : AuthTokenResult
}

sealed interface LoginStatusResult {
  data class Failed(val message: String) : LoginStatusResult
  data class Pending(val message: String) : LoginStatusResult
  data class Completed(val authorizationCode: AuthorizationCode) : LoginStatusResult
}

@JvmInline
value class AuthorizationCode(val code: String)

data class AccessToken(
  val token: String,
  val expiryInSeconds: Int,
)

data class RefreshToken(
  val token: String,
  val expiryInSeconds: Int,
)

sealed interface LogoutResult {
  data class Error(val message: String) : LogoutResult
  object Success : LogoutResult
}
