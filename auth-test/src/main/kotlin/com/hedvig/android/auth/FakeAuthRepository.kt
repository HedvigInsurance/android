package com.hedvig.android.auth

import app.cash.turbine.Turbine
import com.hedvig.authlib.AuthAttemptResult
import com.hedvig.authlib.AuthRepository
import com.hedvig.authlib.AuthTokenResult
import com.hedvig.authlib.Grant
import com.hedvig.authlib.LoginMethod
import com.hedvig.authlib.LoginStatusResult
import com.hedvig.authlib.ResendOtpResult
import com.hedvig.authlib.RevokeResult
import com.hedvig.authlib.StatusUrl
import com.hedvig.authlib.SubmitOtpResult
import kotlinx.coroutines.flow.Flow

class FakeAuthRepository : AuthRepository {

  val resendOtpResponse = Turbine<ResendOtpResult>()
  val submitOtpResponse = Turbine<SubmitOtpResult>()
  val exchangeResponse = Turbine<AuthTokenResult>()

  override suspend fun startLoginAttempt(
    loginMethod: LoginMethod,
    market: String,
    personalNumber: String?,
    email: String?,
  ): AuthAttemptResult {
    error("Not implemented")
  }

  override fun observeLoginStatus(statusUrl: StatusUrl): Flow<LoginStatusResult> {
    error("Not implemented")
  }

  override suspend fun submitOtp(verifyUrl: String, otp: String): SubmitOtpResult {
    return submitOtpResponse.awaitItem()
  }

  override suspend fun resendOtp(resendUrl: String): ResendOtpResult {
    return resendOtpResponse.awaitItem()
  }

  override suspend fun exchange(grant: Grant): AuthTokenResult {
    return exchangeResponse.awaitItem()
  }

  override suspend fun loginStatus(statusUrl: StatusUrl): LoginStatusResult {
    error("Not implemented")
  }

  override suspend fun revoke(token: String): RevokeResult {
    error("Not implemented")
  }
}
