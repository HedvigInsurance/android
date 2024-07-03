package com.hedvig.android.auth.test

// TODO("todo does not work without authlib")
// class FakeAuthRepository : AuthRepository {
//  val authAttemptResponse = Turbine<AuthAttemptResult>()
//  val loginStatusResponse = Turbine<LoginStatusResult>()
//  val resendOtpResponse = Turbine<ResendOtpResult>()
//  val submitOtpResponse = Turbine<SubmitOtpResult>()
//  val exchangeResponse = Turbine<AuthTokenResult>()
//
//  override suspend fun startLoginAttempt(
//    loginMethod: LoginMethod,
//    market: OtpMarket,
//    personalNumber: String?,
//    email: String?,
//  ): AuthAttemptResult {
//    return authAttemptResponse.awaitItem()
//  }
//
//  override fun observeLoginStatus(statusUrl: StatusUrl): Flow<LoginStatusResult> {
//    return loginStatusResponse.asChannel().receiveAsFlow()
//  }
//
//  override suspend fun submitOtp(verifyUrl: String, otp: String): SubmitOtpResult {
//    return submitOtpResponse.awaitItem()
//  }
//
//  override suspend fun resendOtp(resendUrl: String): ResendOtpResult {
//    return resendOtpResponse.awaitItem()
//  }
//
//  override suspend fun exchange(grant: Grant): AuthTokenResult {
//    return exchangeResponse.awaitItem()
//  }
//
//  override suspend fun loginStatus(statusUrl: StatusUrl): LoginStatusResult {
//    error("Not implemented")
//  }
//
//  override suspend fun revoke(token: String): RevokeResult {
//    error("Not implemented")
//  }
// }
