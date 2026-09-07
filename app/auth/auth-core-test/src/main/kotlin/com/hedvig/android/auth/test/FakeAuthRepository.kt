package com.hedvig.android.auth.test

import app.cash.turbine.Turbine
import com.hedvig.android.authlib.AuthAttemptResult
import com.hedvig.android.authlib.AuthRepository
import com.hedvig.android.authlib.AuthTokenResult
import com.hedvig.android.authlib.Grant
import com.hedvig.android.authlib.LoginMethod
import com.hedvig.android.authlib.LoginStatusResult
import com.hedvig.android.authlib.OtpMarket
import com.hedvig.android.authlib.ResendOtpResult
import com.hedvig.android.authlib.RevokeResult
import com.hedvig.android.authlib.StatusUrl
import com.hedvig.android.authlib.SubmitOtpResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class FakeAuthRepository : AuthRepository {
  val authAttemptResponse = Turbine<AuthAttemptResult>()
  val loginStatusResponse = Turbine<LoginStatusResult>()
  val resendOtpResponse = Turbine<ResendOtpResult>()
  val submitOtpResponse = Turbine<SubmitOtpResult>()
  val exchangeResponse = Turbine<AuthTokenResult>()

  override suspend fun startLoginAttempt(
    loginMethod: LoginMethod,
    market: OtpMarket,
    personalNumber: String?,
    email: String?,
  ): AuthAttemptResult {
    return authAttemptResponse.awaitItem()
  }

  override fun observeLoginStatus(statusUrl: StatusUrl): Flow<LoginStatusResult> {
    return loginStatusResponse.asChannel().receiveAsFlow()
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

  override suspend fun revoke(token: String): RevokeResult {
    error("Not implemented")
  }
}
